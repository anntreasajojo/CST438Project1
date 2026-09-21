package com.example.cst438project1.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.cst438project1.FoodEntry
import com.example.cst438project1.Meal
import java.time.LocalDate

@Entity(
    tableName = "meal_logs",
    indices = [Index(value = ["userId", "dateEpochDay"])],
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MealLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val dateEpochDay: Long,
    val meal: Meal,
    @Embedded val food: FoodEntry
) {
    init {
        require(userId > 0)
        LocalDate.ofEpochDay(dateEpochDay)
        require(food.name.isNotBlank())
        require(listOf(food.calories, food.carbs, food.protein, food.fat).all { it >= 0 })
    }
}
