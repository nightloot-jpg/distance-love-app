package com.example.distancelove.data

data class CountdownTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

data class PersonProfile(
    val name: String,
    val city: String,
    val timeZone: String,
    val temp: String,
    val weatherType: WeatherType,
    val battery: Int,
    val status: String,
    val isOnline: Boolean
)

enum class WeatherType {
    SUN, RAIN, CLOUD
}

data class SharedNote(
    val id: Long,
    val text: String,
    val by: String,
    val isDone: Boolean
)

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val by: String,
    val text: String
)

data class DailyQuestionState(
    val question: String,
    val partnerResponse: String,
    val userResponse: String = "",
    val isRevealed: Boolean = false,
    val chat: List<ChatMessage> = listOf(
        ChatMessage(1L, "Yuki", "Jajaja sabía que dirías eso 🥹")
    )
)

data class CoupleChallenge(
    val id: Int,
    val title: String,
    val desc: String,
    val iconName: String,
    val isMeDone: Boolean,
    val isPartnerDone: Boolean
)

data class SecretQuestion(
    val id: Long,
    val question: String,
    val from: String,
    val isAnswered: Boolean,
    val answer: String = ""
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

data class CommentItem(
    val id: Long = System.currentTimeMillis(),
    val by: String,
    val text: String
)

data class FeedPost(
    val id: Long,
    val by: String,
    val imageResName: String,
    val text: String,
    val place: String,
    val timeAgo: String,
    val voiceSeconds: Int? = null,
    val isLiked: Boolean,
    val comments: List<CommentItem> = emptyList()
)

data class StoryItem(
    val name: String,
    val imageResName: String
)

data class DesireCard(
    val id: Int,
    val text: String,
    val partnerLiked: Boolean
)

data class VoiceMemory(
    val id: Int,
    val title: String,
    val by: String,
    val duration: String
)
