package com.example.distancelove.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.distancelove.data.local.*
import com.example.distancelove.data.remote.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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
    val messageDao = database.messageDao()
    val noteDao = database.noteDao()
    val dailyDao = database.dailyDao()
    val challengeDao = database.challengeDao()
    val secretDao = database.secretQuestionDao()
    val desireDao = database.desireDao()
    val voiceDao = database.voiceMemoryDao()
    val vaultDao = database.vaultSettingsDao()

    val supabase = SupabaseService()

    // --- Authentication & User Operations ---
    val currentUserFlow: Flow<UserEntity?> = userDao.getCurrentUserFlow()
    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getCurrentUser(): UserEntity? = userDao.getCurrentUser()

    suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()

        // 1. Try Supabase Auth
        val supabaseResult = supabase.signIn(cleanEmail, password)
        if (supabaseResult.isSuccess) {
            val json = supabaseResult.getOrNull()
            val userObj = json?.optJSONObject("user")
            val meta = userObj?.optJSONObject("user_metadata")
            val fullName = meta?.optString("full_name") ?: cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

            var localUser = userDao.getUserByEmail(cleanEmail)
            if (localUser == null) {
                localUser = UserEntity(
                    email = cleanEmail,
                    passwordHash = password,
                    username = cleanEmail.substringBefore("@"),
                    fullName = fullName,
                    avatarUrl = "feed3",
                    city = meta?.optString("city", "Madrid") ?: "Madrid",
                    timeZone = "Europe/Madrid",
                    weatherTemp = "22°",
                    weatherIcon = "sun",
                    batteryLevel = 80,
                    status = "Libre",
                    bio = "Conectados a través de Supabase 💕",
                    isCurrentSession = true
                )
                val id = userDao.insertUser(localUser)
                localUser = localUser.copy(id = id)
            } else {
                userDao.clearCurrentSessions()
                userDao.setCurrentSession(localUser.id)
            }
            syncWithSupabase()
            return@withContext Result.success(localUser)
        }

        // 2. Fallback to Local SQLite DB
        val localUser = userDao.getUserByEmail(cleanEmail)
        if (localUser == null) {
            return@withContext Result.failure(Exception("No existe ninguna cuenta con este correo electrónico"))
        }
        if (localUser.passwordHash != password) {
            return@withContext Result.failure(Exception("Contraseña incorrecta"))
        }
        userDao.clearCurrentSessions()
        userDao.setCurrentSession(localUser.id)
        syncWithSupabase()
        Result.success(localUser)
    }

    suspend fun register(
        email: String,
        password: String,
        username: String,
        fullName: String,
        city: String,
        timeZone: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()

        val meta = JSONObject().apply {
            put("full_name", fullName.trim())
            put("username", username.trim())
            put("city", city.trim())
            put("timezone", timeZone.trim())
        }

        // 1. Try Supabase Auth SignUp
        supabase.signUp(cleanEmail, password, meta)

        // 2. Insert into Local DB & Set Active Session
        userDao.clearCurrentSessions()
        val newUser = UserEntity(
            email = cleanEmail,
            passwordHash = password,
            username = username.trim().lowercase(),
            fullName = fullName.trim(),
            avatarUrl = "feed3",
            city = city.ifBlank { "Madrid" },
            timeZone = timeZone.ifBlank { "Europe/Madrid" },
            weatherTemp = "21°",
            weatherIcon = "sun",
            batteryLevel = 85,
            status = "Libre",
            bio = "Juntos a pesar de la distancia 💕",
            isCurrentSession = true
        )
        val id = userDao.insertUser(newUser)
        val saved = newUser.copy(id = id)

        // Sync to Supabase profiles table
        try {
            val profileJson = JSONObject().apply {
                put("id", saved.id.toString())
                put("email", saved.email)
                put("full_name", saved.fullName)
                put("username", saved.username)
                put("city", saved.city)
                put("status", saved.status)
                put("bio", saved.bio)
            }
            supabase.insertRow("profiles", profileJson)
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Profiles sync note: ${e.message}")
        }

        syncWithSupabase()
        Result.success(saved)
    }

    suspend fun switchUser(userId: Long) = withContext(Dispatchers.IO) {
        userDao.clearCurrentSessions()
        userDao.setCurrentSession(userId)
        syncWithSupabase()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.clearCurrentSessions()
        supabase.setAuthToken(null)
    }

    suspend fun updateProfile(
        userId: Long,
        fullName: String,
        city: String,
        timeZone: String,
        status: String,
        bio: String,
        avatarUri: String? = null
    ) = withContext(Dispatchers.IO) {
        val current = userDao.getUserById(userId) ?: return@withContext
        val updated = current.copy(
            fullName = fullName.trim(),
            city = city.trim(),
            timeZone = timeZone.trim(),
            status = status.trim(),
            bio = bio.trim(),
            avatarUrl = avatarUri ?: current.avatarUrl
        )
        userDao.updateUser(updated)

        // Remote Supabase sync
        try {
            val json = JSONObject().apply {
                put("full_name", updated.fullName)
                put("city", updated.city)
                put("status", updated.status)
                put("bio", updated.bio)
                put("avatar_url", updated.avatarUrl)
            }
            supabase.updateRow("profiles", "email=eq.${updated.email}", json)
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote profile update: ${e.message}")
        }
    }

    // --- File & Photo Storage ---
    suspend fun saveImageToInternalStorage(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val mediaDir = File(context.filesDir, "media").apply { if (!exists()) mkdirs() }
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val destFile = File(mediaDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Also upload to Supabase Storage if available
            val remoteUpload = supabase.uploadStorageFile("media", fileName, destFile)
            if (remoteUpload.isSuccess) {
                remoteUpload.getOrNull() ?: destFile.absolutePath
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
        author: UserEntity,
        imageUri: String,
        caption: String,
        location: String,
        voiceSeconds: Int? = null
    ): Long = withContext(Dispatchers.IO) {
        val post = PostEntity(
            authorId = author.id,
            authorName = author.fullName,
            authorAvatar = author.avatarUrl,
            imageUri = imageUri,
            caption = caption.trim(),
            location = location.ifBlank { author.city },
            timeAgo = "Ahora",
            voiceSeconds = voiceSeconds,
            createdAt = System.currentTimeMillis()
        )
        val id = postDao.insertPost(post)

        // Sync to Supabase
        try {
            val postJson = JSONObject().apply {
                put("author_id", author.id.toString())
                put("author_name", author.fullName)
                put("author_avatar", author.avatarUrl)
                put("image_url", imageUri)
                put("caption", caption)
                put("location", location)
                put("created_at", System.currentTimeMillis())
            }
            supabase.insertRow("posts", postJson)
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote post insert note: ${e.message}")
        }

        id
    }

    fun getCommentsForPost(postId: Long): Flow<List<PostCommentEntity>> = postDao.getCommentsForPost(postId)

    fun isPostLiked(postId: Long, userId: Long): Flow<Boolean> = postDao.isPostLikedByUser(postId, userId)

    fun getLikeCount(postId: Long): Flow<Int> = postDao.getLikeCountForPost(postId)

    suspend fun toggleLike(postId: Long, userId: Long) = withContext(Dispatchers.IO) {
        try {
            postDao.insertLike(PostLikeEntity(postId = postId, userId = userId))
            supabase.insertRow("post_likes", JSONObject().apply {
                put("post_id", postId)
                put("user_id", userId)
            })
        } catch (e: Exception) {
            postDao.deleteLike(postId, userId)
            supabase.deleteRow("post_likes", "post_id=eq.$postId&user_id=eq.$userId")
        }
    }

    suspend fun addComment(postId: Long, user: UserEntity, text: String): Long = withContext(Dispatchers.IO) {
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
                put("author_id", user.id.toString())
                put("author_name", user.fullName)
                put("author_avatar", user.avatarUrl)
                put("text", text.trim())
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote comment insert note: ${e.message}")
        }

        id
    }

    // --- Notes ---
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun addNote(user: UserEntity, text: String): Long = withContext(Dispatchers.IO) {
        val note = NoteEntity(
            text = text.trim(),
            authorId = user.id,
            authorName = user.fullName,
            isDone = false
        )
        val id = noteDao.insertNote(note)

        try {
            supabase.insertRow("notes", JSONObject().apply {
                put("text", text.trim())
                put("author_name", user.fullName)
                put("is_done", false)
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote note insert note: ${e.message}")
        }

        id
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

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
        try {
            supabase.deleteRow("notes", "id=eq.$id")
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote note delete: ${e.message}")
        }
    }

    // --- Daily Question ---
    fun getDailyAnswers(questionId: Int): Flow<List<DailyAnswerEntity>> = dailyDao.getAnswersForQuestion(questionId)

    fun getUserDailyAnswer(questionId: Int, userId: Long): Flow<DailyAnswerEntity?> = dailyDao.getUserAnswer(questionId, userId)

    fun getDailyChats(questionId: Int): Flow<List<DailyChatEntity>> = dailyDao.getDailyChats(questionId)

    suspend fun submitDailyAnswer(questionId: Int, user: UserEntity, answer: String) = withContext(Dispatchers.IO) {
        dailyDao.insertAnswer(
            DailyAnswerEntity(
                questionId = questionId,
                userId = user.id,
                userName = user.fullName,
                answer = answer.trim()
            )
        )
        try {
            supabase.insertRow("daily_answers", JSONObject().apply {
                put("question_id", questionId)
                put("user_id", user.id.toString())
                put("user_name", user.fullName)
                put("answer", answer.trim())
            })
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Remote answer insert: ${e.message}")
        }
    }

    suspend fun sendDailyChat(questionId: Int, user: UserEntity, text: String) = withContext(Dispatchers.IO) {
        dailyDao.insertDailyChat(
            DailyChatEntity(
                questionId = questionId,
                senderId = user.id,
                senderName = user.fullName,
                text = text.trim()
            )
        )
        try {
            supabase.insertRow("daily_chats", JSONObject().apply {
                put("question_id", questionId)
                put("sender_id", user.id.toString())
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
        val updated = if (isMe) {
            challenge.copy(meDone = !challenge.meDone)
        } else {
            challenge.copy(partnerDone = !challenge.partnerDone)
        }
        challengeDao.updateChallenge(updated)
    }

    // --- Secret Questions ---
    val allSecretQuestions: Flow<List<SecretQuestionEntity>> = secretDao.getAllSecretQuestions()

    suspend fun addSecretQuestion(user: UserEntity, text: String): Long = withContext(Dispatchers.IO) {
        secretDao.insertSecretQuestion(
            SecretQuestionEntity(
                authorId = user.id,
                authorName = user.fullName,
                questionText = text.trim(),
                isAnswered = false
            )
        )
    }

    // --- Desire Match ---
    val allDesireVotes: Flow<List<DesireVoteEntity>> = desireDao.getAllDesireVotes()
    val matchedDesires: Flow<List<DesireVoteEntity>> = desireDao.getMatches()

    suspend fun voteDesire(cardId: Int, title: String, userYes: Boolean) = withContext(Dispatchers.IO) {
        val partnerYes = cardId in listOf(1, 2, 4, 5)
        val isMatch = userYes && partnerYes
        desireDao.insertOrUpdateVote(
            DesireVoteEntity(
                cardId = cardId,
                title = title,
                userVotedYes = userYes,
                partnerVotedYes = partnerYes,
                isMatched = isMatch
            )
        )
    }

    // --- Supabase Cloud Sync ---
    suspend fun syncWithSupabase() = withContext(Dispatchers.IO) {
        try {
            // Pull remote notes if available
            val notesResult = supabase.getTable("notes")
            if (notesResult.isSuccess) {
                val array = notesResult.getOrNull()
                if (array != null && array.length() > 0) {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val text = obj.optString("text")
                        val author = obj.optString("author_name", "Pareja")
                        val isDone = obj.optBoolean("is_done", false)
                        if (text.isNotBlank()) {
                            noteDao.insertNote(NoteEntity(text = text, authorId = 0L, authorName = author, isDone = isDone))
                        }
                    }
                }
            }

            // Pull remote posts if available
            val postsResult = supabase.getTable("posts", "select=*&order=created_at.desc")
            if (postsResult.isSuccess) {
                val array = postsResult.getOrNull()
                if (array != null && array.length() > 0) {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val caption = obj.optString("caption")
                        val imgUrl = obj.optString("image_url")
                        val authorName = obj.optString("author_name", "Yuki")
                        val authorAvatar = obj.optString("author_avatar", "feed2")
                        val location = obj.optString("location", "Tokio")
                        if (imgUrl.isNotBlank() || caption.isNotBlank()) {
                            postDao.insertPost(
                                PostEntity(
                                    authorId = 2L,
                                    authorName = authorName,
                                    authorAvatar = authorAvatar,
                                    imageUri = imgUrl,
                                    caption = caption,
                                    location = location,
                                    timeAgo = "Reciente",
                                    createdAt = obj.optLong("created_at", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("NosotrosRepo", "Sync check: ${e.message}")
        }
    }
}
