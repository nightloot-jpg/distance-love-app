package com.example.distancelove.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.distancelove.data.ChatMessage
import com.example.distancelove.data.CinemaFloater
import com.example.distancelove.ui.components.GlassCard
import com.example.distancelove.ui.components.RoseGradientButton
import com.example.distancelove.ui.components.ScreenHeader
import com.example.distancelove.ui.components.SmartImage
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StreamingPlatform(
    val title: String,
    val initialUrl: String,
    val loginUrl: String,
    val iconEmoji: String,
    val tagColor: Color,
    val secondaryColor: Color,
    val subtitle: String,
    val description: String
) {
    NETFLIX(
        title = "Netflix",
        initialUrl = "https://www.netflix.com/browse",
        loginUrl = "https://www.netflix.com/login",
        iconEmoji = "🎬",
        tagColor = Color(0xFFE50914),
        secondaryColor = Color(0xFF831010),
        subtitle = "Películas, series y documentales",
        description = "Inicia sesión con tu cuenta de Netflix para ver contenido juntos"
    ),
    PRIME_VIDEO(
        title = "Prime Video",
        initialUrl = "https://www.primevideo.com",
        loginUrl = "https://www.primevideo.com/auth/login",
        iconEmoji = "📦",
        tagColor = Color(0xFF00A8E1),
        secondaryColor = Color(0xFF005A7A),
        subtitle = "Amazon Prime Video",
        description = "Disfruta de estrenos exclusivos y producciones Amazon Originals"
    ),
    DISNEY_PLUS(
        title = "Disney+",
        initialUrl = "https://www.disneyplus.com",
        loginUrl = "https://www.disneyplus.com/login",
        iconEmoji = "✨",
        tagColor = Color(0xFF113CCF),
        secondaryColor = Color(0xFF0B2175),
        subtitle = "Disney, Marvel, Pixar y Star Wars",
        description = "Tus películas animadas, sagas y universos favoritos juntos"
    ),
    MAX(
        title = "Max / HBO",
        initialUrl = "https://www.max.com",
        loginUrl = "https://auth.max.com/login",
        iconEmoji = "📺",
        tagColor = Color(0xFF002BE7),
        secondaryColor = Color(0xFF001570),
        subtitle = "HBO, Warner Bros y DC",
        description = "Series icónicas, estrenos de cine y franquicias legendarias"
    ),
    YOUTUBE(
        title = "YouTube",
        initialUrl = "https://m.youtube.com",
        loginUrl = "https://accounts.google.com/ServiceLogin?service=youtube",
        iconEmoji = "🔴",
        tagColor = Color(0xFFFF0000),
        secondaryColor = Color(0xFF8B0000),
        subtitle = "Videos, directos y música",
        description = "Accede a tu cuenta de Google para listas de reproducción y directos"
    ),
    TWITCH(
        title = "Twitch",
        initialUrl = "https://m.twitch.tv",
        loginUrl = "https://www.twitch.tv/login",
        iconEmoji = "🟣",
        tagColor = Color(0xFF9146FF),
        secondaryColor = Color(0xFF53249E),
        subtitle = "Streams en vivo y gaming",
        description = "Sintoniza a tus streamers favoritos y chatea en tiempo real"
    ),
    CRUNCHYROLL(
        title = "Crunchyroll",
        initialUrl = "https://www.crunchyroll.com",
        loginUrl = "https://www.crunchyroll.com/login",
        iconEmoji = "🍙",
        tagColor = Color(0xFFFF6400),
        secondaryColor = Color(0xFF8C3600),
        subtitle = "Anime simulcast y mangas",
        description = "El catálogo más grande de anime en emisión directa de Japón"
    )
}

enum class CinemaPickerTab(val title: String, val icon: String) {
    EMOJIS("Emojis y Reacciones", "😊"),
    GIFS("GIFs", "🎬")
}

data class CinemaGifItem(
    val title: String,
    val category: String,
    val gifUrl: String
)

