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

data class CinemaMovieItem(
    val id: String,
    val title: String,
    val platform: StreamingPlatform,
    val type: String, // "Película" or "Serie"
    val genre: String,
    val year: String,
    val duration: String,
    val rating: String,
    val synopsis: String,
    val posterUrl: String,
    val videoStreamUrl: String,
    val aspectRatio: Float = 16f / 9f,
    val formatName: String = "16:9 HD",
    val availableAudios: List<String> = listOf("Español Latino (5.1 Dolby)", "Español España (Castellano)", "Inglés [Original] (Dolby Atmos)", "Francés (Estéreo)"),
    val availableSubtitles: List<String> = listOf("Desactivados", "Español Latino", "Español (CC)", "Inglés [CC]", "Francés"),
    val availableQualities: List<String> = listOf("4K Ultra HD (2160p HDR)", "1080p Full HD (60 FPS)", "720p HD", "480p SD", "Automática (Dinámica)")
)

enum class CinemaAspectMode(val label: String, val ratio: Float?, val icon: String) {
    AUTO("Formato Original", null, "📐"),
    CINEMA_21_9("21:9 Cinema Scope", 21f / 9f, "🎬"),
    WIDESCREEN_16_9("16:9 Panorámico", 16f / 9f, "📺"),
    UNIVISIUM_2_1("2:1 Univisium", 2.0f, "📽️"),
    CLASSIC_4_3("4:3 Clásico", 4f / 3f, "📼")
}

