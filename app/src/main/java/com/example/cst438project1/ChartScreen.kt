package com.example.cst438project1

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cst438project1.database.MealLogDao
import com.example.cst438project1.database.MealLogEntry
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ChartScreen(
    dao: MealLogDao,
    userId: Int,
    today: LocalDate,
    period: ChartPeriod,
    anchor: LocalDate,
    onPeriodChange: (ChartPeriod) -> Unit,
    onDateChange: (LocalDate) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text("CHARTS", fontFamily = Mono, fontSize = 11.sp, letterSpacing = 2.sp)
        Text("Nutrition history", fontFamily = Display, fontSize = 32.sp)
        Spacer(Modifier.height(20.dp))
        PeriodControls(period, anchor, today, onPeriodChange, onDateChange)
        Spacer(Modifier.height(16.dp))
        MealLogContent(dao, userId, period.start(anchor), period.end(anchor)) { entries ->
            ChartSummary(entries, period, anchor, today) { date ->
                onDateChange(date)
                onPeriodChange(ChartPeriod.DAILY)
            }
        }
    }
}

@Composable
internal fun PeriodControls(
    period: ChartPeriod,
    anchor: LocalDate,
    today: LocalDate,
    onPeriodChange: (ChartPeriod) -> Unit,
    onDateChange: (LocalDate) -> Unit
) {
    Row(Modifier.fillMaxWidth().selectableGroup()) {
        ChartPeriod.entries.forEach { option ->
            val selected = option == period
            Box(
                Modifier.weight(1f)
                    .background(if (selected) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surface)
                    .selectable(selected, role = Role.Tab, onClick = { onPeriodChange(option) })
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(option.label, fontSize = 12.sp)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    val start = period.start(anchor)
    val end = period.end(anchor)
    Text(if (period == ChartPeriod.DAILY) "$start" else "$start – ${end.minusDays(1)}",
        fontFamily = Mono, fontSize = 13.sp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = { onDateChange(period.move(anchor, -1)) }) { Text("Previous") }
        TextButton(onClick = { onDateChange(today) }) { Text("Current") }
        TextButton(
            onClick = { onDateChange(period.move(anchor, 1)) },
            enabled = end <= today
        ) { Text("Next") }
    }
}

@Composable
internal fun ChartSummary(
    entries: List<MealLogEntry>,
    period: ChartPeriod,
    anchor: LocalDate,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    val points = chartPoints(entries, period, anchor, today)
    if (period == ChartPeriod.MONTHLY) {
        NutritionCalendar(points, anchor, today, onSelectDate)
        return
    }
    val totals = entries.map { it.food }.macros()
    Text("${totals.calories.grouped()} kcal", fontFamily = Mono, fontSize = 28.sp)
    Text("Total for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth()) {
        MacroStat("Carbs", totals.carbs, Modifier.weight(1f))
        MacroStat("Protein", totals.protein, Modifier.weight(1f))
        MacroStat("Fat", totals.fat, Modifier.weight(1f))
    }
    Spacer(Modifier.height(24.dp))
    if (entries.isEmpty()) {
        Text("No records for this period. Add food from Favorites to start tracking.")
        Spacer(Modifier.height(16.dp))
    }
    NutritionGraph(points)
    Spacer(Modifier.height(12.dp))
    Text("Calories (kcal). Gaps mean no food was logged. Future dates are not plotted.",
        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun NutritionGraph(points: List<ChartPoint>) {
    val maximum = (points.filterNot { it.future }.maxOfOrNull { it.calories } ?: 0).coerceAtLeast(1)
    val ink = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.width(42.dp).height(200.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(maximum.grouped(), fontSize = 10.sp)
            Text((maximum / 2).grouped(), fontSize = 10.sp)
            Text("0", fontSize = 10.sp)
        }
        Canvas(Modifier.weight(1f).height(200.dp).padding(vertical = 6.dp)) {
            val positions = points.mapIndexed { index, point ->
                Offset(size.width * (index + 0.5f) / points.size,
                    size.height * (1f - point.calories.toFloat() / maximum))
            }
            for (step in 0..2) {
                val y = size.height * step / 2
                drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            drawLine(grid, Offset.Zero, Offset(0f, size.height), 1.dp.toPx())
            points.forEachIndexed { index, point ->
                if (!point.future) {
                    if (index > 0 && point.hasRecords && points[index - 1].hasRecords) {
                        drawLine(ink, positions[index - 1], positions[index], 2.dp.toPx())
                    }
                    drawCircle(ink, 4.dp.toPx(), positions[index],
                        style = if (point.hasRecords) Fill else Stroke(1.dp.toPx()))
                }
            }
        }
    }
    GraphLabels(points)
}

@Composable
private fun GraphLabels(points: List<ChartPoint>) {
    Row(Modifier.fillMaxWidth().padding(start = 42.dp)) {
        points.forEach { point ->
            val value = when {
                point.future -> "Future"
                !point.hasRecords -> "No record"
                else -> "${point.calories.grouped()} kcal"
            }
            Column(Modifier.weight(1f).clearAndSetSemantics {
                contentDescription = "${point.description}: $value"
            }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(point.label, fontSize = 10.sp, textAlign = TextAlign.Center)
                Text(if (point.hasRecords && !point.future) point.calories.grouped() else "—",
                    fontFamily = Mono, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun NutritionCalendar(
    points: List<ChartPoint>,
    anchor: LocalDate,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    Text("Daily calories (kcal)", fontFamily = Mono, fontSize = 14.sp)
    Text("Tap a date to view its daily graph.", fontSize = 12.sp)
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEach { day ->
            Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(8.dp))
    calendarDays(anchor).chunked(DayOfWeek.entries.size).forEach { week ->
        Row(Modifier.fillMaxWidth()) {
            week.forEach { date ->
                if (date == null) {
                    Spacer(Modifier.weight(1f).height(72.dp))
                } else {
                    CalendarDay(date, points[date.dayOfMonth - 1], today,
                        Modifier.weight(1f), onSelectDate)
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    point: ChartPoint,
    today: LocalDate,
    modifier: Modifier,
    onSelectDate: (LocalDate) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val value = if (point.future) "Future" else if (point.hasRecords) "${point.calories} kcal" else "No record"
    Column(
        modifier.height(72.dp).padding(1.dp)
            .border(1.dp, if (date == today) colors.primary else colors.outlineVariant)
            .background(if (date == today) colors.secondaryContainer else colors.surface)
            .clickable(enabled = !point.future, role = Role.Button, onClick = { onSelectDate(date) })
            .semantics(mergeDescendants = true) { contentDescription = "$date: $value" }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(date.dayOfMonth.toString(), fontSize = 12.sp,
            color = if (point.future) colors.outline else colors.onSurface)
        Text(if (point.future) "—" else point.calories.toString(), fontSize = 10.sp,
            color = if (point.future) colors.outline else colors.onSurface)
    }
}
