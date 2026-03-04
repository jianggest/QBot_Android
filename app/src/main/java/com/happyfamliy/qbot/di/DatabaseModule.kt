package com.happyfamliy.qbot.di

import android.content.Context
import androidx.room.Room
import com.happyfamliy.qbot.data.local.QBotDatabase
import com.happyfamliy.qbot.data.local.dao.FactDao
import com.happyfamliy.qbot.data.local.dao.MessageDao
import com.happyfamliy.qbot.data.local.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQBotDatabase(
        @ApplicationContext context: Context
    ): QBotDatabase {
        return Room.databaseBuilder(
            context,
            QBotDatabase::class.java,
            "qbot_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: QBotDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: QBotDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideFactDao(database: QBotDatabase): FactDao {
        return database.factDao()
    }
}
