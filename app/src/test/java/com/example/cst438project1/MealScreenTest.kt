package com.example.cst438project1

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LunchScreenTest {

    @Test
    fun foodEntryList_returnsCorrectCalorieTotal() {
        val foods = listOf(
            FoodEntry( name = "Banana", calories = 105, carbs = 27, protein = 1, fat = 0),
            FoodEntry( name = "Oatmeal", calories = 150, carbs = 27, protein = 5, fat = 3)
        )

        var result = 0

        for (food in foods) {
            result = result + food.calories
        }

        assertEquals(255, result)
    }

    @Test
    fun emptyFoodEntryList_returnsZeroCalories() {
        val foods = emptyList<FoodEntry>()

        var result = 0

        for (food in foods) {
            result = result + food.calories
        }

        assertEquals(0, result)
    }

    @Test
    fun foodEntry_storesCorrectName() {
        val food = FoodEntry("Banana", 105, 27, 1, 0)

        assertEquals("Banana", food.name)
    }

    @Test
    fun foodEntry_storesCorrectNutritionValues() {
        val food = FoodEntry("Oatmeal", 150, 27, 5, 3)

        assertEquals(150, food.calories)
        assertEquals(27, food.carbs)
        assertEquals(5, food.protein)
        assertEquals(3, food.fat)
    }

//    invalid or blank food searches do not accidentally add a food
@Test
fun invalidFoodName_returnsNoMatch() {
    val foods = listOf(
        FoodEntry("Banana", 105, 27, 1, 0),
        FoodEntry("Oatmeal", 150, 27, 5, 3)
    )

    // assume the food was not found
    var foodWasFound = false

    // checks each food in the list
    for (food in foods) {
        if (food.name == "Pizza") {
            foodWasFound = true
            break
        }
    }

    // pizza should not be found
    assertEquals(false, foodWasFound)
}

    @Test
    fun blankFoodName_returnsNoMatch() {
        val foods = listOf(
            FoodEntry("Banana", 105, 27, 1, 0)
        )

        // assume a matching food was not found
        var foodWasFound = false

        // checks each food in the list
        for (food in foods) {
            if (food.name == "") {
                foodWasFound = true
                break
            }
        }

        // a blank name should not match any food
        assertEquals(false, foodWasFound)
    }
}
