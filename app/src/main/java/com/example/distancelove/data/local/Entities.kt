package com.example.distancelove.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String,
    val city: String,
    val timeZone: String,
    val weatherTemp: String,
    val weatherIcon: String,
    val batteryLevel: Int,
    val status: String,
    val bio: String,
    val isCurrentSession: Boolean = false,
    val partnerId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String,
    val imageUri: String,
    val caption: String,
    val location: String,
    val timeAgo: String,
    val voiceSeconds: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "post_likes",
    indices = [Index(value = ["postId", "userId"], unique = true)]
)
data class PostLikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "post_comments")
data class PostCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long,
    val receiverId: Long,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val authorId: Long,
    val authorName: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_answers",
    indices = [Index(value = ["questionId", "userId"], unique = true)]
)
data class DailyAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Int,
    val userId: Long,
    val userName: String,
    val answer: String,
    val answeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_chats")
data class DailyChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Int,
    val senderId: Long,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val desc: String,
    val iconName: String,
    val meDone: Boolean,
    val partnerDone: Boolean
)

@Entity(tableName = "secret_questions")
data class SecretQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: Long,
    val authorName: String,
    val questionText: String,
    val isAnswered: Boolean = false,
    val answerText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "desire_votes")
data class DesireVoteEntity(
    @PrimaryKey val cardId: Int,
    val title: String,
    val userVotedYes: Boolean,
    val partnerVotedYes: Boolean,
    val isMatched: Boolean
)

@Entity(tableName = "voice_memories")
data class VoiceMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val authorName: String,
    val duration: String,
    val fileUri: String = "",
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_settings")
data class VaultSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val pinCode: String = "1402",
    val biometricsEnabled: Boolean = true
)
