package com.example.distancelove.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isCurrentSession = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentSession = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isCurrentSession = 0")
    suspend fun clearCurrentSessions()

    @Query("UPDATE users SET isCurrentSession = 1 WHERE id = :userId")
    suspend fun setCurrentSession(userId: Long)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Delete
    suspend fun deletePost(post: PostEntity)

    // Likes
    @Query("SELECT * FROM post_likes WHERE postId = :postId")
    fun getLikesForPost(postId: Long): Flow<List<PostLikeEntity>>

    @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
    fun getLikeCountForPost(postId: Long): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM post_likes WHERE postId = :postId AND userId = :userId)")
    fun isPostLikedByUser(postId: Long, userId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: PostLikeEntity)

    @Query("DELETE FROM post_likes WHERE postId = :postId AND userId = :userId")
    suspend fun deleteLike(postId: Long, userId: Long)

    // Comments
    @Query("SELECT * FROM post_comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: Long): Flow<List<PostCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PostCommentEntity): Long
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :partnerId) OR (senderId = :partnerId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getConversationMessages(userId: Long, partnerId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :partnerId")
    suspend fun markMessagesAsRead(userId: Long, partnerId: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface DailyDao {
    @Query("SELECT * FROM daily_answers WHERE questionId = :questionId")
    fun getAnswersForQuestion(questionId: Int): Flow<List<DailyAnswerEntity>>

    @Query("SELECT * FROM daily_answers WHERE questionId = :questionId AND userId = :userId LIMIT 1")
    fun getUserAnswer(questionId: Int, userId: Long): Flow<DailyAnswerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: DailyAnswerEntity)

    @Query("SELECT * FROM daily_chats WHERE questionId = :questionId ORDER BY timestamp ASC")
    fun getDailyChats(questionId: Int): Flow<List<DailyChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyChat(chat: DailyChatEntity)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY id ASC")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)
}

@Dao
interface SecretQuestionDao {
    @Query("SELECT * FROM secret_questions ORDER BY createdAt DESC")
    fun getAllSecretQuestions(): Flow<List<SecretQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecretQuestion(question: SecretQuestionEntity): Long

    @Update
    suspend fun updateSecretQuestion(question: SecretQuestionEntity)
}

@Dao
interface DesireDao {
    @Query("SELECT * FROM desire_votes ORDER BY cardId ASC")
    fun getAllDesireVotes(): Flow<List<DesireVoteEntity>>

    @Query("SELECT * FROM desire_votes WHERE isMatched = 1")
    fun getMatches(): Flow<List<DesireVoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(votes: List<DesireVoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVote(vote: DesireVoteEntity)
}

@Dao
interface VoiceMemoryDao {
    @Query("SELECT * FROM voice_memories ORDER BY createdAt ASC")
    fun getAllVoiceMemories(): Flow<List<VoiceMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<VoiceMemoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: VoiceMemoryEntity): Long
}

@Dao
interface VaultSettingsDao {
    @Query("SELECT * FROM vault_settings WHERE id = 1 LIMIT 1")
    fun getVaultSettings(): Flow<VaultSettingsEntity?>

    @Query("SELECT * FROM vault_settings WHERE id = 1 LIMIT 1")
    suspend fun getVaultSettingsDirect(): VaultSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVaultSettings(settings: VaultSettingsEntity)
}
