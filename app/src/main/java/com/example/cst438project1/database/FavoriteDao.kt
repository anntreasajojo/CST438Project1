package com.example.cst438project1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

@Dao
interface FavoriteDao {
    @Insert
    suspend fun insertFavorite(favorite: Favorite): Long

    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getFavoriteById(id: Int): Favorite?

    @Query("SELECT * FROM favorites WHERE user_id = :userId ORDER BY id DESC")
    suspend fun getFavoritesByUserId(userId: Int): List<Favorite>

    @Query("SELECT * FROM favorites WHERE food_id = :foodId ORDER BY id DESC")
    suspend fun getFavoritesByFoodId(foodId: Int): List<Favorite>

    @Query("SELECT * FROM favorites ORDER BY id DESC")
    suspend fun getAllFavorites(): List<Favorite>

    @Update
    suspend fun updateFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE user_id = :userId")
    suspend fun deleteFavoritesByUserId(userId: Int)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()
}