val PLATFORM_CATALOG_MOVIES = listOf(
    // Netflix
    CinemaMovieItem(
        id = "nf_1",
        title = "Heartstopper: París con Amor",
        platform = StreamingPlatform.NETFLIX,
        type = "Serie",
        genre = "Romance / Drama",
        year = "2024",
        duration = "3 Temporadas",
        rating = "99% Match",
        synopsis = "Nick y Charlie viajan juntos a París mientras descubren nuevos sentimientos y fortalecen su relación en una aventura inolvidable.",
        posterUrl = "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        aspectRatio = 2.39f,
        formatName = "21:9 Cinema Scope"
    ),
    CinemaMovieItem(
        id = "nf_2",
        title = "A Través de Mi Ventana",
        platform = StreamingPlatform.NETFLIX,
        type = "Película",
        genre = "Romance / Juvenil",
        year = "2023",
        duration = "1h 56m",
        rating = "96% Match",
        synopsis = "La atracción magnética y secreta entre Raquel y Ares se transforma en una historia apasionada que desafía todas las reglas familiares.",
        posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        aspectRatio = 16f / 9f,
        formatName = "16:9 Panorámico"
    ),
    CinemaMovieItem(
        id = "nf_3",
        title = "Stranger Things: El Desenlace",
        platform = StreamingPlatform.NETFLIX,
        type = "Serie",
        genre = "Ciencia Ficción / Misterio",
        year = "2024",
        duration = "4 Temporadas",
        rating = "98% Match",
        synopsis = "El grupo de Hawkins se reúne para librar la batalla definitiva contra las sombras del Upside Down y proteger a sus seres amados.",
        posterUrl = "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        aspectRatio = 2.0f,
        formatName = "2:1 Univisium"
    ),
    CinemaMovieItem(
        id = "nf_4",
        title = "Noche de Romance en Venecia",
        platform = StreamingPlatform.NETFLIX,
        type = "Película",
        genre = "Comedia Romántica",
        year = "2024",
        duration = "1h 48m",
        rating = "95% Match",
        synopsis = "Dos almas destinadas a encontrarse cruzan sus caminos entre góndolas, luces mágicas y secretos compartidos bajo la luna veneciana.",
        posterUrl = "https://images.unsplash.com/photo-1514306191717-452ec28c7814?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        aspectRatio = 2.35f,
        formatName = "2.35:1 Anamórfico"
    ),

    // Prime Video
    CinemaMovieItem(
        id = "pv_1",
        title = "Culpa Mía: Amor a Toda Velocidad",
        platform = StreamingPlatform.PRIME_VIDEO,
        type = "Película",
        genre = "Romance / Acción",
        year = "2023",
        duration = "1h 59m",
        rating = "★ 9.4",
        synopsis = "Noah y Nick no pueden evitar la pasión desmedida entre carreras clandestinas y un amor que desafía todos los límites.",
        posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        aspectRatio = 2.39f,
        formatName = "21:9 Cinema Scope"
    ),
    CinemaMovieItem(
        id = "pv_2",
        title = "El Verano en que me Enamoré",
        platform = StreamingPlatform.PRIME_VIDEO,
        type = "Serie",
        genre = "Romance / Drama",
        year = "2024",
        duration = "2 Temporadas",
        rating = "★ 9.5",
        synopsis = "Un verano que lo cambia todo: primeros amores, promesas en la playa de Cousins y decisiones que marcarán sus vidas para siempre.",
        posterUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        aspectRatio = 2.0f,
        formatName = "2:1 Univisium"
    ),
    CinemaMovieItem(
        id = "pv_3",
        title = "The Boys: Edición Especial",
        platform = StreamingPlatform.PRIME_VIDEO,
        type = "Serie",
        genre = "Acción / Humor Negro",
        year = "2024",
        duration = "4 Temporadas",
        rating = "★ 9.7",
        synopsis = "La batalla sin reglas entre vigilantes decididos y superhéroes fuera de control llega a su punto más explosivo.",
        posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
        aspectRatio = 16f / 9f,
        formatName = "16:9 Panorámico"
    ),

    // Disney+
    CinemaMovieItem(
        id = "dp_1",
        title = "Encanto: La Magia Juntos",
        platform = StreamingPlatform.DISNEY_PLUS,
        type = "Película",
        genre = "Animación / Musical",
        year = "2023",
        duration = "1h 49m",
        rating = "★ 9.6",
        synopsis = "La extraordinaria familia Madrigal vive escondida en las montañas de Colombia en una casa mágica llena de música y calidez.",
        posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        aspectRatio = 16f / 9f,
        formatName = "16:9 HD"
    ),
    CinemaMovieItem(
        id = "dp_2",
        title = "Aladdín: Un Mundo Ideal",
        platform = StreamingPlatform.DISNEY_PLUS,
        type = "Película",
        genre = "Romance / Aventura",
        year = "2022",
        duration = "2h 08m",
        rating = "★ 9.4",
        synopsis = "Vuela sobre la alfombra mágica a través de las estrellas mientras el amor florece en el reino de Agrabah.",
        posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        aspectRatio = 2.39f,
        formatName = "21:9 Cinema Scope"
    ),
    CinemaMovieItem(
        id = "dp_3",
        title = "The Mandalorian & Grogu",
        platform = StreamingPlatform.DISNEY_PLUS,
        type = "Serie",
        genre = "Ciencia Ficción / Aventura",
        year = "2024",
        duration = "3 Temporadas",
        rating = "★ 9.8",
        synopsis = "Un viaje épico por los confines de la galaxia protegiendo el lazo más puro de lealtad y cariño.",
        posterUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        aspectRatio = 2.39f,
        formatName = "21:9 Cinema Scope"
    ),

    // Max
    CinemaMovieItem(
        id = "mx_1",
        title = "Euphoria: Amor & Sueños",
        platform = StreamingPlatform.MAX,
        type = "Serie",
        genre = "Drama / Romance",
        year = "2024",
        duration = "2 Temporadas",
        rating = "★ 9.5",
        synopsis = "Un retrato visualmente hipnótico sobre la vulnerabilidad, las conexiones profundas y el anhelo de afecto verdadero.",
        posterUrl = "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        aspectRatio = 2.39f,
        formatName = "21:9 Cinema Scope"
    ),
    CinemaMovieItem(
        id = "mx_2",
        title = "Dune: Destino de Arrakis",
        platform = StreamingPlatform.MAX,
        type = "Película",
        genre = "Ciencia Ficción / Romance Épico",
        year = "2024",
        duration = "2h 46m",
        rating = "★ 9.9",
        synopsis = "Paul Atreides y Chani forjan una alianza de honor y pasión en el desierto infinito de Arrakis.",
        posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        aspectRatio = 2.39f,
        formatName = "21:9 Cinema Scope"
    ),

    // Crunchyroll
    CinemaMovieItem(
        id = "cr_1",
        title = "Your Name (Kimi no Na wa)",
        platform = StreamingPlatform.CRUNCHYROLL,
        type = "Película",
        genre = "Anime / Romance",
        year = "2023",
        duration = "1h 52m",
        rating = "★ 9.9",
        synopsis = "Mitsuha y Taki intercambian cuerpos y memorias a través del tiempo, unidos por el hilo rojo del destino.",
        posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        aspectRatio = 16f / 9f,
        formatName = "16:9 HD",
        availableAudios = listOf("Japonés [Original] (Voces Japonesas)", "Español Latino (Doblaje)", "Español España (Castellano)", "Inglés"),
        availableSubtitles = listOf("Desactivados", "Español Latino", "Español España", "Inglés"),
        availableQualities = listOf("1080p Full HD (60 FPS)", "720p HD", "480p SD", "Automática")
    ),
    CinemaMovieItem(
        id = "cr_2",
        title = "Kimetsu no Yaiba: Lazos Eternos",
        platform = StreamingPlatform.CRUNCHYROLL,
        type = "Serie",
        genre = "Anime / Acción",
        year = "2024",
        duration = "4 Temporadas",
        rating = "★ 9.8",
        synopsis = "Tanjiro y Nezuko luchan juntos sin rendirse jamás para proteger el vínculo indestructible de su familia.",
        posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        aspectRatio = 16f / 9f,
        formatName = "16:9 HD",
        availableAudios = listOf("Japonés [Original] (Voces Japonesas)", "Español Latino (Doblaje)", "Español España (Castellano)", "Inglés"),
        availableSubtitles = listOf("Desactivados", "Español Latino", "Español España", "Inglés"),
        availableQualities = listOf("1080p Full HD (60 FPS)", "720p HD", "480p SD", "Automática")
    ),

    // YouTube
    CinemaMovieItem(
        id = "yt_1",
        title = "Noche de Cortos Románticos 4K",
        platform = StreamingPlatform.YOUTUBE,
        type = "Película",
        genre = "Cine Independiente",
        year = "2024",
        duration = "48m",
        rating = "★ 9.7",
        synopsis = "Una selección premiada de historias de amor y momentos que tocan el corazón con cinematografía en alta definición.",
        posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        aspectRatio = 2.35f,
        formatName = "2.35:1 Anamórfico"
    ),
    CinemaMovieItem(
        id = "yt_2",
        title = "Lo-Fi Romance Beats 24/7",
        platform = StreamingPlatform.YOUTUBE,
        type = "Serie",
        genre = "Música / Chill",
        year = "2024",
        duration = "En Vivo",
        rating = "★ 9.9",
        synopsis = "Música y animación acogedora para charlar, relajarse y disfrutar de la compañía del otro a la distancia.",
        posterUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        aspectRatio = 16f / 9f,
        formatName = "16:9 HD"
    ),

    // Twitch
    CinemaMovieItem(
        id = "tw_1",
        title = "Watch Party Directo en Vivo",
        platform = StreamingPlatform.TWITCH,
        type = "Serie",
        genre = "Streaming / Gaming",
        year = "2024",
        duration = "Directo",
        rating = "★ 9.6",
        synopsis = "Transmisión interactiva en tiempo real con comentarios sincronizados y diversión en pareja.",
        posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&auto=format&fit=crop&q=80",
        videoStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
        aspectRatio = 16f / 9f,
        formatName = "16:9 HD"
    )
)

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

    // Catalog & Selected Movie state
    var selectedMovie by remember(selectedPlatform) {
        mutableStateOf(
            PLATFORM_CATALOG_MOVIES.firstOrNull { it.platform == selectedPlatform }
                ?: PLATFORM_CATALOG_MOVIES.first()
        )
    }
    var isCatalogOpen by remember { mutableStateOf(false) }

    // Audio, Subtitles & Quality settings
    var selectedAudio by remember(selectedMovie) {
        mutableStateOf(selectedMovie.availableAudios.firstOrNull() ?: "Español Latino (5.1 Dolby)")
    }
    var selectedSubtitle by remember(selectedMovie) {
        mutableStateOf(selectedMovie.availableSubtitles.getOrNull(1) ?: "Español Latino")
    }
    var selectedQuality by remember(selectedMovie) {
        mutableStateOf(selectedMovie.availableQualities.getOrNull(1) ?: "1080p Full HD (60 FPS)")
    }

    // Video aspect ratio adaptation
    var selectedAspectMode by remember { mutableStateOf(CinemaAspectMode.AUTO) }
    var isPlayerMenuExpanded by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showAspectDialog by remember { mutableStateOf(false) }

    val effectiveAspectRatio = selectedAspectMode.ratio ?: selectedMovie.aspectRatio
    val animatedAspectRatio by animateFloatAsState(
        targetValue = effectiveAspectRatio,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cinema_viewport_aspect_ratio"
    )

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

    // Language & Subtitles Selection Dialog
    if (showLanguageDialog) {
        CinemaAudioSubtitleDialog(
            platform = selectedPlatform,
            movie = selectedMovie,
            selectedAudio = selectedAudio,
            selectedSubtitle = selectedSubtitle,
            onSelectAudio = { audio ->
                selectedAudio = audio
                viewModel.sendCinemaMessage("🎧 Audio seleccionado: $audio")
            },
            onSelectSubtitle = { sub ->
                selectedSubtitle = sub
                viewModel.sendCinemaMessage("💬 Subtítulos: $sub")
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Video Quality Selection Dialog
    if (showQualityDialog) {
        CinemaQualityDialog(
            platform = selectedPlatform,
            movie = selectedMovie,
            selectedQuality = selectedQuality,
            onSelectQuality = { quality ->
                selectedQuality = quality
                viewModel.sendCinemaMessage("⚡ Calidad de video: $quality")
            },
            onDismiss = { showQualityDialog = false }
        )
    }

    // Aspect Ratio / Formato Selection Dialog
    if (showAspectDialog) {
        CinemaAspectDialog(
            currentMovie = selectedMovie,
            selectedAspectMode = selectedAspectMode,
            onSelectAspectMode = { mode ->
                selectedAspectMode = mode
            },
            onDismiss = { showAspectDialog = false }
        )
    }

    // Platform Movies & Series Catalog Dialog (Opened from 3-dots menu or "Catálogo")
    if (isCatalogOpen) {
        PlatformCatalogDialog(
            platform = selectedPlatform,
            currentSelectedMovie = selectedMovie,
            onSelectMovie = { movie ->
                selectedMovie = movie
                viewModel.leaderSetPlaying(true)
                viewModel.sendCinemaMessage("🎬 Ahora viendo juntos: ${movie.title} (${movie.type})")
                isCatalogOpen = false
            },
            onOpenWebLogin = {
                fullScreenTargetUrl = selectedPlatform.loginUrl
                isCatalogOpen = false
                isFullScreenOpen = true
            },
            onDismiss = { isCatalogOpen = false }
        )
    }

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

        // 2. FIXED CINEMA PLAYER (ADAPTS ITS SHAPE TO THE VIDEO FORMAT)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cinema_main_player"),
            shape = RoundedCornerShape(20.dp),
            borderColor = selectedPlatform.tagColor.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top header row: 3-dots options menu on top-left, Platform info & Title, Status & Mic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left section: 3-dots menu button, platform badge, title & details
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // 3-DOTS OPTIONS MENU (Top-Left of Player)
                        Box {
                            IconButton(
                                onClick = { isPlayerMenuExpanded = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, selectedPlatform.tagColor.copy(alpha = 0.5f), CircleShape)
                                    .testTag("cinema_player_three_dots_menu")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Opciones del reproductor",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isPlayerMenuExpanded,
                                onDismissRequest = { isPlayerMenuExpanded = false },
                                modifier = Modifier
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                                    .width(260.dp)
                            ) {
                                // 1. Elegir Película / Serie
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "Elegir Película / Serie",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Catálogo de ${selectedPlatform.title}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.MovieCreation,
                                            contentDescription = null,
                                            tint = selectedPlatform.tagColor
                                        )
                                    },
                                    onClick = {
                                        isPlayerMenuExpanded = false
                                        isCatalogOpen = true
                                    }
                                )

                                HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // 2. Idioma de Audio y Subtítulos
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "Idioma y Subtítulos",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Audio: $selectedAudio",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = selectedPlatform.tagColor,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Language,
                                            contentDescription = null,
                                            tint = RosePrimary
                                        )
                                    },
                                    onClick = {
                                        isPlayerMenuExpanded = false
                                        showLanguageDialog = true
                                    }
                                )

                                HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // 3. Calidad de Video
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "Calidad de Video",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = selectedQuality,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = selectedPlatform.tagColor,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.HighQuality,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700)
                                        )
                                    },
                                    onClick = {
                                        isPlayerMenuExpanded = false
                                        showQualityDialog = true
                                    }
                                )

                                HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // 4. Formato de Pantalla / Aspect Ratio
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "Formato de Pantalla",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (selectedAspectMode == CinemaAspectMode.AUTO) "Auto (${selectedMovie.formatName})" else selectedAspectMode.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.AspectRatio,
                                            contentDescription = null,
                                            tint = TextPrimary
                                        )
                                    },
                                    onClick = {
                                        isPlayerMenuExpanded = false
                                        showAspectDialog = true
                                    }
                                )

                                HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // 5. Iniciar sesión / Web Login
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Iniciar Sesión en ${selectedPlatform.title}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary
                                        )
                                    },
                                    leadingIcon = {
                                        Text(selectedPlatform.iconEmoji, fontSize = 16.sp)
                                    },
                                    onClick = {
                                        isPlayerMenuExpanded = false
                                        fullScreenTargetUrl = selectedPlatform.loginUrl
                                        isFullScreenOpen = true
                                    }
                                )
                            }
                        }

                        // Platform Icon Badge
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(selectedPlatform.tagColor.copy(alpha = 0.35f))
                                .border(1.dp, selectedPlatform.tagColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedPlatform.iconEmoji, fontSize = 16.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedMovie.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${selectedMovie.type} • ${selectedMovie.year}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(selectedPlatform.tagColor.copy(alpha = 0.25f))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = selectedMovie.rating,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = selectedPlatform.tagColor
                                    )
                                }
                            }
                        }
                    }

                    // Right section: Status badge & Mic
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Play status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkBackground.copy(alpha = 0.8f))
                                .border(1.dp, if (isPlaying) SuccessGreen.copy(alpha = 0.6f) else RosePrimary.copy(alpha = 0.6f), RoundedCornerShape(50))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) SuccessGreen else RosePrimary)
                                )
                                Text(
                                    text = if (isPlaying) "En Vivo" else "Pausa",
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
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isMicActive) SuccessGreen else DarkBackground.copy(alpha = 0.8f))
                                .border(1.dp, DarkCardBorder, CircleShape)
                                .clickable { viewModel.toggleMic() }
                                .testTag("cinema_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMicActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                                contentDescription = "Micrófono",
                                tint = if (isMicActive) DarkBackground else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Video Screen Box (ADAPTS ITS SHAPE AUTOMATICALLY TO THE VIDEO FORMAT)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(animatedAspectRatio)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .border(1.dp, selectedPlatform.tagColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CinemaPureVideoPlayer(
                        videoUrl = selectedMovie.videoStreamUrl,
                        isPlaying = isPlaying,
                        onTogglePlayPause = { viewModel.leaderSetPlaying(!isPlaying) }
                    )

                    // Floating emojis layer inside the player viewport
                    floaters.forEach { floater ->
                        FloaterEmojiView(floater = floater)
                    }

                    // Central play overlay icon when paused (reproduces directly with play button)
                    if (!isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .border(1.5.dp, RosePrimary, CircleShape)
                                .clickable { viewModel.leaderSetPlaying(true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Reproducir",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
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

        // 3. INDEPENDENTLY SCROLLING CHAT CARD AT BOTTOM (EXPANDS TO FILL SCREEN)
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
 * Audio and Subtitles Selector Dialog
 */
@Composable
fun CinemaAudioSubtitleDialog(
    platform: StreamingPlatform,
    movie: CinemaMovieItem,
    selectedAudio: String,
    selectedSubtitle: String,
    onSelectAudio: (String) -> Unit,
    onSelectSubtitle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                borderColor = platform.tagColor.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(platform.tagColor.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = platform.tagColor, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = "Idioma y Subtítulos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${movie.title} • ${platform.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }

                    // Section 1: Audio / Doblaje
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🎧 PISTA DE AUDIO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = platform.tagColor,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )

                        movie.availableAudios.forEach { audio ->
                            val isSelected = selectedAudio == audio
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) platform.tagColor.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) platform.tagColor else DarkCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectAudio(audio)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.VolumeUp,
                                            contentDescription = null,
                                            tint = if (isSelected) platform.tagColor else TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = audio,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Activo",
                                            tint = platform.tagColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: Subtítulos
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "💬 SUBTÍTULOS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RosePrimary,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )

                        movie.availableSubtitles.forEach { sub ->
                            val isSelected = selectedSubtitle == sub
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) RosePrimary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) RosePrimary else DarkCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectSubtitle(sub)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Subtitles,
                                            contentDescription = null,
                                            tint = if (isSelected) RosePrimary else TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = sub,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Activo",
                                            tint = RosePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    RoseGradientButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aplicar y Volver al Reproductor", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Video Quality Selector Dialog
 */
@Composable
fun CinemaQualityDialog(
    platform: StreamingPlatform,
    movie: CinemaMovieItem,
    selectedQuality: String,
    onSelectQuality: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                borderColor = Color(0xFFFFD700).copy(alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFD700).copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Filled.HighQuality, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = "Calidad de Video",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Disponible en ${platform.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        movie.availableQualities.forEach { quality ->
                            val isSelected = selectedQuality == quality
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFFFFD700).copy(alpha = 0.18f) else DarkSurfaceElevated)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFFD700) else DarkCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectQuality(quality)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Hd,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFFFFD700) else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = quality,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Activo",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    RoseGradientButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirmar Calidad", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Aspect Ratio / Screen Format Selector Dialog
 */
@Composable
fun CinemaAspectDialog(
    currentMovie: CinemaMovieItem,
    selectedAspectMode: CinemaAspectMode,
    onSelectAspectMode: (CinemaAspectMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                borderColor = RosePrimary.copy(alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RosePrimary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Filled.AspectRatio, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = "Formato de Pantalla",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Adapta la forma del reproductor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CinemaAspectMode.entries.forEach { mode ->
                            val isSelected = selectedAspectMode == mode
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) RosePrimary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) RosePrimary else DarkCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectAspectMode(mode)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 11.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = mode.icon, fontSize = 16.sp)
                                        Column {
                                            Text(
                                                text = if (mode == CinemaAspectMode.AUTO) "Auto / Original (${currentMovie.formatName})" else mode.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isSelected) Color.White else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                            if (mode == CinemaAspectMode.AUTO) {
                                                Text(
                                                    text = "Adapta automáticamente a ${currentMovie.formatName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = RosePrimary,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Activo",
                                            tint = RosePrimary,
                                            modifier = Modifier.size(16.dp)
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

/**
 * Clean Pure Video Player that renders ONLY the video stream without web chrome/ads
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CinemaPureVideoPlayer(
    videoUrl: String,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(isPlaying, webViewInstance) {
        webViewInstance?.let { wv ->
            val js = if (isPlaying) {
                "var v = document.getElementById('cinema_video'); if (v) { v.play(); }"
            } else {
                "var v = document.getElementById('cinema_video'); if (v) { v.pause(); }"
            }
            wv.evaluateJavascript(js, null)
        }
    }

    LaunchedEffect(videoUrl) {
        webViewInstance?.let { wv ->
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; background: #000; }
                        body, html { width: 100%; height: 100%; overflow: hidden; background: #000000; display: flex; align-items: center; justify-content: center; }
                        video { width: 100%; height: 100%; object-fit: contain; background: #000; outline: none; }
                    </style>
                </head>
                <body>
                    <video id="cinema_video" src="$videoUrl" autoplay ${if (isPlaying) "autoplay" else ""} loop playsinline webkit-playsinline></video>
                    <script>
                        var v = document.getElementById('cinema_video');
                        if (v) {
                            ${if (isPlaying) "v.play();" else "v.pause();"}
                        }
                    </script>
                </body>
                </html>
            """.trimIndent()
            wv.loadDataWithBaseURL("https://cinema.local", html, "text/html", "UTF-8", null)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black).clickable { onTogglePlayPause() }) {
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
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    setBackgroundColor(android.graphics.Color.BLACK)
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()

                    val html = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                            <style>
                                * { margin: 0; padding: 0; box-sizing: border-box; background: #000; }
                                body, html { width: 100%; height: 100%; overflow: hidden; background: #000000; display: flex; align-items: center; justify-content: center; }
                                video { width: 100%; height: 100%; object-fit: contain; background: #000; outline: none; }
                            </style>
                        </head>
                        <body>
                            <video id="cinema_video" src="$videoUrl" autoplay ${if (isPlaying) "autoplay" else ""} loop playsinline webkit-playsinline></video>
                            <script>
                                var v = document.getElementById('cinema_video');
                                if (v) {
                                    ${if (isPlaying) "v.play();" else "v.pause();"}
                                }
                            </script>
                        </body>
                        </html>
                    """.trimIndent()
                    loadDataWithBaseURL("https://cinema.local", html, "text/html", "UTF-8", null)
                    webViewInstance = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Platform Catalog Modal Dialog (Lists movies & series of the selected platform to choose & watch)
 */
@Composable
fun PlatformCatalogDialog(
    platform: StreamingPlatform,
    currentSelectedMovie: CinemaMovieItem,
    onSelectMovie: (CinemaMovieItem) -> Unit,
    onOpenWebLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Películas", "Series", "Romance", "Acción")

    val platformMovies = remember(platform) {
        PLATFORM_CATALOG_MOVIES.filter { it.platform == platform }
    }

    val filteredMovies = remember(platformMovies, searchQuery, selectedCategory) {
        platformMovies.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "Todos" -> true
                "Películas" -> item.type.contains("Película", ignoreCase = true)
                "Series" -> item.type.contains("Serie", ignoreCase = true)
                "Romance" -> item.genre.contains("Romance", ignoreCase = true)
                "Acción" -> item.genre.contains("Acción", ignoreCase = true) || item.genre.contains("Ficción", ignoreCase = true)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.genre.contains(searchQuery, ignoreCase = true) ||
                    item.synopsis.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
                .padding(horizontal = 14.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                borderColor = platform.tagColor.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Bar
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
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(platform.tagColor.copy(alpha = 0.28f))
                                    .border(1.dp, platform.tagColor, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = platform.iconEmoji, fontSize = 20.sp)
                            }

                            Column {
                                Text(
                                    text = "Catálogo de ${platform.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Elige qué ver juntos en la sala",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Search Field
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar en ${platform.title}...", color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = platform.tagColor, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Limpiar", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = platform.tagColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) platform.tagColor else DarkSurfaceElevated)
                                    .border(1.dp, if (isSelected) platform.tagColor else DarkCardBorder, RoundedCornerShape(50))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Movie & Series List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredMovies, key = { it.id }) { movie ->
                            val isCurrent = movie.id == currentSelectedMovie.id
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                borderColor = if (isCurrent) platform.tagColor else DarkCardBorder.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Movie Poster
                                    Box(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(115.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(DarkSurfaceElevated)
                                    ) {
                                        AsyncImage(
                                            model = movie.posterUrl,
                                            contentDescription = movie.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Black.copy(alpha = 0.75f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = movie.type,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = platform.tagColor
                                            )
                                        }
                                    }

                                    // Movie Information & "VER" Button
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = movie.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${movie.genre} • ${movie.duration}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(platform.tagColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = movie.rating,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = platform.tagColor
                                                )
                                            }
                                        }

                                        Text(
                                            text = movie.synopsis,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary.copy(alpha = 0.75f),
                                            fontSize = 10.sp,
                                            maxLines = 2
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Prominent "VER" Button
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(platform.tagColor, platform.secondaryColor)
                                                    )
                                                )
                                                .clickable { onSelectMovie(movie) }
                                                .padding(vertical = 8.dp)
                                                .testTag("btn_watch_${movie.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = "Ver",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = if (isCurrent) "▶ Ver Ahora (En Reproductor)" else "▶ Ver ${movie.type}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredMovies.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "🎬", fontSize = 32.sp)
                                        Text(text = "No se encontraron títulos", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Option: Web Login for personal accounts
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                            .clickable { onOpenWebLogin() }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = platform.tagColor, modifier = Modifier.size(14.dp))
                            Text(
                                text = "O inicia sesión en tu cuenta web de ${platform.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
