package com.example.distancelove.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.distancelove.data.ChatMessage
import com.example.distancelove.data.CinemaFloater
import com.example.distancelove.data.CinemaVideo
import com.example.distancelove.ui.components.GlassCard
import com.example.distancelove.ui.components.RoseGradientButton
import com.example.distancelove.ui.components.ScreenHeader
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel
import kotlinx.coroutines.delay

enum class StreamingPlatform(
    val title: String,
    val initialUrl: String,
    val iconEmoji: String,
    val tagColor: Color,
    val isNativePlayer: Boolean
) {
    VIDEOS_DIRECTOS("Videos / Enlaces", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", "🍿", RosePrimary, true),
    YOUTUBE("YouTube", "https://m.youtube.com", "🔴", Color(0xFFFF0000), false),
    NETFLIX("Netflix", "https://www.netflix.com", "🎬", Color(0xFFE50914), false),
    PRIME_VIDEO("Prime Video", "https://www.primevideo.com", "📦", Color(0xFF00A8E1), false),
    DISNEY_PLUS("Disney+", "https://www.disneyplus.com", "✨", Color(0xFF113CCF), false)
}

@OptIn(UnstableApi::class)
@Composable
fun CineScreen(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentVideo by viewModel.currentVideo.collectAsState()
    val isPlaying by viewModel.isVideoPlaying.collectAsState()
    val videoPos by viewModel.videoPositionMs.collectAsState()
    val videoDur by viewModel.videoDurationMs.collectAsState()
    val floaters by viewModel.cinemaFloaters.collectAsState()
    val isMicActive by viewModel.isMicActive.collectAsState()
    val chatMessages by viewModel.cinemaChat.collectAsState()
    val currentUser by viewModel.currentUserProfile.collectAsState()
    val partner by viewModel.partnerProfile.collectAsState()
    val couple by viewModel.coupleInfo.collectAsState()

    var selectedPlatform by remember { mutableStateOf(StreamingPlatform.VIDEOS_DIRECTOS) }
    var currentWebUrl by remember { mutableStateOf(selectedPlatform.initialUrl) }
    var customUrlInput by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showSyncFlash by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(currentVideo) {
        val mediaItem = MediaItem.fromUri(Uri.parse(currentVideo.url))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        if (isPlaying) {
            exoPlayer.play()
        }
        onDispose {
            exoPlayer.stop()
        }
    }

    DisposableEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
        onDispose {}
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val current = exoPlayer.currentPosition
            val duration = exoPlayer.duration.coerceAtLeast(0L)
            viewModel.updateVideoProgress(current, duration)
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Cine Juntos",
                subtitle = "Watch Party en sincronía"
            )
        }

        // Rave Sync Room Status Header
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rave_sync_status_card"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Text(
                            text = if (partner != null) "Sincronizado con ${partner?.fullName}" else "Sala lista para invitar pareja",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(RosePrimary.copy(alpha = 0.2f))
                            .border(1.dp, RosePrimary, RoundedCornerShape(50))
                            .clickable {
                                showSyncFlash = true
                                if (selectedPlatform.isNativePlayer) {
                                    exoPlayer.seekTo(videoPos)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = "Sincronizar",
                                tint = RosePrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Sincronizar",
                                style = MaterialTheme.typography.labelSmall,
                                color = RosePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Platform Hub (Netflix, Prime, Disney+, YouTube, Direct)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PLATAFORMAS DISPONIBLES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StreamingPlatform.entries.forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        GlassCard(
                            modifier = Modifier
                                .clickable {
                                    selectedPlatform = platform
                                    currentWebUrl = platform.initialUrl
                                    if (platform.isNativePlayer) {
                                        viewModel.setVideoPlaying(false)
                                    }
                                }
                                .testTag("platform_${platform.name.lowercase()}"),
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = if (isSelected) platform.tagColor.copy(alpha = 0.25f) else DarkSurfaceElevated,
                            borderColor = if (isSelected) platform.tagColor else DarkCardBorder
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = platform.iconEmoji, fontSize = 16.sp)
                                Text(
                                    text = platform.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) TextPrimary else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Video Player Display (Native Player or Integrated Rave WebView)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .testTag("cinema_main_player"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (selectedPlatform.isNativePlayer) {
                        // Native ExoPlayer
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // In-App Web View for Netflix, Disney+, Prime Video, YouTube Web
                        RaveWebPlayer(
                            url = currentWebUrl,
                            onWebViewCreated = { webViewRef = it }
                        )
                    }

                    // Floating emojis layer
                    floaters.forEach { floater ->
                        FloaterEmojiView(floater = floater)
                    }

                    // Sync Flash Overlay
                    if (showSyncFlash) {
                        LaunchedEffect(Unit) {
                            delay(1200)
                            showSyncFlash = false
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(RoseGradient)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = DarkBackground)
                                Text(text = "¡Reproducción en sincronía exacta!", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Media Controls & Timeline (For direct videos / shared rooms)
        if (selectedPlatform.isNativePlayer) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentVideo.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "${formatMs(videoPos)} / ${formatMs(videoDur)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        // Progress Slider
                        Slider(
                            value = if (videoDur > 0) videoPos.toFloat() / videoDur else 0f,
                            onValueChange = { percent ->
                                val target = (percent * videoDur).toLong()
                                exoPlayer.seekTo(target)
                                viewModel.updateVideoProgress(target, videoDur)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = RosePrimary,
                                activeTrackColor = RosePrimary,
                                inactiveTrackColor = DarkSurfaceElevated
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Action Play/Pause & Voice Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { exoPlayer.seekTo((videoPos - 10000).coerceAtLeast(0L)) }) {
                                Icon(imageVector = Icons.Filled.Replay10, contentDescription = "-10s", tint = TextPrimary)
                            }

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(RoseGradient)
                                    .clickable {
                                        viewModel.setVideoPlaying(!isPlaying)
                                    }
                                    .testTag("cinema_play_pause_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play/Pausa",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(onClick = { exoPlayer.seekTo((videoPos + 10000).coerceAtMost(videoDur)) }) {
                                Icon(imageVector = Icons.Filled.Forward10, contentDescription = "+10s", tint = TextPrimary)
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isMicActive) SuccessGreen else DarkSurfaceElevated)
                                    .clickable { viewModel.toggleMic() }
                                    .testTag("cinema_mic_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMicActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                                    contentDescription = "Micrófono",
                                    tint = if (isMicActive) DarkBackground else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Shared Emoji Reactions Tray (Rave Style Floating Reactions)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("❤️", "🍿", "😭", "😂", "🔥", "🫂", "✨", "💋").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkCardBorder, CircleShape)
                            .clickable { viewModel.addCinemaReaction(emoji) }
                            .padding(6.dp)
                            .testTag("reaction_$emoji"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }
        }

        // Custom URL or Search Bar
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        placeholder = { Text("Pegar enlace de video o sala…", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = RosePrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    RoseGradientButton(
                        onClick = {
                            if (customUrlInput.isNotBlank()) {
                                if (customUrlInput.startsWith("http")) {
                                    if (customUrlInput.endsWith(".mp4") || customUrlInput.contains("googlevideo") || customUrlInput.contains("storage")) {
                                        selectedPlatform = StreamingPlatform.VIDEOS_DIRECTOS
                                        viewModel.selectVideo(CinemaVideo("Video Compartido", customUrlInput))
                                    } else {
                                        currentWebUrl = customUrlInput
                                    }
                                }
                                customUrlInput = ""
                            }
                        },
                        enabled = customUrlInput.isNotBlank()
                    ) {
                        Text("Cargar", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Synced Cinema Chat
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cinema_chat_card"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chat de la Sala",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chatMessages.takeLast(6).forEach { msg ->
                            val isMe = msg.by == currentUser?.fullName || msg.by == "Tú"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isMe) RosePrimary else DarkSurfaceElevated)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${msg.by}: ${msg.text}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMe) DarkBackground else TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Comentar escena…", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(50),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = RosePrimary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (chatInput.isNotBlank()) {
                                        viewModel.sendCinemaMessage(chatInput)
                                        chatInput = ""
                                    }
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    viewModel.sendCinemaMessage(chatInput)
                                    chatInput = ""
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Send, contentDescription = "Enviar", tint = RosePrimary)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun RaveWebPlayer(
    url: String,
    onWebViewCreated: (WebView) -> Unit
) {
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
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    cacheMode = WebSettings.LOAD_DEFAULT
                }

                val webView = this
                CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(webView, true)
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                    }
                }

                loadUrl(url)
                onWebViewCreated(this)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun FloaterEmojiView(floater: CinemaFloater) {
    val infiniteTransition = rememberInfiniteTransition(label = "FloaterAnim")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetY"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = (floater.xPercent * 3).dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = floater.emoji,
            fontSize = 28.sp,
            modifier = Modifier
                .offset(y = offsetY.dp)
                .padding(bottom = 20.dp)
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
