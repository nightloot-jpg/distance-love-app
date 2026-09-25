package com.example.distancelove.data

sealed interface AuthState {
    object Loading : AuthState
    data class Authenticated(
        val user: UserProfile,
        val partner: UserProfile?,
        val couple: CoupleInfo?
    ) : AuthState
    object Unauthenticated : AuthState
    data class Error(val message: String) : AuthState
}

data class UserProfile(
    val id: String,
    val email: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String = "feed3",
    val city: String = "Madrid",
    val timeZone: String = "Europe/Madrid",
    val weatherTemp: String = "22°",
    val weatherIcon: String = "sun",
    val batteryLevel: Int = 80,
    val status: String = "Libre",
    val bio: String = "",
    val coupleId: String? = null,
    val partnerId: String? = null
)

data class CoupleInfo(
    val id: String,
    val code: String,
    val member1Id: String,
    val member2Id: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class CountdownTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val by: String,
    val text: String
)

data class CinemaVideo(
    val title: String,
    val url: String,
    val isYouTube: Boolean = false
)

data class CinemaFloater(
    val id: Long,
    val emoji: String,
    val xPercent: Float
)

data class StoryItem(
    val name: String,
    val imageResName: String
)

data class DesireCard(
    val id: Int,
    val text: String,
    val partnerLiked: Boolean = false
)

data class VoiceMemoryItem(
    val id: Int,
    val title: String,
    val authorName: String,
    val duration: String
)
