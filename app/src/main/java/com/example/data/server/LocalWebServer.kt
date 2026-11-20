package com.example.data.server

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Hardened Local Wi-Fi HTTP File Server.
 * - Strictly confined to the canonical shared root directory (prevents path traversal).
 * - Blocks access to private app storage, Safe Vault, databases, and shared preferences.
 * - Sanitizes headers and HTML output.
 */
class LocalWebServer(
    private val context: Context,
    private val rootPathProvider: () -> String
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val serverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow("Offline")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    companion object {
        const val PORT = 8080
    }

    fun startServer(): String {
        if (_isRunning.value) return _serverUrl.value

        val ip = getLocalIpAddress()
        if (ip == null) {
            _isRunning.value = false
            _serverUrl.value = "Connect to Wi-Fi network first"
            return _serverUrl.value
        }

        val url = "http://$ip:$PORT"

        try {
            serverSocket = ServerSocket(PORT)
            _isRunning.value = true
            _serverUrl.value = url

            serverJob = serverScope.launch {
                while (_isRunning.value && serverSocket?.isClosed == false) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        launch {
                            handleClient(clientSocket)
                        }
                    } catch (e: Exception) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            _isRunning.value = false
            _serverUrl.value = "Port $PORT unavailable"
        }

        return _serverUrl.value
    }

    fun stopServer() {
        try {
            _isRunning.value = false
            serverSocket?.close()
            serverSocket = null
            serverJob?.cancel()
            serverJob = null
            _serverUrl.value = "Offline"
        } catch (e: Exception) {
            // Ignore close errors
        }
    }

    private fun resolveSafeTarget(requestedPath: String): File? {
        return try {
            val rootCanonical = File(rootPathProvider()).canonicalPath
            val privateDir = context.filesDir.parentFile?.canonicalPath
            val target = File(requestedPath)
            val targetCanonical = target.canonicalPath

            // Block access to app private internal storage / vault / databases
            if (privateDir != null && (targetCanonical == privateDir || targetCanonical.startsWith(privateDir + File.separator))) {
                // Only allow if rootPath itself is the external/sandbox root, still forbid vault/trash/db/prefs
                val forbidden = listOf("vault", "trash", "databases", "shared_prefs")
                val internalBase = context.filesDir.canonicalPath
                if (forbidden.any {
                        val fPath = File(internalBase, it).canonicalPath
                        val dPath = File(privateDir, it).canonicalPath
                        targetCanonical.startsWith(fPath) || targetCanonical.startsWith(dPath)
                    }
                ) {
                    return null
                }
            }

            if (targetCanonical == rootCanonical || targetCanonical.startsWith(rootCanonical + File.separator)) {
                target
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.use { s ->
                s.soTimeout = 15_000
                val input = s.getInputStream()
                val output = s.getOutputStream()
                val reader = BufferedReader(InputStreamReader(input))

                val requestLine = reader.readLine() ?: return
                val tokens = requestLine.split(" ")
                if (tokens.size < 2) return

                val fullUri = tokens[1]
                val uri = URLDecoder.decode(fullUri, "UTF-8")

                when {
                    uri.startsWith("/download") -> handleDownload(uri, output)
                    uri.startsWith("/browse") -> handleBrowse(uri, output)
                    else -> handleWebUi(File(rootPathProvider()), output)
                }
            }
        } catch (e: Exception) {
            // Client disconnected or socket timeout
        }
    }

    private fun handleBrowse(uri: String, output: OutputStream) {
        val queryIdx = uri.indexOf("path=")
        if (queryIdx == -1) {
            handleWebUi(File(rootPathProvider()), output)
            return
        }
        val requested = uri.substring(queryIdx + 5).substringBefore('&')
        val safeDir = resolveSafeTarget(requested)
        if (safeDir == null || !safeDir.exists() || !safeDir.isDirectory || !safeDir.canRead()) {
            send403(output)
            return
        }
        handleWebUi(safeDir, output)
    }

    private fun handleWebUi(dir: File, output: OutputStream) {
        val safeDir = resolveSafeTarget(dir.absolutePath)
        if (safeDir == null || !safeDir.exists() || !safeDir.canRead()) {
            send403(output)
            return
        }

        val rootCanonical = File(rootPathProvider()).canonicalPath
        val files = safeDir.listFiles()?.filter { !it.name.startsWith(".") } ?: emptyList()

        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'>")
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
        sb.append("<title>Liquid Files - Local Wi-Fi Share</title>")
        sb.append("<style>")
        sb.append("body { background: #07090e; color: #f0f4fc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; padding: 24px; margin: 0; }")
        sb.append(".container { max-width: 800px; margin: 0 auto; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 20px; padding: 24px; }")
        sb.append("h1 { color: #00E5FF; font-size: 22px; margin-top: 0; }")
        sb.append("ul { list-style: none; padding: 0; margin: 20px 0 0 0; }")
        sb.append("li { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid rgba(255,255,255,0.06); }")
        sb.append(".meta { color: #8e99b0; font-size: 12px; }")
        sb.append("a.btn { background: #00E5FF; color: #07090e; font-weight: bold; text-decoration: none; padding: 6px 14px; border-radius: 8px; font-size: 12px; }")
        sb.append("</style></head><body>")
        sb.append("<div class='container'>")
        sb.append("<h1>Liquid Files &bull; Wi-Fi Share</h1>")
        sb.append("<p class='meta'>Local LAN Portal: ${escapeHtml(safeDir.name)}</p>")

        if (safeDir.canonicalPath != rootCanonical) {
            safeDir.parentFile?.let { parent ->
                if (resolveSafeTarget(parent.absolutePath) != null) {
                    val upEncoded = URLEncoder.encode(parent.absolutePath, "UTF-8")
                    sb.append("<p><a class='btn' href='/browse?path=$upEncoded'>&larr; Parent Folder</a></p>")
                }
            }
        }

        sb.append("<ul>")
        if (files.isEmpty()) {
            sb.append("<li style='justify-content: center; color: #8e99b0;'>Folder is empty or storage permission not granted.</li>")
        } else {
            files.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }).forEach { file ->
                val isDir = file.isDirectory
                val sizeText = if (isDir) "Folder" else "${file.length() / 1024} KB"
                val encoded = URLEncoder.encode(file.absolutePath, "UTF-8")
                sb.append("<li>")
                sb.append("<div><div>${if (isDir) "[DIR] " else ""}${escapeHtml(file.name)}</div><div class='meta'>$sizeText</div></div>")
                if (isDir) {
                    sb.append("<a class='btn' href='/browse?path=$encoded'>Open</a>")
                } else {
                    sb.append("<a class='btn' href='/download?path=$encoded'>Download</a>")
                }
                sb.append("</li>")
            }
        }
        sb.append("</ul></div></body></html>")

        val htmlBytes = sb.toString().toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html; charset=utf-8\r\n" +
            "Content-Length: ${htmlBytes.size}\r\n" +
            "X-Content-Type-Options: nosniff\r\n" +
            "Connection: close\r\n\r\n"

        output.write(header.toByteArray(Charsets.UTF_8))
        output.write(htmlBytes)
        output.flush()
    }

    private fun handleDownload(uri: String, output: OutputStream) {
        val queryIndex = uri.indexOf("path=")
        if (queryIndex == -1) {
            send404(output)
            return
        }

        val filePath = uri.substring(queryIndex + 5).substringBefore('&')
        val file = resolveSafeTarget(filePath)
        if (file == null || !file.exists() || file.isDirectory || !file.canRead()) {
            send403(output)
            return
        }

        val safeDownloadName = file.name.replace(Regex("[^a-zA-Z0-9._ -]"), "_")
        val header = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: application/octet-stream\r\n" +
            "Content-Length: ${file.length()}\r\n" +
            "Content-Disposition: attachment; filename=\"$safeDownloadName\"\r\n" +
            "X-Content-Type-Options: nosniff\r\n" +
            "Connection: close\r\n\r\n"

        output.write(header.toByteArray(Charsets.UTF_8))

        FileInputStream(file).use { fis ->
            val buffer = ByteArray(64 * 1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            output.flush()
        }
    }

    private fun send403(output: OutputStream) {
        val msg = "HTTP/1.1 403 Forbidden\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
        output.write(msg.toByteArray(Charsets.UTF_8))
        output.flush()
    }

    private fun send404(output: OutputStream) {
        val msg = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
        output.write(msg.toByteArray(Charsets.UTF_8))
        output.flush()
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val host = addr.hostAddress ?: continue
                        if (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172.")) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore network interface query failure
        }
        return null
    }
}
