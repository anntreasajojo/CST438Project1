package com.example.cst438project1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update

@Dao
interface FoodDao {
    @Insert
    suspend fun insertFood(food: Food): Long

    @Insert
    suspend fun insertFoods(foods: List<Food>): List<Long>

    @Query("SELECT * FROM foods")
    suspend fun getAllFoods(): List<Food>

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Int): Food?

    @Query("SELECT * FROM foods WHERE name = :name")
    suspend fun getFoodByName(name: String): Food?

    @Update
    suspend fun updateFood(food: Food)

    @Delete
    suspend fun deleteFood(food: Food)
}
