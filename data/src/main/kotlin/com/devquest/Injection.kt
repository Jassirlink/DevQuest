package com.devquest.data

import android.content.Context
import androidx.room.Room
import com.devquest.database.QuestDatabase
import com.devquest.domain.*
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): QuestDatabase =
        Room.databaseBuilder(context, QuestDatabase::class.java, "devquest.db").build()

    @Provides fun clock(): Clock = Clock.systemDefaultZone()

    @Provides fun completion(repository: QuestRepository) = CompleteLesson(repository)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun quests(repository: LocalRepository): QuestRepository

    @Binds abstract fun settings(repository: LocalSettings): SettingsRepository
}
