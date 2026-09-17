package com.example.cst438project1.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class Food (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val calories: Int,
    val fat: Double,
    val protein: Double,
    val carbs: Double
) {
    //Testing purposes
    override fun toString(): String {
        return "ID: $id\n Name: $name\n Calories: $calories\n Fat: $fat\n Protein: $protein\n Carbs: $carbs\n"
    }
}
