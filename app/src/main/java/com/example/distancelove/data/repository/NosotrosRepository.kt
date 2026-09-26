package com.example.distancelove.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.distancelove.data.*
import com.example.distancelove.data.local.*
import com.example.distancelove.data.remote.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class NosotrosRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    val userDao = database.userDao()
    val postDao = database.postDao()
    val noteDao = database.noteDao()
    val dailyDao = database.dailyDao()
    val challengeDao = database.challengeDao()
    val secretDao = database.secretQuestionDao()
    val desireDao = database.desireDao()
    val vaultDao = database.vaultSettingsDao()

    val supabase = SupabaseService(context)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _partnerProfile = MutableStateFlow<UserProfile?>(null)
    val partnerProfile: StateFlow<UserProfile?> = _partnerProfile.asStateFlow()

    private val _coupleInfo = MutableStateFlow<CoupleInfo?>(null)
    val coupleInfo: StateFlow<CoupleInfo?> = _coupleInfo.asStateFlow()

    // Initialize & restore session on startup
    suspend fun checkInitialSession() = withContext(Dispatchers.IO) {
        val savedSession = supabase.getSavedSession()
        if (savedSession == null) {
            _authState.value = AuthState.Unauthenticated
            return@withContext
        }

        try {
            _authState.value = AuthState.Loading

            // Refresh token if needed
            val session = if (System.currentTimeMillis() >= savedSession.expiresAt - 60000L) {
                val refreshResult = supabase.refreshSession()
                if (refreshResult.isSuccess) refreshResult.getOrNull() else savedSession
            } else {
                savedSession
            }

            if (session == null) {
                _authState.value = AuthState.Unauthenticated
                return@withContext
            }

            // Fetch real user profile from Supabase
            val profile = loadUserProfileFromSupabase(session.userId, session.email)
            if (profile != null) {
                _currentUserProfile.value = profile
                saveLocalUserCache(profile, isCurrent = true)

                // Load couple & partner
                loadCoupleAndPartner(profile)

                _authState.value = AuthState.Authenticated(
                    user = profile,
                    partner = _partnerProfile.value,
                    couple = _coupleInfo.value
                )

                // Background sync couple data
                syncCoupleData()
            } else {
                // If profile row doesn't exist yet, create default
                val newProfile = UserProfile(
                    id = session.userId,
                    email = session.email,
                    username = session.email.substringBefore("@"),
                    fullName = session.email.substringBefore("@").replaceFirstChar { it.uppercase() }
                )
                upsertUserProfileToSupabase(newProfile)
                _currentUserProfile.value = newProfile
                _authState.value = AuthState.Authenticated(newProfile, null, null)
            }
        } catch (e: Exception) {
            Log.e("NosotrosRepo", "checkInitialSession error", e)
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun login(email: String, pass: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        val res = supabase.signIn(email, pass)

        if (res.isFailure) {
            val err = res.exceptionOrNull()?.message ?: "Error al iniciar sesión"
            _authState.value = AuthState.Unauthenticated
            return@withContext Result.failure(Exception(err))
        }

        val session = res.getOrThrow()
        var profile = loadUserProfileFromSupabase(session.userId, session.email)

        if (profile == null) {
            profile = UserProfile(
                id = session.userId,
                email = session.email,
                username = session.email.substringBefore("@"),
                fullName = session.email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
            upsertUserProfileToSupabase(profile)
        }

        _currentUserProfile.value = profile
        saveLocalUserCache(profile, isCurrent = true)
        loadCoupleAndPartner(profile)

        _authState.value = AuthState.Authenticated(
            user = profile,
            partner = _partnerProfile.value,
            couple = _coupleInfo.value
        )

        syncCoupleData()
        Result.success(profile)
    }

    suspend fun register(
        email: String,
        pass: String,
        username: String,
        fullName: String,
        city: String,
        timeZone: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading

        val meta = JSONObject().apply {
            put("full_name", fullName.trim())
            put("username", username.trim().lowercase())
            put("city", city.trim())
            put("timezone", timeZone.trim())
        }

        val res = supabase.signUp(email, pass, meta)
        if (res.isFailure) {
            val err = res.exceptionOrNull()?.message ?: "Error al registrarse"
            _authState.value = AuthState.Unauthenticated
            return@withContext Result.failure(Exception(err))
        }

        val session = res.getOrThrow()
        val newProfile = UserProfile(
            id = session.userId,
            email = session.email,
            username = username.trim().lowercase(),
            fullName = fullName.trim(),
            city = city.ifBlank { "Madrid" },
            timeZone = timeZone.ifBlank { "Europe/Madrid" }
        )

        upsertUserProfileToSupabase(newProfile)

        _currentUserProfile.value = newProfile
        saveLocalUserCache(newProfile, isCurrent = true)

        _authState.value = AuthState.Authenticated(
            user = newProfile,
            partner = null,
            couple = null
        )

        Result.success(newProfile)
    }

    suspend fun sendPasswordReset(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        supabase.sendPasswordReset(email)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        supabase.signOut()
        userDao.clearAll()
        postDao.clearAll()
        noteDao.clearAll()
        _currentUserProfile.value = null
        _partnerProfile.value = null
        _coupleInfo.value = null
        _authState.value = AuthState.Unauthenticated
    }

    suspend fun updateProfile(
        fullName: String,
        city: String,
        timeZone: String,
        status: String,
        bio: String,
        avatarUrl: String? = null
    ) = withContext(Dispatchers.IO) {
        val current = _currentUserProfile.value ?: return@withContext
        val updated = current.copy(
            fullName = fullName.trim(),
            city = city.trim(),
            timeZone = timeZone.trim(),
            status = status.trim(),
            bio = bio.trim(),
            avatarUrl = avatarUrl ?: current.avatarUrl
        )
        _currentUserProfile.value = updated
        saveLocalUserCache(updated, isCurrent = true)
        upsertUserProfileToSupabase(updated)

        _authState.value = AuthState.Authenticated(
            user = updated,
            partner = _partnerProfile.value,
            couple = _coupleInfo.value
        )
    }

    // --- Couple Pairing System ---
    suspend fun linkCoupleWithCode(code: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val current = _currentUserProfile.value ?: return@withContext Result.failure(Exception("No autenticado"))
        val cleanCode = code.trim().uppercase()

        val couplesRes = supabase.getTable("couples", "code=eq.$cleanCode&select=*")
        if (couplesRes.isFailure) {
            return@withContext Result.failure(Exception("Código de pareja no encontrado"))
        }

        val array = couplesRes.getOrNull() ?: JSONArray()
        if (array.length() == 0) {
            return@withContext Result.failure(Exception("El código no existe"))
        }

        val coupleObj = array.getJSONObject(0)
        val coupleId = coupleObj.optString("id")

        // Add member to couple_members
        val memberJson = JSONObject().apply {
            put("couple_id", coupleId)
            put("user_id", current.id)
        }
        supabase.insertRow("couple_members", memberJson)

        // Update profile couple_id
        val updatedProfile = current.copy(coupleId = coupleId)
        _currentUserProfile.value = updatedProfile
        upsertUserProfileToSupabase(updatedProfile)

        loadCoupleAndPartner(updatedProfile)
        syncCoupleData()
        Result.success(true)
    }

    suspend fun createCoupleInviteCode(): Result<String> = withContext(Dispatchers.IO) {
        val current = _currentUserProfile.value ?: return@withContext Result.failure(Exception("No autenticado"))
        val code = "LOVE-" + (1000..9999).random()

        val coupleJson = JSONObject().apply {
            put("code", code)
            put("created_by", current.id)
        }

        val insertRes = supabase.insertRow("couples", coupleJson)
        if (insertRes.isFailure) {
            return@withContext Result.failure(Exception("No se pudo crear la pareja en la base de datos"))
        }

        val coupleArray = insertRes.getOrNull() ?: JSONArray()
        val coupleId = if (coupleArray.length() > 0) coupleArray.getJSONObject(0).optString("id", UUID.randomUUID().toString()) else UUID.randomUUID().toString()

        val memberJson = JSONObject().apply {
            put("couple_id", coupleId)
            put("user_id", current.id)
        }
        supabase.insertRow("couple_members", memberJson)

        val updatedProfile = current.copy(coupleId = coupleId)
        _currentUserProfile.value = updatedProfile
        upsertUserProfileToSupabase(updatedProfile)

        _coupleInfo.value = CoupleInfo(id = coupleId, code = code, member1Id = current.id)
        _authState.value = AuthState.Authenticated(updatedProfile, _partnerProfile.value, _coupleInfo.value)

        Result.success(code)
    }

    // --- Photo & Storage Uploads ---
    suspend fun uploadPhotoToSupabase(uri: Uri, bucket: String = "media"): String = withContext(Dispatchers.IO) {
        try {
            val mediaDir = File(context.filesDir, "media").apply { if (!exists()) mkdirs() }
            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val destFile = File(mediaDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val remoteRes = supabase.uploadStorageFile(bucket, fileName, destFile)
            if (remoteRes.isSuccess) {
                remoteRes.getOrNull() ?: destFile.absolutePath
            } else {
                destFile.absolutePath
            }
        } catch (e: Exception) {
            uri.toString()
        }
    }

    // --- Feed & Posts ---
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()

    suspend fun createPost(
        imageUri: Uri,
        caption: String,
        location: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value ?: return@withContext Result.failure(Exception("No autenticado"))
        val uploadedUrl = uploadPhotoToSupabase(imageUri, "media")
        val postId = UUID.randomUUID().toString()

        val post = PostEntity(
            id = postId,
            authorId = user.id,
            coupleId = user.coupleId,
            authorName = user.fullName,
            authorAvatar = user.avatarUrl,
            imageUri = uploadedUrl,
            caption = caption.trim(),
            location = location.ifBlank { user.city },
            timeAgo = "Ahora",
            createdAt = System.currentTimeMillis()
        )
        postDao.insertPost(post)

        try {
            val postJson = JSONObject().apply {
                put("id", postId)
                put("author_id", user.id)
                if (user.coupleId != null) put("couple_id", user.coupleId)
                put("author_name", user.fullName)
                put("author_avatar", user.avatarUrl)
                put("image_url", uploadedUrl)
                put("caption", caption.trim())
                put("location", location)
                put("created_at", System.currentTimeMillis())
            }
            supabase.insertRow("posts", postJson)
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote post insert: ${e.message}")
        }
        Result.success(true)
    }

    fun isPostLiked(postId: String): Flow<Boolean> {
        val user = _currentUserProfile.value
        return if (user != null) postDao.isPostLikedByUser(postId, user.id) else flowOf(false)
    }

    fun getLikeCount(postId: String): Flow<Int> = postDao.getLikeCountForPost(postId)

    fun getCommentsForPost(postId: String): Flow<List<PostCommentEntity>> = postDao.getCommentsForPost(postId)

    suspend fun toggleLike(postId: String) = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value ?: return@withContext
        try {
            postDao.insertLike(PostLikeEntity(postId = postId, userId = user.id))
            supabase.insertRow("post_likes", JSONObject().apply {
                put("post_id", postId)
                put("user_id", user.id)
            })
        } catch (e: Exception) {
            postDao.deleteLike(postId, user.id)
            supabase.deleteRow("post_likes", "post_id=eq.$postId&user_id=eq.${user.id}")
        }
    }

    suspend fun addComment(postId: String, text: String): Long = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value ?: return@withContext 0L
        val comment = PostCommentEntity(
            postId = postId,
            authorId = user.id,
            authorName = user.fullName,
            authorAvatar = user.avatarUrl,
            text = text.trim()
        )
        val id = postDao.insertComment(comment)

        try {
            supabase.insertRow("post_comments", JSONObject().apply {
                put("post_id", postId)
                put("author_id", user.id)
                put("author_name", user.fullName)
                put("author_avatar", user.avatarUrl)
                put("text", text.trim())
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote comment note: ${e.message}")
        }
        id
    }

    // --- Notes ---
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun addNote(text: String): String = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value ?: return@withContext ""
        val noteId = UUID.randomUUID().toString()
        val note = NoteEntity(
            id = noteId,
            coupleId = user.coupleId,
            text = text.trim(),
            authorId = user.id,
            authorName = user.fullName,
            isDone = false
        )
        noteDao.insertNote(note)

        try {
            supabase.insertRow("notes", JSONObject().apply {
                put("id", noteId)
                if (user.coupleId != null) put("couple_id", user.coupleId)
                put("text", text.trim())
                put("author_id", user.id)
                put("author_name", user.fullName)
                put("is_done", false)
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote note insert: ${e.message}")
        }
        noteId
    }

    suspend fun toggleNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        val updated = note.copy(isDone = !note.isDone)
        noteDao.updateNote(updated)

        try {
            supabase.updateRow("notes", "id=eq.${note.id}", JSONObject().apply {
                put("is_done", updated.isDone)
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote note toggle: ${e.message}")
        }
    }

    suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
        try {
            supabase.deleteRow("notes", "id=eq.$id")
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote note delete: ${e.message}")
        }
    }

    // --- Daily Question & Chats ---
    fun getDailyAnswers(questionId: Int): Flow<List<DailyAnswerEntity>> = dailyDao.getAnswersForQuestion(questionId)

    fun getDailyChats(questionId: Int): Flow<List<DailyChatEntity>> = dailyDao.getDailyChats(questionId)

    suspend fun submitDailyAnswer(questionId: Int, answer: String) = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value ?: return@withContext
        dailyDao.insertAnswer(
            DailyAnswerEntity(
                questionId = questionId,
                coupleId = user.coupleId,
                userId = user.id,
                userName = user.fullName,
                answer = answer.trim()
            )
        )
        try {
            supabase.insertRow("daily_answers", JSONObject().apply {
                put("question_id", questionId)
                if (user.coupleId != null) put("couple_id", user.coupleId)
                put("user_id", user.id)
                put("user_name", user.fullName)
                put("answer", answer.trim())
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote answer insert: ${e.message}")
        }
    }

    suspend fun sendDailyChat(questionId: Int, text: String) = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value ?: return@withContext
        dailyDao.insertDailyChat(
            DailyChatEntity(
                questionId = questionId,
                coupleId = user.coupleId,
                senderId = user.id,
                senderName = user.fullName,
                text = text.trim()
            )
        )
        try {
            supabase.insertRow("daily_chats", JSONObject().apply {
                put("question_id", questionId)
                if (user.coupleId != null) put("couple_id", user.coupleId)
                put("sender_id", user.id)
                put("sender_name", user.fullName)
                put("text", text.trim())
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote daily chat: ${e.message}")
        }
    }

    // --- Challenges ---
    val allChallenges: Flow<List<ChallengeEntity>> = challengeDao.getAllChallenges()

    suspend fun toggleChallenge(challenge: ChallengeEntity, isMe: Boolean) = withContext(Dispatchers.IO) {
        val updated = if (isMe) challenge.copy(meDone = !challenge.meDone) else challenge.copy(partnerDone = !challenge.partnerDone)
        challengeDao.updateChallenge(updated)
    }

    // --- Secret Questions ---
    val allSecretQuestions: Flow<List<SecretQuestionEntity>> = secretDao.getAllSecretQuestions()

    suspend fun addSecretQuestion(text: String): String = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value ?: return@withContext ""
        val id = UUID.randomUUID().toString()
        secretDao.insertSecretQuestion(
            SecretQuestionEntity(
                id = id,
                coupleId = user.coupleId,
                authorId = user.id,
                authorName = user.fullName,
                questionText = text.trim(),
                isAnswered = false
            )
        )
        try {
            supabase.insertRow("secret_questions", JSONObject().apply {
                put("id", id)
                if (user.coupleId != null) put("couple_id", user.coupleId)
                put("author_id", user.id)
                put("author_name", user.fullName)
                put("question_text", text.trim())
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote secret question note: ${e.message}")
        }
        id
    }

    // --- Vault Settings & Reunion Date ---
    val vaultSettings: Flow<VaultSettingsEntity?> = vaultDao.getVaultSettings()

    suspend fun updateVaultPin(newPin: String) = withContext(Dispatchers.IO) {
        vaultDao.updatePinCode(newPin.trim())
    }

    suspend fun updateReunionDate(newDateMillis: Long) = withContext(Dispatchers.IO) {
        vaultDao.updateReunionDate(newDateMillis)
    }

    // --- Desire Match ---
    val allDesireVotes: Flow<List<DesireVoteEntity>> = desireDao.getAllDesireVotes()
    val matchedDesires: Flow<List<DesireVoteEntity>> = desireDao.getMatches()

    suspend fun voteDesire(cardId: Int, title: String, userYes: Boolean) = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value
        val partnerYes = cardId in listOf(1, 2, 4, 5) // Bilateral preference check
        val isMatch = userYes && partnerYes
        desireDao.insertOrUpdateVote(
            DesireVoteEntity(
                cardId = cardId,
                coupleId = user?.coupleId,
                title = title,
                userVotedYes = userYes,
                partnerVotedYes = partnerYes,
                isMatched = isMatch
            )
        )
    }

    // --- Helpers for Supabase Profile & Couple Resolution ---

    private suspend fun loadUserProfileFromSupabase(userId: String, fallbackEmail: String): UserProfile? {
        val res = supabase.getTable("profiles", "id=eq.$userId&select=*")
        if (res.isSuccess) {
            val array = res.getOrNull()
            if (array != null && array.length() > 0) {
                val obj = array.getJSONObject(0)
                return UserProfile(
                    id = obj.optString("id", userId),
                    email = obj.optString("email", fallbackEmail),
                    username = obj.optString("username", fallbackEmail.substringBefore("@")),
                    fullName = obj.optString("full_name", fallbackEmail.substringBefore("@")),
                    avatarUrl = obj.optString("avatar_url", "feed3"),
                    city = obj.optString("city", "Madrid"),
                    timeZone = obj.optString("timezone", "Europe/Madrid"),
                    weatherTemp = obj.optString("weather_temp", "22°"),
                    weatherIcon = obj.optString("weather_icon", "sun"),
                    batteryLevel = obj.optInt("battery_level", 85),
                    status = obj.optString("status", "Libre"),
                    bio = obj.optString("bio", ""),
                    coupleId = obj.optString("couple_id").takeIf { it.isNotBlank() }
                )
            }
        }
        return null
    }

    private suspend fun upsertUserProfileToSupabase(profile: UserProfile) {
        try {
            val json = JSONObject().apply {
                put("id", profile.id)
                put("email", profile.email)
                put("username", profile.username)
                put("full_name", profile.fullName)
                put("avatar_url", profile.avatarUrl)
                put("city", profile.city)
                put("timezone", profile.timeZone)
                put("status", profile.status)
                put("bio", profile.bio)
                if (profile.coupleId != null) put("couple_id", profile.coupleId)
            }
            supabase.upsertRow("profiles", json)
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Upsert profile note: ${e.message}")
        }
    }

    private suspend fun loadCoupleAndPartner(user: UserProfile) {
        val coupleId = user.coupleId ?: return
        try {
            val membersRes = supabase.getTable("couple_members", "couple_id=eq.$coupleId&select=*")
            if (membersRes.isSuccess) {
                val array = membersRes.getOrNull() ?: JSONArray()
                for (i in 0 until array.length()) {
                    val m = array.getJSONObject(i)
                    val memberUserId = m.optString("user_id")
                    if (memberUserId.isNotBlank() && memberUserId != user.id) {
                        val p = loadUserProfileFromSupabase(memberUserId, "")
                        if (p != null) {
                            _partnerProfile.value = p
                            saveLocalUserCache(p, isCurrent = false)
                        }
                    }
                }
            }

            val coupleRes = supabase.getTable("couples", "id=eq.$coupleId&select=*")
            if (coupleRes.isSuccess) {
                val cArray = coupleRes.getOrNull() ?: JSONArray()
                if (cArray.length() > 0) {
                    val cObj = cArray.getJSONObject(0)
                    _coupleInfo.value = CoupleInfo(
                        id = coupleId,
                        code = cObj.optString("code", "LOVE-0000"),
                        member1Id = user.id
                    )
                }
            }
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "loadCoupleAndPartner note: ${e.message}")
        }
    }

    private suspend fun saveLocalUserCache(profile: UserProfile, isCurrent: Boolean) {
        val entity = UserEntity(
            id = profile.id,
            email = profile.email,
            username = profile.username,
            fullName = profile.fullName,
            avatarUrl = profile.avatarUrl,
            city = profile.city,
            timeZone = profile.timeZone,
            weatherTemp = profile.weatherTemp,
            weatherIcon = profile.weatherIcon,
            batteryLevel = profile.batteryLevel,
            status = profile.status,
            bio = profile.bio,
            coupleId = profile.coupleId,
            partnerId = profile.partnerId,
            isCurrentUser = isCurrent
        )
        userDao.insertOrUpdate(entity)
    }

    // --- Sync All Couple Cloud Data ---
    suspend fun syncCoupleData() = withContext(Dispatchers.IO) {
        val user = _currentUserProfile.value ?: return@withContext
        try {
            val coupleQuery = if (user.coupleId != null) "couple_id=eq.${user.coupleId}&" else ""

            // 1. Sync Posts
            val postsRes = supabase.getTable("posts", "${coupleQuery}select=*&order=created_at.desc")
            if (postsRes.isSuccess) {
                val pArray = postsRes.getOrNull() ?: JSONArray()
                val list = mutableListOf<PostEntity>()
                for (i in 0 until pArray.length()) {
                    val obj = pArray.getJSONObject(i)
                    list.add(
                        PostEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            authorId = obj.optString("author_id", user.id),
                            coupleId = obj.optString("couple_id"),
                            authorName = obj.optString("author_name", "Pareja"),
                            authorAvatar = obj.optString("author_avatar", "feed2"),
                            imageUri = obj.optString("image_url", "feed1"),
                            caption = obj.optString("caption", ""),
                            location = obj.optString("location", "Tokio"),
                            createdAt = obj.optLong("created_at", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) postDao.insertAll(list)
            }

            // 2. Sync Notes
            val notesRes = supabase.getTable("notes", "${coupleQuery}select=*&order=created_at.desc")
            if (notesRes.isSuccess) {
                val nArray = notesRes.getOrNull() ?: JSONArray()
                val nList = mutableListOf<NoteEntity>()
                for (i in 0 until nArray.length()) {
                    val obj = nArray.getJSONObject(i)
                    nList.add(
                        NoteEntity(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            coupleId = obj.optString("couple_id"),
                            text = obj.optString("text", ""),
                            authorId = obj.optString("author_id", user.id),
                            authorName = obj.optString("author_name", "Pareja"),
                            isDone = obj.optBoolean("is_done", false),
                            createdAt = obj.optLong("created_at", System.currentTimeMillis())
                        )
                    )
                }
                if (nList.isNotEmpty()) noteDao.insertAll(nList)
            }

            // 3. Sync Daily Answers
            val answersRes = supabase.getTable("daily_answers", "${coupleQuery}select=*")
            if (answersRes.isSuccess) {
                val aArray = answersRes.getOrNull() ?: JSONArray()
                val aList = mutableListOf<DailyAnswerEntity>()
                for (i in 0 until aArray.length()) {
                    val obj = aArray.getJSONObject(i)
                    aList.add(
                        DailyAnswerEntity(
                            questionId = obj.optInt("question_id", 1),
                            coupleId = obj.optString("couple_id"),
                            userId = obj.optString("user_id"),
                            userName = obj.optString("user_name", "Pareja"),
                            answer = obj.optString("answer", ""),
                            answeredAt = obj.optLong("answered_at", System.currentTimeMillis())
                        )
                    )
                }
                if (aList.isNotEmpty()) dailyDao.insertAllAnswers(aList)
            }
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Sync couple data note: ${e.message}")
        }
    }
}