val CINEMA_GIFS = listOf(
    // Romance & Besos
    CinemaGifItem("Beso Tierno", "Romance", "https://media.giphy.com/media/G3va31oEEnIkM/giphy.gif"),
    CinemaGifItem("Abrazo de Amor", "Romance", "https://media.giphy.com/media/l2QDM9Jnim1YV5998C/giphy.gif"),
    CinemaGifItem("Te Amo Mucho", "Romance", "https://media.giphy.com/media/26FLdmIp6wJr91JAI/giphy.gif"),
    CinemaGifItem("Mimos & Cuddle", "Romance", "https://media.giphy.com/media/3o7TKoWXm3okO1kgHC/giphy.gif"),
    
    // Cine & Palomitas
    CinemaGifItem("Comiendo Palomitas", "Cine", "https://media.giphy.com/media/GLbiGvv9qrpny/giphy.gif"),
    CinemaGifItem("Suspenso Total", "Cine", "https://media.giphy.com/media/5GoVLqeAOo6PK/giphy.gif"),
    CinemaGifItem("Atento a la Pantalla", "Cine", "https://media.giphy.com/media/13cptIwW9bgzk6UVfr/giphy.gif"),
    CinemaGifItem("Emoción de Película", "Cine", "https://media.giphy.com/media/artj92V8o75VPL7AeQ/giphy.gif"),
    
    // Risas & Diversión
    CinemaGifItem("Risa Incontrolable", "Risas", "https://media.giphy.com/media/10JhviFuU2gWD6/giphy.gif"),
    CinemaGifItem("Celebración & Baile", "Risas", "https://media.giphy.com/media/blSTtZehjAZ8I/giphy.gif"),
    CinemaGifItem("Aplausos Fuertes", "Risas", "https://media.giphy.com/media/nbvFVPiEiJH6h41zg5/giphy.gif"),
    CinemaGifItem("Meme Épico", "Risas", "https://media.giphy.com/media/3oEjHAUOqG3lSS0f1C/giphy.gif"),

    // Drama & Ternura
    CinemaGifItem("Lágrimas de Emoción", "Drama", "https://media.giphy.com/media/d2lcHJTG5Tscg/giphy.gif"),
    CinemaGifItem("Corazón Roto", "Drama", "https://media.giphy.com/media/OPU6wzx8JrHna/giphy.gif"),
    CinemaGifItem("Abrazo de Apoyo", "Drama", "https://media.giphy.com/media/PHZ7v9tfQu0o0/giphy.gif"),
    CinemaGifItem("Me Dejó en Shock", "Drama", "https://media.giphy.com/media/l3q2K5jinAlChoCLS/giphy.gif")
)

