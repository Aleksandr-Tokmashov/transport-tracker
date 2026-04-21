package com.example.transporttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.transporttracker.data.local.entity.PatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: PatternEntity)

    @Query("SELECT * FROM patterns ORDER BY count DESC")
    fun getAllPatterns(): Flow<List<PatternEntity>>

    @Query("DELETE FROM patterns")
    suspend fun clearAll()
}