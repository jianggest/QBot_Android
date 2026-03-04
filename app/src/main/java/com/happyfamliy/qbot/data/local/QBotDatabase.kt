package com.happyfamliy.qbot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.happyfamliy.qbot.data.local.dao.FactDao
import com.happyfamliy.qbot.data.local.dao.MessageDao
import com.happyfamliy.qbot.data.local.dao.SessionDao
import com.happyfamliy.qbot.data.local.entity.FactEntity
import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.data.local.entity.SessionEntity

@Database(
    entities = [
        SessionEntity::class,
        MessageEntity::class,
        FactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QBotDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun factDao(): FactDao
}
