package com.example.cst438project1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface EntriesDao {
    @Insert
    suspend fun insertEntry(entry: Entries): Long

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById(id: Int): Entries?

    @Query("SELECT * FROM entries WHERE user_id = :userId ORDER BY date DESC, id DESC")
    suspend fun getEntriesByUserId(userId: Int): List<Entries>

    @Query("SELECT * FROM entries WHERE food_id = :foodId ORDER BY date DESC, id DESC")
    suspend fun getEntriesByFoodId(foodId: Int): List<Entries>

    @Query("SELECT * FROM entries ORDER BY date DESC, id DESC")
    suspend fun getAllEntries(): List<Entries>

    @Update
    suspend fun updateEntry(entry: Entries)

    @Delete
    suspend fun deleteEntry(entry: Entries)

    @Query("DELETE FROM entries WHERE user_id = :userId")
    suspend fun deleteEntriesByUserId(userId: Int)

    @Query("DELETE FROM entries")
    suspend fun clearEntries()
}