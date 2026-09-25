package com.example.distancelove.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        PostLikeEntity::class,
        PostCommentEntity::class,
        MessageEntity::class,
        NoteEntity::class,
        DailyAnswerEntity::class,
        DailyChatEntity::class,
        ChallengeEntity::class,
        SecretQuestionEntity::class,
        DesireVoteEntity::class,
        VoiceMemoryEntity::class,
        VaultSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun messageDao(): MessageDao
    abstract fun noteDao(): NoteDao
    abstract fun dailyDao(): DailyDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun secretQuestionDao(): SecretQuestionDao
    abstract fun desireDao(): DesireDao
    abstract fun voiceMemoryDao(): VoiceMemoryDao
    abstract fun vaultSettingsDao(): VaultSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nosotros_app.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDatabase(database)
                    }
                }
            }
        }

        suspend fun populateInitialDatabase(db: AppDatabase) {
            val userDao = db.userDao()
            val postDao = db.postDao()
            val noteDao = db.noteDao()
            val challengeDao = db.challengeDao()
            val secretDao = db.secretQuestionDao()
            val desireDao = db.desireDao()
            val voiceDao = db.voiceMemoryDao()
            val vaultDao = db.vaultSettingsDao()

            // 1. Initial Users
            val albertoId = userDao.insertUser(
                UserEntity(
                    id = 1L,
                    email = "alberto@myentik.com",
                    passwordHash = "123456",
                    username = "alberto",
                    fullName = "Alberto",
                    avatarUrl = "feed3",
                    city = "Madrid",
                    timeZone = "Europe/Madrid",
                    weatherTemp = "22°",
                    weatherIcon = "sun",
                    batteryLevel = 78,
                    status = "Libre",
                    bio = "Contando los días para abrazarte en Tokio ✈️",
                    isCurrentSession = true,
                    partnerId = 2L
                )
            )

            val yukiId = userDao.insertUser(
                UserEntity(
                    id = 2L,
                    email = "yuki@myentik.com",
                    passwordHash = "123456",
                    username = "yuki",
                    fullName = "Yuki",
                    avatarUrl = "feed2",
                    city = "Tokio",
                    timeZone = "Asia/Tokyo",
                    weatherTemp = "19°",
                    weatherIcon = "rain",
                    batteryLevel = 23,
                    status = "Durmiendo",
                    bio = "Te extraño cada día bajo la lluvia de Shibuya 🌸",
                    isCurrentSession = false,
                    partnerId = 1L
                )
            )

            // 2. Initial Posts
            val p1 = postDao.insertPost(
                PostEntity(
                    id = 1L,
                    authorId = yukiId,
                    authorName = "Yuki",
                    authorAvatar = "feed2",
                    imageUri = "feed2",
                    caption = "Shibuya bajo la lluvia. Pensé en ti en cada paraguas.",
                    location = "Shibuya, Tokio",
                    timeAgo = "hace 2 h",
                    voiceSeconds = 14,
                    createdAt = System.currentTimeMillis() - 7200000
                )
            )
            postDao.insertLike(PostLikeEntity(postId = p1, userId = albertoId))
            postDao.insertComment(
                PostCommentEntity(
                    postId = p1,
                    authorId = albertoId,
                    authorName = "Alberto",
                    authorAvatar = "feed3",
                    text = "Quiero pasear ahí contigo 🌧️"
                )
            )

            val p2 = postDao.insertPost(
                PostEntity(
                    id = 2L,
                    authorId = albertoId,
                    authorName = "Alberto",
                    authorAvatar = "feed3",
                    imageUri = "feed3",
                    caption = "El atardecer de hoy te pertenece.",
                    location = "La Latina, Madrid",
                    timeAgo = "ayer",
                    createdAt = System.currentTimeMillis() - 86400000
                )
            )

            val p3 = postDao.insertPost(
                PostEntity(
                    id = 3L,
                    authorId = yukiId,
                    authorName = "Yuki",
                    authorAvatar = "feed1",
                    imageUri = "feed1",
                    caption = "Nuestro café favorito, versión a distancia ☕",
                    location = "Kioto",
                    timeAgo = "hace 3 días",
                    voiceSeconds = 22,
                    createdAt = System.currentTimeMillis() - 259200000
                )
            )
            postDao.insertLike(PostLikeEntity(postId = p3, userId = albertoId))

            // 3. Initial Notes
            noteDao.insertNote(NoteEntity(id = 1L, text = "Videollamada el domingo a las 11h (mi hora) 💛", authorId = yukiId, authorName = "Yuki", isDone = false))
            noteDao.insertNote(NoteEntity(id = 2L, text = "Comprar billetes de tren Kioto", authorId = albertoId, authorName = "Alberto", isDone = true))
            noteDao.insertNote(NoteEntity(id = 3L, text = "Mándame la receta del ramen", authorId = albertoId, authorName = "Alberto", isDone = false))

            // 4. Initial Partner Answer
            db.dailyDao().insertAnswer(
                DailyAnswerEntity(
                    questionId = 1,
                    userId = yukiId,
                    userName = "Yuki",
                    answer = "Cuando me mandaste el audio cantando a las 3 de la mañana solo para que me durmiera. Me hizo llorar de lo bonito."
                )
            )
            db.dailyDao().insertDailyChat(
                DailyChatEntity(
                    questionId = 1,
                    senderId = yukiId,
                    senderName = "Yuki",
                    text = "Jajaja sabía que dirías eso 🥹"
                )
            )

            // 5. Challenges
            challengeDao.insertAll(
                listOf(
                    ChallengeEntity(1, "Cocinar lo mismo", "Preparad la misma receta y cenad en videollamada.", "ChefHat", meDone = true, partnerDone = false),
                    ChallengeEntity(2, "Foto espontánea", "Mandad una foto de lo que veis ahora mismo, sin filtros.", "Camera", meDone = true, partnerDone = true),
                    ChallengeEntity(3, "Playlist cruzada", "Añadid 5 canciones que os recuerden al otro.", "Music", meDone = false, partnerDone = true),
                    ChallengeEntity(4, "Misma luna", "Fotografiad la luna desde vuestra ciudad esta noche.", "Moon", meDone = false, partnerDone = false)
                )
            )

            // 6. Secret Questions
            secretDao.insertSecretQuestion(
                SecretQuestionEntity(
                    id = 1L,
                    authorId = yukiId,
                    authorName = "Yuki",
                    questionText = "¿Cuál fue el primer momento en que supiste que me querías?",
                    isAnswered = false
                )
            )
            secretDao.insertSecretQuestion(
                SecretQuestionEntity(
                    id = 2L,
                    authorId = albertoId,
                    authorName = "Alberto",
                    questionText = "Si pudieras revivir un día conmigo, ¿cuál sería?",
                    isAnswered = true,
                    answerText = "El atardecer en el mirador bajo la lluvia"
                )
            )

            // 7. Desire Match Cards
            desireDao.insertAll(
                listOf(
                    DesireVoteEntity(1, "Baño a la luz de las velas", userVotedYes = false, partnerVotedYes = true, isMatched = false),
                    DesireVoteEntity(2, "Fin de semana sin móviles", userVotedYes = false, partnerVotedYes = true, isMatched = false),
                    DesireVoteEntity(3, "Carta erótica escrita a mano", userVotedYes = false, partnerVotedYes = false, isMatched = false),
                    DesireVoteEntity(4, "Masaje con aceites al reencontrarnos", userVotedYes = false, partnerVotedYes = true, isMatched = false),
                    DesireVoteEntity(5, "Desayuno en la cama… y quedarnos", userVotedYes = false, partnerVotedYes = true, isMatched = false)
                )
            )

            // 8. Voice Memories
            voiceDao.insertAll(
                listOf(
                    VoiceMemoryEntity(1L, "Buenas noches, mi amor", "Yuki", "0:48"),
                    VoiceMemoryEntity(2L, "Lo que no te dije en el aeropuerto", "Alberto", "2:12"),
                    VoiceMemoryEntity(3L, "Nuestro primer aniversario", "Yuki", "1:05")
                )
            )

            // 9. Vault Settings
            vaultDao.saveVaultSettings(VaultSettingsEntity(id = 1, pinCode = "1402", biometricsEnabled = true))
        }
    }
}
