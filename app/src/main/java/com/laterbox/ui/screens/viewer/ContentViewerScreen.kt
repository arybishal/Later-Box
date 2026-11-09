package com.laterbox.ui.screens.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.ui.components.openInExternalApp
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.launch
import java.io.File

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentViewerScreen(
    item: SavedItemEntity,
    onBack: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFavorite by remember(item.id, item.isFavorite) { mutableStateOf(item.isFavorite) }

    // Record last opened timestamp immediately
    LaunchedEffect(item.id) {
        LaterBoxApp.instance.contentRepository.updateLastOpened(item.id)
    }

    val onProgressUpdate: (Float) -> Unit = { progress ->
        scope.launch {
            LaterBoxApp.instance.contentRepository.updateReadProgress(item.id, progress)
        }
    }

    val isSocialPlatform = item.sourceDomain?.let { d ->
        d.contains("instagram.com") || d.contains("tiktok.com") || d.contains("twitter.com") || d.contains("x.com")
    } ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.sourceDomain ?: item.url,
                            style = MaterialTheme.typography.labelSmall,
                            color = extended.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val newFav = !isFavorite
                        isFavorite = newFav
                        scope.launch {
                            LaterBoxApp.instance.contentRepository.toggleFavorite(item.id, !newFav)
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from Favorites" else "Mark as Favorite",
                            tint = if (isFavorite) extended.amberAccent else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { openInExternalApp(context, item.url) }) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open in Original App",
                            tint = extended.amberAccent
                        )
                    }
                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${item.title}\n${item.url}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Notice banner for social sites that prefer native app playback
            if (isSocialPlatform) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(extended.amberAccentSubtle)
                        .clickable { openInExternalApp(context, item.url) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = extended.amberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "For full interactive comments and video, tap to open in the app.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = extended.amberAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Local or streamable video
                    item.contentType == ContentType.VIDEO && (item.localMediaPath != null || item.url.endsWith(".mp4")) -> {
                        VideoPlayerView(
                            mediaUri = item.localMediaPath ?: item.url,
                            initialProgress = item.readProgress,
                            onProgressUpdate = onProgressUpdate
                        )
                    }
                    // Image view
                    item.contentType == ContentType.IMAGE -> {
                        ImageViewerView(
                            model = item.localMediaPath?.let { File(it) } ?: item.url
                        )
                    }
                    // In-app web reader
                    else -> {
                        InAppWebReader(
                            url = item.url,
                            initialProgress = item.readProgress,
                            onProgressUpdate = onProgressUpdate
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppWebReader(
    url: String,
    initialProgress: Float,
    onProgressUpdate: (Float) -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var hasRestoredScroll by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading && progress < 1f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = extended.amberAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress / 100f
                            if (newProgress == 100) isLoading = false
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            if (!hasRestoredScroll && initialProgress > 0f) {
                                postDelayed({
                                    val maxScroll = (contentHeight * scale - height).toInt()
                                    if (maxScroll > 0) {
                                        scrollTo(0, (initialProgress * maxScroll).toInt())
                                    }
                                    hasRestoredScroll = true
                                }, 300)
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return false
                        }
                    }

                    setOnScrollChangeListener { _, _, scrollY, _, _ ->
                        val maxScroll = (contentHeight * scale - height).toInt()
                        if (maxScroll > 0) {
                            val currentProgress = (scrollY.toFloat() / maxScroll).coerceIn(0f, 1f)
                            onProgressUpdate(currentProgress)
                        }
                    }

                    loadUrl(url)
                    webViewInstance = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.stopLoading()
            webViewInstance?.destroy()
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    mediaUri: String,
    initialProgress: Float,
    onProgressUpdate: (Float) -> Unit
) {
    val context = LocalContext.current
    var hasRestoredSeek by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = if (mediaUri.startsWith("http")) Uri.parse(mediaUri) else Uri.fromFile(File(mediaUri))
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY && !hasRestoredSeek && initialProgress > 0f) {
                        val duration = this@apply.duration
                        if (duration > 0) {
                            seekTo((initialProgress * duration).toLong())
                        }
                        hasRestoredSeek = true
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val duration = exoPlayer.duration
            val position = exoPlayer.currentPosition
            if (duration > 0) {
                val progress = (position.toFloat() / duration).coerceIn(0f, 1f)
                onProgressUpdate(progress)
            }
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ImageViewerView(model: Any) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
