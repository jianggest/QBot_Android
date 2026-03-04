package com.happyfamliy.qbot.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.happyfamliy.qbot.data.local.entity.FactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FactDao {
    @Query("SELECT * FROM facts ORDER BY lastUpdatedAt DESC")
    fun getAllFacts(): Flow<List<FactEntity>>

    @Query("SELECT * FROM facts")
    suspend fun getAllFactsSync(): List<FactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: FactEntity): Long

    @Update
    suspend fun updateFact(fact: FactEntity)

    @Delete
    suspend fun deleteFact(fact: FactEntity)
}
