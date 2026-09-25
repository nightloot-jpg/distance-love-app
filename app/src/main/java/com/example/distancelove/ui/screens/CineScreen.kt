package com.example.distancelove.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.example.distancelove.data.CinemaFloater
import com.example.distancelove.data.CinemaVideo
import com.example.distancelove.ui.components.GlassCard
import com.example.distancelove.ui.components.RoseGradientButton
import com.example.distancelove.ui.components.ScreenHeader
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel
import kotlinx.coroutines.delay

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

    var customUrlInput by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }

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
                title = "Sala de Cine",
                subtitle = "En directo con Yuki"
            )
        }

        // Main Video Player Box with Floating Reactions & Badge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(DarkSurfaceElevated)
                    .testTag("cinema_video_container")
            ) {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.setVideoPlaying(!isPlaying) }
                )

                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DarkBackground.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Text(
                            text = "Sincronizado · 2 viendo",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    floaters.forEach { floater ->
                        FloatingEmojiItem(floater = floater)
                    }
                }
            }
        }

        // Controls
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cinema_controls_bar"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(RoseGradient)
                            .clickable { viewModel.setVideoPlaying(!isPlaying) }
                            .testTag("cinema_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = DarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = formatDuration(videoPos),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Slider(
                        value = if (videoDur > 0) videoPos.toFloat() / videoDur else 0f,
                        onValueChange = { frac ->
                            val target = (frac * videoDur).toLong()
                            exoPlayer.seekTo(target)
                            viewModel.updateVideoProgress(target, videoDur)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = RosePrimary,
                            activeTrackColor = RosePrimary,
                            inactiveTrackColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cinema_seek_slider")
                    )

                    Text(
                        text = formatDuration(videoDur),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Reaction Bar + Mic
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassCard(
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("❤️", "🍿", "😂", "😭", "😍", "🔥").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { viewModel.addCinemaReaction(emoji) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("reaction_$emoji")
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .then(
                            if (isMicActive) Modifier.background(RoseGradient)
                            else Modifier.background(DarkSurfaceElevated)
                        )
                        .clickable { viewModel.toggleMic() }
                        .testTag("voice_channel_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMicActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Micrófono",
                        tint = if (isMicActive) DarkBackground else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        if (isMicActive) {
            item {
                Text(
                    text = "🎙️ Canal de voz abierto con Yuki",
                    style = MaterialTheme.typography.labelSmall,
                    color = RosePrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }

        // Library & Custom Link
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cinema_library_card"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.defaultVideos.forEach { video ->
                            val isSelected = currentVideo.url == video.url
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .then(
                                        if (isSelected) Modifier.background(RoseGradient)
                                        else Modifier.background(DarkSurfaceElevated)
                                    )
                                    .clickable { viewModel.selectVideo(video) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("select_video_${video.title.lowercase().replace(" ", "_")}")
                            ) {
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) DarkBackground else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = customUrlInput,
                            onValueChange = { customUrlInput = it },
                            placeholder = { Text("Enlace de vídeo (.mp4)", style = MaterialTheme.typography.bodyMedium) },
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_video_input")
                        )

                        RoseGradientButton(
                            onClick = {
                                if (customUrlInput.isNotBlank()) {
                                    viewModel.selectVideo(CinemaVideo("Vídeo personalizado", customUrlInput.trim()))
                                    customUrlInput = ""
                                }
                            },
                            enabled = customUrlInput.isNotBlank(),
                            modifier = Modifier.testTag("load_custom_video_button")
                        ) {
                            Text(
                                text = "Cargar",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Cinema Room Chat
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cinema_chat_card"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chat de sala",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chatMessages.forEach { chatMsg ->
                            val isMe = chatMsg.by == "Tú"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(if (isMe) Alignment.End else Alignment.Start)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .then(
                                            if (isMe) Modifier.background(RoseGradient)
                                            else Modifier.background(DarkSurfaceElevated)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = chatMsg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMe) DarkBackground else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Escribe…", style = MaterialTheme.typography.bodyMedium) },
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cinema_chat_input")
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(RoseGradient)
                                .clickable {
                                    if (chatInput.isNotBlank()) {
                                        viewModel.sendCinemaMessage(chatInput)
                                        chatInput = ""
                                    }
                                }
                                .testTag("send_cinema_chat_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Enviar",
                                tint = DarkBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingEmojiItem(floater: CinemaFloater) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(floater.id) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        val yOffset = (1f - animProgress.value) * 180f

        Text(
            text = floater.emoji,
            fontSize = 28.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(
                    x = (floater.xPercent * 2.5f).dp,
                    y = (-yOffset).dp
                )
        )
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
