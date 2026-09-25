package com.example.distancelove.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.distancelove.data.*
import com.example.distancelove.data.local.*
import com.example.distancelove.data.repository.NosotrosRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class NosotrosViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = NosotrosRepository(application, database)

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // --- Centralized Real Auth State ---
    val authState: StateFlow<AuthState> = repository.authState
    val currentUserProfile: StateFlow<UserProfile?> = repository.currentUserProfile
    val partnerProfile: StateFlow<UserProfile?> = repository.partnerProfile
    val coupleInfo: StateFlow<CoupleInfo?> = repository.coupleInfo

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // --- Time & Reunion Countdown ---
    private val _currentTime = MutableStateFlow(ZonedDateTime.now())
    val currentTime: StateFlow<ZonedDateTime> = _currentTime.asStateFlow()
    private val reunionTarget = ZonedDateTime.of(2026, 10, 18, 10, 0, 0, 0, ZoneId.systemDefault())

    // --- Heartbeat state ---
    private val _isHoldingHeart = MutableStateFlow(false)
    val isHoldingHeart: StateFlow<Boolean> = _isHoldingHeart.asStateFlow()

    private val _heartRipples = MutableStateFlow<List<Long>>(emptyList())
    val heartRipples: StateFlow<List<Long>> = _heartRipples.asStateFlow()
    private var heartbeatJob: Job? = null

    // --- Persistent Notes ---
    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Persistent Daily Questions & Chats ---
    val dailyAnswers: StateFlow<List<DailyAnswerEntity>> = repository.getDailyAnswers(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyChats: StateFlow<List<DailyChatEntity>> = repository.getDailyChats(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Persistent Challenges ---
    val challenges: StateFlow<List<ChallengeEntity>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Persistent Secret Questions ---
    val secretQuestions: StateFlow<List<SecretQuestionEntity>> = repository.allSecretQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Persistent Feed Posts ---
    val feedPosts: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeStory = MutableStateFlow<StoryItem?>(null)
    val activeStory: StateFlow<StoryItem?> = _activeStory.asStateFlow()

    val stories: StateFlow<List<StoryItem>> = combine(currentUserProfile, partnerProfile) { me, partner ->
        listOfNotNull(
            me?.let { StoryItem(it.fullName, it.avatarUrl) },
            partner?.let { StoryItem(it.fullName, it.avatarUrl) }
        ).ifEmpty {
            listOf(StoryItem("Tú", "feed3"))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, listOf(StoryItem("Tú", "feed3")))

    // --- Cinema state ---
    val defaultVideos = listOf(
        CinemaVideo("Big Buck Bunny", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
        CinemaVideo("Sintel", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"),
        CinemaVideo("Tears of Steel", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
    )

    private val _currentVideo = MutableStateFlow(defaultVideos[0])
    val currentVideo: StateFlow<CinemaVideo> = _currentVideo.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(false)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    private val _videoPositionMs = MutableStateFlow(0L)
    val videoPositionMs: StateFlow<Long> = _videoPositionMs.asStateFlow()

    private val _videoDurationMs = MutableStateFlow(0L)
    val videoDurationMs: StateFlow<Long> = _videoDurationMs.asStateFlow()

    private val _cinemaFloaters = MutableStateFlow<List<CinemaFloater>>(emptyList())
    val cinemaFloaters: StateFlow<List<CinemaFloater>> = _cinemaFloaters.asStateFlow()

    private val _isMicActive = MutableStateFlow(false)
    val isMicActive: StateFlow<Boolean> = _isMicActive.asStateFlow()

    private val _cinemaChat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val cinemaChat: StateFlow<List<ChatMessage>> = _cinemaChat.asStateFlow()

    // --- Persistent Desire Match & Vault ---
    val desireVotes: StateFlow<List<DesireVoteEntity>> = repository.allDesireVotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchedDesires: StateFlow<List<DesireVoteEntity>> = repository.matchedDesires
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _vaultUnlocked = MutableStateFlow(false)
    val vaultUnlocked: StateFlow<Boolean> = _vaultUnlocked.asStateFlow()

    private val _vaultPinInput = MutableStateFlow("")
    val vaultPinInput: StateFlow<String> = _vaultPinInput.asStateFlow()

    private val _vaultPinError = MutableStateFlow(false)
    val vaultPinError: StateFlow<Boolean> = _vaultPinError.asStateFlow()

    private val _isBiometricScanning = MutableStateFlow(false)
    val isBiometricScanning: StateFlow<Boolean> = _isBiometricScanning.asStateFlow()

    private val _matchFlash = MutableStateFlow<String?>(null)
    val matchFlash: StateFlow<String?> = _matchFlash.asStateFlow()

    private val _currentDesireIndex = MutableStateFlow(0)
    val currentDesireIndex: StateFlow<Int> = _currentDesireIndex.asStateFlow()

    val desireCards = listOf(
        DesireCard(1, "Baño a la luz de las velas"),
        DesireCard(2, "Fin de semana sin móviles"),
        DesireCard(3, "Carta de amor escrita a mano"),
        DesireCard(4, "Masaje relajante al reencontrarnos"),
        DesireCard(5, "Desayuno en la cama… y quedarnos")
    )

    val voiceMemories: StateFlow<List<VoiceMemoryItem>> = MutableStateFlow(
        listOf(
            VoiceMemoryItem(1, "Buenas noches, mi amor", "Tu pareja", "0:48"),
            VoiceMemoryItem(2, "Lo que no te dije en el aeropuerto", "Tú", "2:12"),
            VoiceMemoryItem(3, "Nuestro primer aniversario", "Tu pareja", "1:05")
        )
    ).asStateFlow()

    init {
        // 1. Check Supabase Auth Session on App Start
        viewModelScope.launch {
            repository.checkInitialSession()
        }

        // 2. Clock loop for live timezones & countdown
        viewModelScope.launch {
            while (true) {
                _currentTime.value = ZonedDateTime.now()
                delay(1000)
            }
        }
    }

    // --- Real Supabase Auth Actions ---

    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val res = repository.login(email, pass)
            _isAuthLoading.value = false
            if (res.isSuccess) {
                onSuccess()
            } else {
                _authError.value = res.exceptionOrNull()?.message ?: "Error al iniciar sesión"
            }
        }
    }

    fun register(
        email: String,
        pass: String,
        username: String,
        fullName: String,
        city: String,
        tz: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val res = repository.register(email, pass, username, fullName, city, tz)
            _isAuthLoading.value = false
            if (res.isSuccess) {
                onSuccess()
            } else {
                _authError.value = res.exceptionOrNull()?.message ?: "Error al registrarse"
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val res = repository.sendPasswordReset(email)
            _isAuthLoading.value = false
            if (res.isSuccess) {
                _statusMessage.value = "Correo de recuperación enviado a $email"
            } else {
                _authError.value = res.exceptionOrNull()?.message ?: "Error al enviar recuperación"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearAuthMessages() {
        _authError.value = null
        _statusMessage.value = null
    }

    // --- Profile & Couple Linking Actions ---

    fun updateProfile(
        fullName: String,
        city: String,
        timeZone: String,
        status: String,
        bio: String,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            repository.updateProfile(fullName, city, timeZone, status, bio, avatarUrl)
        }
    }

    fun updateAvatarFromUri(uri: Uri) {
        viewModelScope.launch {
            val uploadedUrl = repository.uploadPhotoToSupabase(uri, "avatars")
            val current = currentUserProfile.value
            if (current != null) {
                repository.updateProfile(
                    fullName = current.fullName,
                    city = current.city,
                    timeZone = current.timeZone,
                    status = current.status,
                    bio = current.bio,
                    avatarUrl = uploadedUrl
                )
            }
        }
    }

    fun linkCoupleWithCode(code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val res = repository.linkCoupleWithCode(code)
            _isAuthLoading.value = false
            if (res.isSuccess) {
                onSuccess()
            } else {
                _authError.value = res.exceptionOrNull()?.message ?: "Código de pareja no encontrado"
            }
        }
    }

    fun createCoupleInviteCode(onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val res = repository.createCoupleInviteCode()
            _isAuthLoading.value = false
            if (res.isSuccess) {
                onSuccess(res.getOrNull().orEmpty())
            } else {
                _authError.value = res.exceptionOrNull()?.message ?: "Error al crear pareja"
            }
        }
    }

    // --- Post Creation with Supabase Storage Upload ---

    fun createPost(imageUri: Uri, caption: String, location: String) {
        viewModelScope.launch {
            repository.createPost(imageUri, caption, location)
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId)
        }
    }

    fun isPostLikedFlow(postId: String): Flow<Boolean> = repository.isPostLiked(postId)

    fun getLikeCountFlow(postId: String): Flow<Int> = repository.getLikeCount(postId)

    fun getCommentsFlow(postId: String): Flow<List<PostCommentEntity>> = repository.getCommentsForPost(postId)

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(postId, text)
        }
    }

    // --- Notes Actions ---

    fun addNote(text: String) {
        viewModelScope.launch {
            repository.addNote(text)
        }
    }

    fun toggleNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleNote(note)
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // --- Daily Question Actions ---

    fun submitDailyAnswer(answer: String) {
        viewModelScope.launch {
            repository.submitDailyAnswer(1, answer)
        }
    }

    fun sendDailyChat(text: String) {
        viewModelScope.launch {
            repository.sendDailyChat(1, text)
        }
    }

    fun toggleChallenge(challenge: ChallengeEntity, isMe: Boolean) {
        viewModelScope.launch {
            repository.toggleChallenge(challenge, isMe)
        }
    }

    fun addSecretQuestion(text: String) {
        viewModelScope.launch {
            repository.addSecretQuestion(text)
        }
    }

    // --- Cinema Actions ---

    fun selectVideo(video: CinemaVideo) {
        _currentVideo.value = video
        _isVideoPlaying.value = false
        _videoPositionMs.value = 0L
    }

    fun setVideoPlaying(playing: Boolean) {
        _isVideoPlaying.value = playing
    }

    fun updateVideoProgress(positionMs: Long, durationMs: Long) {
        _videoPositionMs.value = positionMs
        _videoDurationMs.value = durationMs
    }

    fun addCinemaReaction(emoji: String) {
        val floater = CinemaFloater(
            id = System.currentTimeMillis(),
            emoji = emoji,
            xPercent = (10..80).random().toFloat()
        )
        _cinemaFloaters.update { it + floater }
        viewModelScope.launch {
            delay(2400)
            _cinemaFloaters.update { list -> list.filter { it.id != floater.id } }
        }
    }

    fun toggleMic() {
        _isMicActive.update { !it }
    }

    fun sendCinemaMessage(text: String) {
        if (text.isBlank()) return
        val user = currentUserProfile.value
        val msg = ChatMessage(System.currentTimeMillis(), user?.fullName ?: "Tú", text.trim())
        _cinemaChat.update { it + msg }
    }

    // --- Desire Match Actions ---

    fun decideDesireCard(userYes: Boolean) {
        val idx = _currentDesireIndex.value
        val card = desireCards.getOrNull(idx) ?: return
        viewModelScope.launch {
            repository.voteDesire(card.id, card.text, userYes)
            if (userYes && card.partnerLiked) {
                _matchFlash.value = card.text
                delay(1800)
                _matchFlash.value = null
            }
        }
        _currentDesireIndex.value = idx + 1
    }

    // --- Vault & PIN Actions ---

    fun enterVaultDigit(digit: String) {
        val current = _vaultPinInput.value
        if (current.length >= 4) return
        val updated = current + digit
        _vaultPinInput.value = updated
        _vaultPinError.value = false

        if (updated.length == 4) {
            viewModelScope.launch {
                delay(200)
                val settings = repository.vaultDao.getVaultSettingsDirect()
                val correctPin = settings?.pinCode ?: "1402"
                if (updated == correctPin) {
                    _vaultUnlocked.value = true
                    _vaultPinInput.value = ""
                } else {
                    _vaultPinError.value = true
                    _vaultPinInput.value = ""
                }
            }
        }
    }

    fun deleteVaultDigit() {
        val current = _vaultPinInput.value
        if (current.isNotEmpty()) {
            _vaultPinInput.value = current.dropLast(1)
        }
    }

    fun scanBiometrics() {
        _isBiometricScanning.value = true
        viewModelScope.launch {
            delay(1000)
            _vaultUnlocked.value = true
            _isBiometricScanning.value = false
            _vaultPinInput.value = ""
        }
    }

    fun lockVault() {
        _vaultUnlocked.value = false
        _vaultPinInput.value = ""
        _vaultPinError.value = false
    }

    // --- Heartbeat actions ---

    fun startHeartbeat() {
        _isHoldingHeart.value = true
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (_isHoldingHeart.value) {
                val rippleId = System.currentTimeMillis()
                _heartRipples.update { it + rippleId }
                triggerHapticPulse()
                launch {
                    delay(1600)
                    _heartRipples.update { list -> list.filter { it != rippleId } }
                }
                delay(900)
            }
        }
    }

    fun stopHeartbeat() {
        _isHoldingHeart.value = false
        heartbeatJob?.cancel()
    }

    private fun triggerHapticPulse() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 80, 40)
                val amplitudes = intArrayOf(0, 180, 0, 220)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun openStory(story: StoryItem) {
        _activeStory.value = story
    }

    fun closeStory() {
        _activeStory.value = null
    }

    fun getReunionCountdown(): CountdownTime {
        val now = _currentTime.value
        val nowLocal = now.toLocalDateTime()
        val targetLocal = reunionTarget.toLocalDateTime()
        val duration = if (targetLocal.isAfter(nowLocal)) Duration.between(nowLocal, targetLocal) else Duration.ZERO
        val totalSeconds = duration.seconds
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return CountdownTime(days, hours, minutes, seconds)
    }

    fun formatCityTime(tzString: String): String {
        return try {
            val zone = ZoneId.of(tzString)
            val zdt = _currentTime.value.withZoneSameInstant(zone)
            zdt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
        } catch (e: Exception) {
            "--:--"
        }
    }
}
