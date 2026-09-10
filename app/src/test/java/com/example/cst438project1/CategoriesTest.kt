package com.example.cst438project1

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class CategoriesTest {

    @Test
    fun foodStoresValuesCorrectly() {
        val food = Food("Eggs", 70, "Breakfast")
        assertEquals("Eggs", food.name)
        assertEquals(70, food.calories)
        assertEquals("Breakfast", food.category)
    }

    @Test
    fun foodsListIsNotEmpty() {
        assertTrue(foods.isNotEmpty())
    }

    @Test
    fun foodsOnlyHaveExpectedCategories() {
        val validCategories = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
        for (food in foods) {
            assertTrue(food.category in validCategories)
        }
    }

    @Test
    fun categoryOrderHasAllFourCategories() {
        assertEquals(4, categoryOrder.size)
        assertTrue(categoryOrder.contains("Breakfast"))
        assertTrue(categoryOrder.contains("Lunch"))
        assertTrue(categoryOrder.contains("Dinner"))
        assertTrue(categoryOrder.contains("Snacks"))
    }

    @Test
    fun everyFoodCategoryExistsInCategoryOrder() {
        // makes sure nothing in foods has a category that categoryOrder doesn't know about
        for (food in foods) {
            assertTrue(food.category in categoryOrder)
        }
    }

    @Test
    fun filteringByCategoryReturnsOnlyThatCategory() {
        val breakfastFoods = foods.filter { it.category == "Breakfast" }
        assertEquals(2, breakfastFoods.size)
        for (food in breakfastFoods) {
            assertEquals("Breakfast", food.category)
        }
    }

    @Test
    fun twoFoodsWithSameValuesAreEqual() {
        // data classes auto-generate equals(), this just confirms that behavior
        val food1 = Food("Apple", 95, "Snacks")
        val food2 = Food("Apple", 95, "Snacks")
        assertEquals(food1, food2)
    }
}