package com.example.cst438project1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Insert
    suspend fun insert(entry: MealLogEntry): Long

    @Query("DELETE FROM meal_logs WHERE userId = :userId AND id = :id")
    suspend fun delete(userId: Int, id: Int)

    @Query(
        "SELECT * FROM meal_logs WHERE userId = :userId " +
            "AND dateEpochDay >= :startDay AND dateEpochDay < :endDay " +
            "ORDER BY dateEpochDay, id"
    )
    fun observe(userId: Int, startDay: Long, endDay: Long): Flow<List<MealLogEntry>>
}
