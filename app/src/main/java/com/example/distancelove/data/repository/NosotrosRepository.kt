package com.example.distancelove.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.distancelove.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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

    // --- Authentication & User Operations ---
    val currentUserFlow: Flow<UserEntity?> = userDao.getCurrentUserFlow()
    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getCurrentUser(): UserEntity? = userDao.getCurrentUser()

    suspend fun login(email: String, password: String):Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        if (user == null) {
            return@withContext Result.failure(Exception("No existe ninguna cuenta con este correo electrónico"))
        }
        if (user.passwordHash != password) {
            return@withContext Result.failure(Exception("Contraseña incorrecta"))
        }
        userDao.clearCurrentSessions()
        userDao.setCurrentSession(user.id)
        Result.success(user)
    }

    suspend fun register(
        email: String,
        password: String,
        username: String,
        fullName: String,
        city: String,
        timeZone: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return@withContext Result.failure(Exception("Ya existe una cuenta con este correo electrónico"))
        }
        userDao.clearCurrentSessions()
        val newUser = UserEntity(
            email = email.trim().lowercase(),
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
        Result.success(saved)
    }

    suspend fun switchUser(userId: Long) = withContext(Dispatchers.IO) {
        userDao.clearCurrentSessions()
        userDao.setCurrentSession(userId)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.clearCurrentSessions()
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
            destFile.absolutePath
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
        postDao.insertPost(post)
    }

    fun getCommentsForPost(postId: Long): Flow<List<PostCommentEntity>> = postDao.getCommentsForPost(postId)

    fun isPostLiked(postId: Long, userId: Long): Flow<Boolean> = postDao.isPostLikedByUser(postId, userId)

    fun getLikeCount(postId: Long): Flow<Int> = postDao.getLikeCountForPost(postId)

    suspend fun toggleLike(postId: Long, userId: Long) = withContext(Dispatchers.IO) {
        val count = postDao.getPostById(postId) ?: return@withContext
        try {
            postDao.insertLike(PostLikeEntity(postId = postId, userId = userId))
        } catch (e: Exception) {
            postDao.deleteLike(postId, userId)
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
        postDao.insertComment(comment)
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
        noteDao.insertNote(note)
    }

    suspend fun toggleNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.copy(isDone = !note.isDone))
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
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
        val partnerYes = cardId in listOf(1, 2, 4, 5) // partner's pre-saved preference
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

    // --- Vault Settings ---
    val vaultSettings: Flow<VaultSettingsEntity?> = vaultDao.getVaultSettings()

    suspend fun updateVaultPin(newPin: String) = withContext(Dispatchers.IO) {
        vaultDao.saveVaultSettings(VaultSettingsEntity(id = 1, pinCode = newPin))
    }
}
