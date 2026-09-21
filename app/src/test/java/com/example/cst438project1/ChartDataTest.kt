package com.example.cst438project1

import com.example.cst438project1.database.MealLogEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ChartDataTest {
    private val today = LocalDate.of(2025, 1, 5)

    private fun entry(date: String, calories: Int, meal: Meal = Meal.BREAKFAST) = MealLogEntry(
        userId = 1, dateEpochDay = LocalDate.parse(date).toEpochDay(), meal = meal,
        food = FoodEntry("Food", calories, 10, 5, 2)
    )

    @Test
    fun dailyGroupsMealsAndExcludesBothOutsideBoundaries() {
        val entries = listOf(entry("2025-01-04", 900), entry("2025-01-05", 100),
            entry("2025-01-05", 200), entry("2025-01-05", 50, Meal.DINNER), entry("2025-01-06", 900))
        val bars = chartPoints(entries, ChartPeriod.DAILY, today, today)
        assertEquals(listOf(300, 0, 50, 0), bars.map { it.calories })
        assertTrue(bars.first().hasRecords)
        assertFalse(bars[1].hasRecords)
    }

    @Test
    fun weeklyUsesMondayAndIncludesDatesAcrossTheYearBoundary() {
        val entries = listOf(entry("2024-12-29", 900), entry("2024-12-30", 100),
            entry("2025-01-01", 200), entry("2025-01-05", 50), entry("2025-01-06", 900))
        assertEquals(LocalDate.of(2024, 12, 30), ChartPeriod.WEEKLY.start(today))
        assertEquals(LocalDate.of(2025, 1, 6), ChartPeriod.WEEKLY.end(today))
        assertEquals(listOf(100, 0, 200, 0, 0, 0, 50),
            chartPoints(entries, ChartPeriod.WEEKLY, today, today).map { it.calories })
    }

    @Test
    fun monthsHaveAllCalendarDaysIncludingLeapDay() {
        for ((date, days) in listOf("2024-02-15" to 29, "2025-02-15" to 28,
            "2025-04-15" to 30, "2025-01-15" to 31)) {
            val anchor = LocalDate.parse(date)
            assertEquals(days, chartPoints(emptyList(), ChartPeriod.MONTHLY, anchor, today).size)
        }
        val leapDay = LocalDate.of(2024, 2, 29)
        val bars = chartPoints(listOf(entry("2024-02-29", 150), entry("2024-03-01", 900)),
            ChartPeriod.MONTHLY, leapDay, today)
        assertEquals(150, bars.last().calories)
        assertEquals(150, bars.sumOf { it.calories })
        assertEquals(LocalDate.of(2024, 3, 1), ChartPeriod.MONTHLY.move(leapDay, 1))
    }

    @Test
    fun monthlyDistinguishesFutureFromMissingAndZeroRecords() {
        val entries = listOf(entry("2024-12-31", 900), entry("2025-01-01", 100),
            entry("2025-01-05", 200), entry("2026-01-01", 900))
        val bars = chartPoints(entries, ChartPeriod.MONTHLY, today, today)
        assertEquals(31, bars.size)
        assertEquals(100, bars.first().calories)
        assertEquals(300, bars.sumOf { it.calories })
        assertFalse(bars.first().future)
        assertTrue(bars[5].future)
        assertFalse(bars[1].hasRecords)
        assertEquals(LocalDate.of(2025, 2, 1), ChartPeriod.MONTHLY.move(today, 1))

        val daily = chartPoints(listOf(entry("2025-01-05", 0)), ChartPeriod.DAILY, today, today)
        assertTrue(daily.first().hasRecords)
        assertFalse(daily[1].hasRecords)
        assertTrue(daily.none { it.future })
        assertEquals(Macros(1000, 20, 10, 4), entries.take(2).map { it.food }.macros())
    }

    @Test
    fun calendarAlignsMondayFirstAndPadsOnlyOutsideTheMonth() {
        for (month in 1..12) {
            val start = LocalDate.of(2024, month, 1)
            val cells = calendarDays(start)
            assertEquals(0, cells.size % 7)
            assertEquals(start.dayOfWeek.value - 1, cells.indexOf(start))
            assertEquals((1..start.lengthOfMonth()).map { start.withDayOfMonth(it) }, cells.filterNotNull())
        }
        assertEquals(LocalDate.of(2024, 2, 29), calendarDays(LocalDate.of(2024, 2, 1)).filterNotNull().last())
    }

    @Test
    fun rejectsInvalidRecordsBeforeSaving() {
        val valid = entry("2025-01-05", 100)
        for (invalid in listOf<() -> MealLogEntry>(
            { valid.copy(userId = 0) },
            { valid.copy(food = valid.food.copy(name = " ")) },
            { valid.copy(food = valid.food.copy(calories = -1)) },
            { valid.copy(food = valid.food.copy(carbs = -1)) }
        )) {
            assertTrue(runCatching(invalid).exceptionOrNull() is IllegalArgumentException)
        }
    }
}
