package com.example.data.server

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.net.URLEncoder

class LocalWebServer(
    private val context: Context,
    private val rootPathProvider: () -> String
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val serverScope = CoroutineScope(Dispatchers.IO)

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow("Offline")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount: StateFlow<Int> = _connectedClientsCount.asStateFlow()

    companion object {
        const val PORT = 8080
    }

    fun startServer(): String {
        if (_isRunning.value) return _serverUrl.value

        val ip = getLocalIpAddress() ?: "127.0.0.1"
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
            e.printStackTrace()
            _isRunning.value = false
            _serverUrl.value = "Failed to bind port $PORT"
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
            e.printStackTrace()
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.use { s ->
                val input = s.getInputStream()
                val output = s.getOutputStream()
                val reader = BufferedReader(InputStreamReader(input))

                val requestLine = reader.readLine() ?: return
                val tokens = requestLine.split(" ")
                if (tokens.size < 2) return

                val method = tokens[0]
                val fullUri = tokens[1]
                val uri = URLDecoder.decode(fullUri, "UTF-8")

                if (uri.startsWith("/download")) {
                    handleDownload(uri, output)
                } else {
                    handleWebUi(output)
                }
            }
        } catch (e: Exception) {
            // Socket closed or client disconnected
        }
    }

    private fun handleWebUi(output: OutputStream) {
        val rootDir = File(rootPathProvider())
        val files = rootDir.listFiles() ?: emptyArray()

        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'>")
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
        sb.append("<title>Liquid Files - Wi-Fi Share</title>")
        sb.append("<style>")
        sb.append("body { background: #07090e; color: #f0f4fc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; padding: 24px; margin: 0; }")
        sb.append(".container { max-width: 800px; margin: 0 auto; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 20px; padding: 24px; box-shadow: 0 8px 32px rgba(0,0,0,0.5); }")
        sb.append("h1 { color: #00E5FF; font-size: 22px; margin-top: 0; display: flex; align-items: center; gap: 8px; }")
        sb.append(".badge { background: rgba(0,229,255,0.15); color: #00E5FF; padding: 4px 10px; border-radius: 20px; font-size: 12px; }")
        sb.append("ul { list-style: none; padding: 0; margin: 20px 0 0 0; }")
        sb.append("li { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid rgba(255,255,255,0.06); }")
        sb.append("li:last-child { border-bottom: none; }")
        sb.append(".name { font-weight: 500; font-size: 14px; }")
        sb.append(".meta { color: #8e99b0; font-size: 12px; }")
        sb.append("a.btn { background: #00E5FF; color: #07090e; font-weight: bold; text-decoration: none; padding: 6px 14px; border-radius: 8px; font-size: 12px; }")
        sb.append("</style></head><body>")
        sb.append("<div class='container'>")
        sb.append("<h1>Liquid Files <span class='badge'>Wi-Fi File Server</span></h1>")
        sb.append("<p style='color: #8e99b0; font-size: 13px;'>Browse and download files directly from your mobile device.</p>")
        sb.append("<ul>")

        if (files.isEmpty()) {
            sb.append("<li style='justify-content: center; color: #8e99b0;'>No files found in shared directory.</li>")
        } else {
            files.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }).forEach { file ->
                val isDir = file.isDirectory
                val sizeText = if (isDir) "Folder" else "${file.length() / 1024} KB"
                sb.append("<li>")
                sb.append("<div><div class='name'>${if (isDir) "📁 " else "📄 "}${escapeHtml(file.name)}</div>")
                sb.append("<div class='meta'>$sizeText</div></div>")
                if (!isDir) {
                    val encoded = URLEncoder.encode(file.absolutePath, "UTF-8")
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

        val filePath = uri.substring(queryIndex + 5)
        val file = File(filePath)

        if (!file.exists() || file.isDirectory || !file.canRead()) {
            send404(output)
            return
        }

        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Length: ${file.length()}\r\n" +
                "Content-Disposition: attachment; filename=\"${file.name}\"\r\n" +
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
                        val host = addr.hostAddress
                        if (host != null && host.startsWith("192.168.") || host?.startsWith("10.") == true || host?.startsWith("172.") == true) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
