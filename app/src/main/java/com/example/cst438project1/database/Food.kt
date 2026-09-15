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
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun getId(): Int = id
    fun getName(): String = name
    fun getCalories(): Int = calories
    fun getFat(): Double = fat
    fun getProtein(): Double = protein
    fun getCarbs(): Double = carbs

    //Testing purposes
    override fun toString(): String {
        return "ID: $id\n Name: $name\n Calories: $calories\n Fat: $fat\n Protein: $protein\n Carbs: $carbs\n"
    }
}