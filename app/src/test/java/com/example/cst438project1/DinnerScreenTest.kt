package com.example.cst438project1

import org.junit.Assert.assertEquals
import org.junit.Test

class DinnerScreenTest {
// the tests below check the total calorie calculation based on
    // single foods
    // multiple foods
    // empty lists
    // zero calories foods
    // repeated foods

    @Test
    fun calculateTotalCalories_twoFoods_returnsCorrectTotal() {
        val foods = listOf(
            DinnerFood("Banana", 105),
            DinnerFood("Oatmeal", 150)
        )

        val result = calculateTotalCalories(foods)

        assertEquals(255, result)
    }

    @Test
    fun calculateTotalCalories_oneFood_returnsThatFoodCalories() {
        val foods = listOf(
            DinnerFood("Apple", 95)
        )

        val result = calculateTotalCalories(foods)

        assertEquals(95, result)
    }

    @Test
    fun calculateTotalCalories_emptyList_returnsZero() {
        val foods = emptyList<DinnerFood>()

        val result = calculateTotalCalories(foods)

        assertEquals(0, result)
    }

    @Test
    fun calculateTotalCalories_zeroCalorieFood_returnsZero() {
        val foods = listOf(
            DinnerFood("Water", 0)
        )

        val result = calculateTotalCalories(foods)

        assertEquals(0, result)
    }

    @Test
    fun calculateTotalCalories_multipleFoods_returnsCorrectTotal() {
        val foods = listOf(
            DinnerFood("Rice", 200),
            DinnerFood("Chicken", 300),
            DinnerFood("Salad", 100)
        )

        val result = calculateTotalCalories(foods)

        assertEquals(600, result)
    }

    @Test
    fun calculateTotalCalories_duplicateFoods_countsEachFood() {
        val foods = listOf(
            DinnerFood("Banana", 105),
            DinnerFood("Banana", 105)
        )

        val result = calculateTotalCalories(foods)

        assertEquals(210, result)
    }
}