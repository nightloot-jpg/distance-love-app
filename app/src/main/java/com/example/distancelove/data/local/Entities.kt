package com.example.distancelove.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profiles_cache",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String,
    val city: String,
    val timeZone: String,
    val weatherTemp: String = "22°",
    val weatherIcon: String = "sun",
    val batteryLevel: Int = 85,
    val status: String = "Libre",
    val bio: String = "",
    val coupleId: String? = null,
    val partnerId: String? = null,
    val isCurrentUser: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts_cache")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val coupleId: String? = null,
    val authorName: String,
    val authorAvatar: String,
    val imageUri: String,
    val caption: String,
    val location: String,
    val timeAgo: String = "Ahora",
    val voiceSeconds: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "post_likes_cache",
    indices = [Index(value = ["postId", "userId"], unique = true)]
)
data class PostLikeEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val postId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "post_comments_cache")
data class PostCommentEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val remoteId: String? = null,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes_cache")
data class NoteEntity(
    @PrimaryKey val id: String,
    val coupleId: String? = null,
    val text: String,
    val authorId: String,
    val authorName: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_answers_cache",
    indices = [Index(value = ["questionId", "userId"], unique = true)]
)
data class DailyAnswerEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val questionId: Int,
    val coupleId: String? = null,
    val userId: String,
    val userName: String,
    val answer: String,
    val answeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_chats_cache")
data class DailyChatEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val questionId: Int,
    val coupleId: String? = null,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "challenges_cache")
data class ChallengeEntity(
    @PrimaryKey val id: Int,
    val coupleId: String? = null,
    val title: String,
    val desc: String,
    val iconName: String,
    val meDone: Boolean = false,
    val partnerDone: Boolean = false
)

@Entity(tableName = "secret_questions_cache")
data class SecretQuestionEntity(
    @PrimaryKey val id: String,
    val coupleId: String? = null,
    val authorId: String,
    val authorName: String,
    val questionText: String,
    val isAnswered: Boolean = false,
    val answerText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "desire_votes_cache")
data class DesireVoteEntity(
    @PrimaryKey val cardId: Int,
    val coupleId: String? = null,
    val title: String,
    val userVotedYes: Boolean = false,
    val partnerVotedYes: Boolean = false,
    val isMatched: Boolean = false
)

@Entity(tableName = "vault_settings")
data class VaultSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val pinCode: String = "1402",
    val biometricsEnabled: Boolean = true,
    val reunionDateMillis: Long = 1792231200000L
)
