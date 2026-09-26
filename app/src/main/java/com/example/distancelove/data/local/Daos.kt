package com.example.distancelove.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM profiles_cache WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM profiles_cache WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM profiles_cache WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM profiles_cache WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM profiles_cache WHERE id != :currentUserId AND (coupleId = :coupleId OR coupleId IS NOT NULL) LIMIT 1")
    fun getPartnerFlow(currentUserId: String, coupleId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("UPDATE profiles_cache SET isCurrentUser = 0")
    suspend fun clearCurrentUserFlag()

    @Query("DELETE FROM profiles_cache")
    suspend fun clearAll()
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts_cache ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Query("DELETE FROM posts_cache WHERE id = :id")
    suspend fun deletePostById(id: String)

    @Query("DELETE FROM posts_cache")
    suspend fun clearAll()

    // Likes
    @Query("SELECT COUNT(*) FROM post_likes_cache WHERE postId = :postId")
    fun getLikeCountForPost(postId: String): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM post_likes_cache WHERE postId = :postId AND userId = :userId)")
    fun isPostLikedByUser(postId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: PostLikeEntity)

    @Query("DELETE FROM post_likes_cache WHERE postId = :postId AND userId = :userId")
    suspend fun deleteLike(postId: String, userId: String)

    // Comments
    @Query("SELECT * FROM post_comments_cache WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: String): Flow<List<PostCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PostCommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllComments(comments: List<PostCommentEntity>)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes_cache ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes_cache WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("DELETE FROM notes_cache")
    suspend fun clearAll()
}

@Dao
interface DailyDao {
    @Query("SELECT * FROM daily_answers_cache WHERE questionId = :questionId")
    fun getAnswersForQuestion(questionId: Int): Flow<List<DailyAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: DailyAnswerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnswers(answers: List<DailyAnswerEntity>)

    @Query("SELECT * FROM daily_chats_cache WHERE questionId = :questionId ORDER BY timestamp ASC")
    fun getDailyChats(questionId: Int): Flow<List<DailyChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyChat(chat: DailyChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChats(chats: List<DailyChatEntity>)

    @Query("DELETE FROM daily_answers_cache")
    suspend fun clearAll()
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges_cache ORDER BY id ASC")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)
}

@Dao
interface SecretQuestionDao {
    @Query("SELECT * FROM secret_questions_cache ORDER BY createdAt DESC")
    fun getAllSecretQuestions(): Flow<List<SecretQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecretQuestion(question: SecretQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<SecretQuestionEntity>)
}

@Dao
interface DesireDao {
    @Query("SELECT * FROM desire_votes_cache ORDER BY cardId ASC")
    fun getAllDesireVotes(): Flow<List<DesireVoteEntity>>

    @Query("SELECT * FROM desire_votes_cache WHERE isMatched = 1")
    fun getMatches(): Flow<List<DesireVoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(votes: List<DesireVoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVote(vote: DesireVoteEntity)
}

@Dao
interface VaultSettingsDao {
    @Query("SELECT * FROM vault_settings WHERE id = 1 LIMIT 1")
    fun getVaultSettings(): Flow<VaultSettingsEntity?>

    @Query("SELECT * FROM vault_settings WHERE id = 1 LIMIT 1")
    suspend fun getVaultSettingsDirect(): VaultSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVaultSettings(settings: VaultSettingsEntity)

    @Query("UPDATE vault_settings SET pinCode = :newPin WHERE id = 1")
    suspend fun updatePinCode(newPin: String)

    @Query("UPDATE vault_settings SET reunionDateMillis = :newDateMillis WHERE id = 1")
    suspend fun updateReunionDate(newDateMillis: Long)
}
