package com.example.ui.viewers

import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.data.model.FileItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerDialog(
    file: FileItem,
    imageList: List<FileItem> = listOf(file),
    onDismiss: () -> Unit,
    onShare: (FileItem) -> Unit
) {
    val context = LocalContext.current
    val safeList = remember(file, imageList) {
        if (imageList.isEmpty()) listOf(file)
        else if (imageList.any { it.path == file.path }) imageList
        else listOf(file) + imageList
    }
    val initialIndex = remember(file, safeList) {
        safeList.indexOfFirst { it.path == file.path }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { safeList.size })
    val currentFile = safeList.getOrElse(pagerState.currentPage) { file }

    var showOverlay by remember { mutableStateOf(true) }
    var showInfo by remember { mutableStateOf(false) }

    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { pageIndex ->
                val pageFile = safeList[pageIndex]
                ZoomableImagePage(
                    file = pageFile,
                    imageLoader = gifEnabledLoader,
                    onSingleTap = { showOverlay = !showOverlay }
                )
            }

            // Top Toolbar
            AnimatedVisibility(
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x9907090E))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x30FFFFFF))
                            .testTag("btn_close_image_viewer")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextPrimary)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = currentFile.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        if (safeList.size > 1) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${safeList.size} • ${currentFile.formattedSize}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showInfo = !showInfo },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x30FFFFFF))
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = "Image Info", tint = NeonCyan)
                        }
                        if (!currentFile.isProtected) {
                            IconButton(
                                onClick = { onShare(currentFile) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x30FFFFFF))
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Info Sheet Overlay
            if (showInfo) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    cornerRadius = 22.dp,
                    backgroundColor = Color(0xF0101626)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Image Details", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Name: ${currentFile.name}", color = TextSecondary, fontSize = 11.sp)
                        Text("Size: ${currentFile.formattedSize}", color = TextSecondary, fontSize = 11.sp)
                        Text("Modified: ${currentFile.formattedDate}", color = TextSecondary, fontSize = 11.sp)
                        Text("Format: ${currentFile.extension.uppercase().ifBlank { "Image" }}", color = TextSecondary, fontSize = 11.sp)
                        Text("Pinch or double-tap to zoom • Swipe left/right for next", color = NeonCyan, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableImagePage(
    file: FileItem,
    imageLoader: ImageLoader,
    onSingleTap: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember(file.path) { mutableFloatStateOf(1f) }
    var offset by remember(file.path) { mutableStateOf(Offset.Zero) }

    val modelData: Any = remember(file.path) {
        if (file.path.startsWith("content://") || file.path.startsWith("file://")) {
            Uri.parse(file.path)
        } else {
            File(file.path)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(file.path) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.2f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    },
                    onTap = { onSingleTap() }
                )
            }
            .pointerInput(file.path) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    if (newScale > 1.01f) {
                        val maxX = (size.width * (newScale - 1f)) / 2f
                        val maxY = (size.height * (newScale - 1f)) / 2f
                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                            y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                        )
                    } else {
                        offset = Offset.Zero
                    }
                    scale = newScale
                }
            },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(modelData)
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            contentDescription = file.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            },
            error = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BrokenImage,
                        contentDescription = "Unsupported image",
                        tint = SunsetPink,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Unable to load image", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${file.name} (${file.formattedSize}) may be corrupted or permission was denied.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        )
    }
}
