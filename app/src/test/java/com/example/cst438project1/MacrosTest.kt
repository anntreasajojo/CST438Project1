package com.example.cst438project1

import org.junit.Assert.assertEquals
import org.junit.Test

class MacrosTest {
    @Test
    fun sumsEveryMacroAcrossEntries() {
        val totals = listOf(
            FoodEntry("Banana", 105, 27, 1, 0),
            FoodEntry("Oatmeal", 150, 27, 5, 3)
        ).macros()
        assertEquals(Macros(255, 54, 6, 3), totals)
    }

    @Test
    fun emptyLogIsAllZero() {
        assertEquals(Macros(0, 0, 0, 0), emptyList<FoodEntry>().macros())
    }
}
