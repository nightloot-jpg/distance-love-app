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
        NoteEntity::class,
        DailyAnswerEntity::class,
        DailyChatEntity::class,
        ChallengeEntity::class,
        SecretQuestionEntity::class,
        DesireVoteEntity::class,
        VaultSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun noteDao(): NoteDao
    abstract fun dailyDao(): DailyDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun secretQuestionDao(): SecretQuestionDao
    abstract fun desireDao(): DesireDao
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
                    .fallbackToDestructiveMigration()
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
                        seedDefaultTemplates(database)
                    }
                }
            }
        }

        suspend fun seedDefaultTemplates(db: AppDatabase) {
            val challengeDao = db.challengeDao()
            val vaultDao = db.vaultSettingsDao()

            challengeDao.insertAll(
                listOf(
                    ChallengeEntity(1, null, "Cocinar lo mismo", "Preparad la misma receta y cenad en videollamada.", "ChefHat"),
                    ChallengeEntity(2, null, "Foto espontánea", "Mandad una foto de lo que veis ahora mismo, sin filtros.", "Camera"),
                    ChallengeEntity(3, null, "Playlist cruzada", "Añadid 5 canciones que os recuerden al otro.", "Music"),
                    ChallengeEntity(4, null, "Misma luna", "Fotografiad la luna desde vuestra ciudad esta noche.", "Moon")
                )
            )

            vaultDao.saveVaultSettings(VaultSettingsEntity(id = 1, pinCode = "1402", biometricsEnabled = true))
        }
    }
}
