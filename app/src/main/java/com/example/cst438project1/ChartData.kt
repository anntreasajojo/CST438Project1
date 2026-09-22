package com.example.cst438project1

import com.example.cst438project1.database.MealLogEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class ChartPeriod(val label: String, val unit: ChronoUnit) {
    DAILY("Daily", ChronoUnit.DAYS),
    WEEKLY("Weekly", ChronoUnit.WEEKS),
    MONTHLY("Monthly", ChronoUnit.MONTHS);

    fun start(date: LocalDate): LocalDate = when (this) {
        DAILY -> date
        WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        MONTHLY -> date.withDayOfMonth(1)
    }

    fun end(date: LocalDate): LocalDate = start(date).plus(1, unit)

    fun move(date: LocalDate, steps: Long): LocalDate = start(date).plus(steps, unit)
}

data class ChartPoint(
    val label: String,
    val description: String,
    val calories: Int,
    val hasRecords: Boolean,
    val future: Boolean
)

fun chartPoints(
    entries: List<MealLogEntry>,
    period: ChartPeriod,
    anchor: LocalDate,
    today: LocalDate
): List<ChartPoint> {
    val start = period.start(anchor)
    val end = period.end(anchor)
    val records = entries.filter { it.dateEpochDay in start.toEpochDay() until end.toEpochDay() }
    if (period == ChartPeriod.DAILY) {
        val byMeal = records.groupBy { it.meal }
        return Meal.entries.map { meal ->
            val foods = byMeal[meal].orEmpty()
            ChartPoint(meal.label, "$start ${meal.label}", foods.sumOf { it.food.calories },
                foods.isNotEmpty(), start > today)
        }
    }
    val byDate = records.groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }
    return generateSequence(start) { it.plusDays(1) }.takeWhile { it < end }.map { date ->
        val foods = byDate[date].orEmpty()
        val label = when (period) {
            ChartPeriod.WEEKLY -> date.format(DateTimeFormatter.ofPattern("EEE d"))
            else -> date.dayOfMonth.toString()
        }
        ChartPoint(label, date.toString(),
            foods.sumOf { it.food.calories }, foods.isNotEmpty(), date > today)
    }.toList()
}

internal fun calendarDays(anchor: LocalDate): List<LocalDate?> {
    val start = anchor.withDayOfMonth(1)
    val days = List<LocalDate?>(start.dayOfWeek.value - 1) { null } +
        List(start.lengthOfMonth()) { start.plusDays(it.toLong()) }
    val weekSize = DayOfWeek.entries.size
    return days + List((weekSize - days.size % weekSize) % weekSize) { null }
}
