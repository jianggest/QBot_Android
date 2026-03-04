package com.happyfamliy.qbot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facts")
data class FactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val topic: String,
    val embedding: String, // FloatArray serialized to JSON String
    val accessCount: Int = 1,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
