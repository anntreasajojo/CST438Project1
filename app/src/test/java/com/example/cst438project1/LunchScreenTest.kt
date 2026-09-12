package com.example.cst438project1

import org.junit.Assert.assertEquals
import org.junit.Test

class LunchScreenTest {

    @Test
    fun calculateTotalCalories_returnsCorrectTotal() {
        val foods = listOf(
            LunchFood("Banana", 105),
            LunchFood("Oatmeal", 150)
        )

        val result = calculateTotalCalories(foods)

        assertEquals(255, result)
    }

    @Test
    fun calculateTotalCalories_emptyListReturnsZero() {
        assertEquals(0, calculateTotalCalories(emptyList()))
    }
}