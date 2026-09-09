package com.example.cst438project1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update

@Dao
interface FoodDao {

    @Insert
    suspend fun insertFood(food: Food)

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Int): Food?

    @Query("SELECT * FROM foods WHERE name = :name")
    suspend fun getFoodByName(name: String): Food?

    @Query("SELECT * FROM foods WHERE calories = :calories")
    suspend fun getFoodByCalories(calories: Int): Food?

    @Query("SELECT * FROM foods WHERE fat = :fat")
    suspend fun getFoodByFat(fat: Double): Food?

    @Query("SELECT * FROM foods WHERE protein = :protein")
    suspend fun getFoodByProtein(protein: Double): Food?

    @Query("SELECT * FROM foods WHERE carbs = :carbs")
    suspend fun getFoodByCarbs(carbs: Double): Food?

}