@Composable
fun CineScreen(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val floaters by viewModel.cinemaFloaters.collectAsState()
    val isMicActive by viewModel.isMicActive.collectAsState()
    val chatMessages by viewModel.cinemaChat.collectAsState()
    val currentUser by viewModel.currentUserProfile.collectAsState()
    val partner by viewModel.partnerProfile.collectAsState()
    val isPlaying by viewModel.isVideoPlaying.collectAsState()

    var selectedPlatform by remember { mutableStateOf(StreamingPlatform.NETFLIX) }
    var isPlatformMenuExpanded by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    var showSyncFlash by remember { mutableStateOf(false) }

    // Modals state
    var isFullScreenOpen by remember { mutableStateOf(false) }
    var fullScreenTargetUrl by remember { mutableStateOf(selectedPlatform.loginUrl) }
    var isMediaPickerOpen by remember { mutableStateOf(false) }
    var mediaPickerInitialTab by remember { mutableStateOf(CinemaPickerTab.EMOJIS) }

    // Moment Photo state
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPhotoMomentDialog by remember { mutableStateOf(false) }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            showPhotoMomentDialog = true
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhotoLauncher.launch(null)
        } else {
            Toast.makeText(context, "Se necesita permiso de cámara para hacer la foto del momento", Toast.LENGTH_SHORT).show()
        }
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isPlatformMenuExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "chevronRotation"
    )

    // Fullscreen in-app Dialog/Window for logging in or watching full screen
    if (isFullScreenOpen) {
        FullScreenStreamingDialog(
            platform = selectedPlatform,
            initialUrl = fullScreenTargetUrl,
            isPlaying = isPlaying,
            onTogglePlayPause = {
                viewModel.leaderSetPlaying(!isPlaying)
            },
            onOpenMediaPicker = { tab ->
                mediaPickerInitialTab = tab
                isMediaPickerOpen = true
            },
            onDismiss = {
                isFullScreenOpen = false
            },
            onSendReaction = { emoji ->
                viewModel.addCinemaReaction(emoji)
            }
        )
    }

    // Unified Media & Reactions Picker Modal (Emojis & GIFs in tabs)
    if (isMediaPickerOpen) {
        CinemaMediaPickerDialog(
            initialTab = mediaPickerInitialTab,
            onDismiss = { isMediaPickerOpen = false },
            onSelectEmoji = { emoji ->
                viewModel.addCinemaReaction(emoji)
            },
            onSelectGif = { gifUrl, title ->
                viewModel.sendCinemaGif(gifUrl = gifUrl, caption = "🎬 $title")
                isMediaPickerOpen = false
            }
        )
    }

    // Captured Moment Polaroid Dialog
    if (showPhotoMomentDialog && capturedBitmap != null) {
        CinemaMomentPhotoDialog(
            bitmap = capturedBitmap!!,
            partnerName = partner?.fullName ?: "Pareja",
            onDismiss = {
                showPhotoMomentDialog = false
                capturedBitmap = null
            },
            onSendToChat = {
                capturedBitmap?.let { bmp ->
                    val path = saveBitmapToInternalCache(context, bmp)
                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                    viewModel.sendCinemaPhotoMoment(imageUri = path, caption = "📸 Noche de Cine ($dateStr)")
                    Toast.makeText(context, "¡Foto del momento enviada al chat!", Toast.LENGTH_SHORT).show()
                }
                showPhotoMomentDialog = false
                capturedBitmap = null
            }
        )
    }

    val chatListState = rememberLazyListState()

    // Auto-scroll chat to latest message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Header Row with Platform Selector Trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cine Juntos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Watch Party en pareja",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Dropdown trigger button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, selectedPlatform.tagColor.copy(alpha = 0.7f), RoundedCornerShape(50))
                    .clickable { isPlatformMenuExpanded = !isPlatformMenuExpanded }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("streaming_platform_dropdown_card"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = selectedPlatform.iconEmoji, fontSize = 14.sp)
                    Text(
                        text = selectedPlatform.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = selectedPlatform.tagColor,
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Cambiar plataforma",
                        tint = selectedPlatform.tagColor,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(chevronRotation)
                    )
                }
            }
        }

        // Expandable Platform Selector Menu
        AnimatedVisibility(
            visible = isPlatformMenuExpanded,
            enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(250)),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                borderColor = selectedPlatform.tagColor.copy(alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SELECCIONA PLATAFORMA DE STREAMING",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        letterSpacing = 1.sp,
                        fontSize = 9.sp
                    )

                    val rows = StreamingPlatform.entries.chunked(2)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { platform ->
                                val isSelected = selectedPlatform == platform
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) platform.tagColor.copy(alpha = 0.22f)
                                            else DarkSurfaceElevated
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) platform.tagColor else DarkCardBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedPlatform = platform
                                            isPlatformMenuExpanded = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 7.dp)
                                        .testTag("platform_${platform.name.lowercase()}"),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = platform.iconEmoji, fontSize = 16.sp)
                                        Text(
                                            text = platform.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) TextPrimary else TextPrimary.copy(alpha = 0.85f),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Seleccionado",
                                                tint = platform.tagColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 2. FIXED PLAYER CARD (STAYS AT THE TOP OF THE SCREEN ALWAYS)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cinema_main_player"),
            shape = RoundedCornerShape(20.dp),
            borderColor = selectedPlatform.tagColor.copy(alpha = 0.5f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                selectedPlatform.tagColor.copy(alpha = 0.25f),
                                DarkSurfaceElevated,
                                DarkBackground
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top badges row (Platform badge + Play status indicator & Mic)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(selectedPlatform.tagColor.copy(alpha = 0.25f))
                                    .border(1.dp, selectedPlatform.tagColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = selectedPlatform.iconEmoji, fontSize = 18.sp)
                            }

                            Column {
                                Text(
                                    text = selectedPlatform.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = selectedPlatform.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = selectedPlatform.tagColor,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(DarkBackground.copy(alpha = 0.7f))
                                    .border(1.dp, selectedPlatform.tagColor.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isPlaying) SuccessGreen else RosePrimary)
                                    )
                                    Text(
                                        text = if (isPlaying) "Reproduciendo" else "En Pausa",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Mic status
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isMicActive) SuccessGreen else DarkBackground.copy(alpha = 0.7f))
                                    .border(1.dp, DarkCardBorder, CircleShape)
                                    .clickable { viewModel.toggleMic() }
                                    .testTag("cinema_mic_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMicActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                                    contentDescription = "Micrófono",
                                    tint = if (isMicActive) DarkBackground else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Player Control & In-App Streaming Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play/Pause Leader Control Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.leaderSetPlaying(!isPlaying)
                                }
                                .padding(vertical = 10.dp)
                                .testTag("cinema_leader_play_pause_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled,
                                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isPlaying) "Pausar" else "Reanudar",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Open In-App Streaming Window Button
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(selectedPlatform.tagColor, selectedPlatform.secondaryColor)
                                    )
                                )
                                .clickable {
                                    fullScreenTargetUrl = selectedPlatform.loginUrl
                                    isFullScreenOpen = true
                                }
                                .padding(vertical = 10.dp)
                                .testTag("btn_login_platform_${selectedPlatform.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Key,
                                    contentDescription = "Iniciar Sesión",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Abrir ${selectedPlatform.title}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
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
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(RoseGradient)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                            Text(text = "¡Sincronizado!", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 3. INDEPENDENTLY SCROLLING CHAT CARD (TAKES REMAINING SPACE)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("cinema_chat_card"),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header row of the chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chat de la Sala de Cine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Action to capture photo of the moment directly in chat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, RosePrimary.copy(alpha = 0.6f), RoundedCornerShape(50))
                            .clickable {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    takePhotoLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = RosePrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Foto del Momento",
                                style = MaterialTheme.typography.labelSmall,
                                color = RosePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Chat Message List
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        val isMe = msg.by == currentUser?.fullName || msg.by == "Tú"

                        if (msg.isSystem) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkBackground.copy(alpha = 0.6f))
                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Column(
                                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isMe) RosePrimary else DarkSurfaceElevated)
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = msg.by,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isMe) DarkBackground.copy(alpha = 0.8f) else TextMuted,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )

                                            // GIF content
                                            if (msg.gifUrl != null) {
                                                AsyncImage(
                                                    model = msg.gifUrl,
                                                    contentDescription = "GIF",
                                                    modifier = Modifier
                                                        .widthIn(max = 200.dp)
                                                        .heightIn(max = 140.dp)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            // Photo snapshot content
                                            if (msg.imageBitmapUri != null) {
                                                SmartImage(
                                                    model = msg.imageBitmapUri,
                                                    contentDescription = "Foto",
                                                    modifier = Modifier
                                                        .widthIn(max = 200.dp)
                                                        .heightIn(max = 150.dp)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            if (msg.text.isNotBlank()) {
                                                Text(
                                                    text = msg.text,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isMe) DarkBackground else TextPrimary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Input Composer Row (Fixed at the bottom of the chat card)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Emoji & GIF Picker Button (With Emoji icon 😊)
                    IconButton(
                        onClick = {
                            mediaPickerInitialTab = CinemaPickerTab.EMOJIS
                            isMediaPickerOpen = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .testTag("cinema_emoji_picker_btn")
                    ) {
                        Text(text = "😊", fontSize = 18.sp)
                    }

                    // Quick Camera Button
                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                takePhotoLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .testTag("cinema_quick_camera_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Cámara", tint = RosePrimary, modifier = Modifier.size(18.dp))
                    }

                    TextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Comentar película…", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp) },
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
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(RoseGradient)
                    ) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = "Enviar", tint = DarkBackground, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Unified Picker Dialog featuring tabs for both "Emojis y Reacciones" and "GIFs"
 */
@Composable
fun CinemaMediaPickerDialog(
    initialTab: CinemaPickerTab = CinemaPickerTab.EMOJIS,
    onDismiss: () -> Unit,
    onSelectEmoji: (String) -> Unit,
    onSelectGif: (String, String) -> Unit
) {
    var currentTab by remember { mutableStateOf(initialTab) }

    // Emojis category state
    var selectedEmojiCategory by remember { mutableStateOf("Amor") }
    val emojiCategories = mapOf(
        "Amor" to listOf("❤️", "🥰", "😍", "😘", "💑", "💖", "💍", "👩‍❤️‍💋‍👨", "🥺", "💌", "💓", "💘"),
        "Cine" to listOf("🍿", "🎬", "📺", "🎉", "🥳", "🍕", "🥂", "🍫", "🥤", "🌮", "🍦", "🏆"),
        "Risas" to listOf("😂", "🤣", "😜", "🤪", "🤭", "💀", "🤡", "💃", "🕺", "😆", "😎", "🤩"),
        "Drama" to listOf("😱", "😭", "💔", "🫣", "🤯", "😬", "🤐", "🤫", "😢", "😵‍💫", "😳", "🥀"),
        "Pasión" to listOf("🔥", "✨", "💋", "🫂", "🙌", "💯", "🌶️", "😈", "🫦", "🌹", "⚡", "🔮")
    )

    // GIF category state
    var selectedGifCategory by remember { mutableStateOf("Todos") }
    val gifCategories = listOf("Todos", "Romance", "Cine", "Risas", "Drama")
    val filteredGifs = remember(selectedGifCategory) {
        if (selectedGifCategory == "Todos") CINEMA_GIFS
        else CINEMA_GIFS.filter { it.category == selectedGifCategory }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                backgroundColor = DarkSurface.copy(alpha = 0.94f),
                borderColor = RosePrimary.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reacciones y GIFs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }

                    // Main Tab Switcher: Emojis vs GIFs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CinemaPickerTab.entries.forEach { tab ->
                            val isSelected = currentTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) RosePrimary else Color.Transparent)
                                    .clickable { currentTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = tab.icon, fontSize = 15.sp)
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) DarkBackground else TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Content based on selected Tab
                    when (currentTab) {
                        CinemaPickerTab.EMOJIS -> {
                            // Category chips for Emojis
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                emojiCategories.keys.forEach { cat ->
                                    val isSelected = selectedEmojiCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isSelected) RosePrimary else DarkSurfaceElevated)
                                            .border(1.dp, if (isSelected) RosePrimary else DarkCardBorder, RoundedCornerShape(50))
                                            .clickable { selectedEmojiCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) DarkBackground else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "✨ Toca un emoji para lanzar una reacción en vivo",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )

                            // Emoji Grid
                            val currentEmojis = emojiCategories[selectedEmojiCategory] ?: emptyList()
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.heightIn(max = 240.dp)
                            ) {
                                items(currentEmojis) { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(DarkSurfaceElevated)
                                            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                                            .clickable { onSelectEmoji(emoji) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 28.sp)
                                    }
                                }
                            }
                        }

                        CinemaPickerTab.GIFS -> {
                            // Category chips for GIFs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                gifCategories.forEach { cat ->
                                    val isSelected = selectedGifCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isSelected) RosePrimary else DarkSurfaceElevated)
                                            .border(1.dp, if (isSelected) RosePrimary else DarkCardBorder, RoundedCornerShape(50))
                                            .clickable { selectedGifCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) DarkBackground else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "🎬 Toca un GIF para compartirlo en el chat",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )

                            // GIF Grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.heightIn(max = 260.dp)
                            ) {
                                items(filteredGifs) { gifItem ->
                                    Box(
                                        modifier = Modifier
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(DarkSurfaceElevated)
                                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                onSelectGif(gifItem.gifUrl, gifItem.title)
                                            }
                                    ) {
                                        AsyncImage(
                                            model = gifItem.gifUrl,
                                            contentDescription = gifItem.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                                    )
                                                )
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = gifItem.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Polaroid-style captured moment dialog
 */
@Composable
fun CinemaMomentPhotoDialog(
    bitmap: Bitmap,
    partnerName: String,
    onDismiss: () -> Unit,
    onSendToChat: () -> Unit
) {
    val dateStr = remember { SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault()).format(Date()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                borderColor = RosePrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📸 Foto del Momento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Polaroid Card Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Momento",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = "Noche de Cine con $partnerName ❤️",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Descartar", color = TextPrimary, fontSize = 12.sp)
                        }

                        RoseGradientButton(
                            onClick = onSendToChat,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Enviar al Chat", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen in-app Dialog for logging in to Netflix/Prime/Disney or viewing stream in full screen
 */
@Composable
fun FullScreenStreamingDialog(
    platform: StreamingPlatform,
    initialUrl: String,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onOpenMediaPicker: (CinemaPickerTab) -> Unit,
    onDismiss: () -> Unit,
    onSendReaction: (String) -> Unit
) {
    var fullScreenWebView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler {
            if (fullScreenWebView?.canGoBack() == true) {
                fullScreenWebView?.goBack()
            } else {
                onDismiss()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .systemBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Fullscreen Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar ventana completa",
                                tint = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(platform.tagColor.copy(alpha = 0.25f))
                                .border(1.dp, platform.tagColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = platform.iconEmoji, fontSize = 16.sp)
                        }

                        Column {
                            Text(
                                text = platform.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Pantalla Completa",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Top Action Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Play/Pause button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkBackground)
                                .clickable { onTogglePlayPause() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isPlaying) "⏸ Pausar" else "▶ Play",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        // Direct Login URL button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(platform.tagColor.copy(alpha = 0.25f))
                                .clickable { fullScreenWebView?.loadUrl(platform.loginUrl) }
                                .padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.labelSmall,
                                color = platform.tagColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Fullscreen In-App Web Browser
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    RaveWebPlayer(
                        url = initialUrl,
                        onWebViewCreated = { fullScreenWebView = it },
                        onLoadingChange = { isLoading = it }
                    )
                }

                // Bottom quick reaction & picker bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Open Emoji / GIF Picker button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkBackground)
                                .border(1.dp, RosePrimary.copy(alpha = 0.6f), RoundedCornerShape(50))
                                .clickable { onOpenMediaPicker(CinemaPickerTab.EMOJIS) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(text = "😊", fontSize = 12.sp)
                                Text(text = "GIFs", color = RosePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        listOf("❤️", "🍿", "😭", "😂", "🔥", "🫂", "✨", "💋", "🥰", "😱").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkBackground)
                                    .clickable { onSendReaction(emoji) }
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                            }
                        }
                    }

                    RoseGradientButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Volver a Sala",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
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
    onWebViewCreated: (WebView) -> Unit,
    onLoadingChange: (Boolean) -> Unit = {}
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
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    setSupportZoom(true)
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
                        onLoadingChange(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChange(false)
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

private fun saveBitmapToInternalCache(context: Context, bitmap: Bitmap): String {
    val file = File(context.cacheDir, "cinema_moment_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}
