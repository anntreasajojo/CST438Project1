package com.example.cst438project1

import android.content.Context
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.User
import com.example.cst438project1.ui.theme.CST438Project1Theme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ChartScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var user: User

    @Before
    fun createDatabase() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java).build()
        user = runBlocking {
            val id = db.userDao().insertUser(chartTestUser()).toInt()
            chartTestUser().copy(id = id)
        }
    }

    @After
    fun closeDatabase() = db.close()

    @Test
    fun switchesPeriodsAndRestoresTheSelectedPeriodAndDate() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { CST438Project1Theme { SignedInApp(user, db.mealLogDao()) } }
        compose.onNodeWithText("CHARTS").performClick()
        compose.onNodeWithText("Daily").assertIsSelected()
        compose.onNodeWithText("Next").assertIsNotEnabled()
        compose.onNodeWithText("Yearly").assertDoesNotExist()
        for (label in listOf("Weekly", "Monthly")) {
            compose.onNodeWithText(label).performClick().assertIsSelected()
            compose.onNodeWithText("Next").assertIsNotEnabled()
        }
        compose.onNodeWithText("Previous").performClick()
        val previousMonth = LocalDate.now().withDayOfMonth(1).minusMonths(1)
        val range = "$previousMonth – ${previousMonth.withDayOfMonth(previousMonth.lengthOfMonth())}"
        compose.onNodeWithText(range).assertExists()
        compose.onNodeWithText("FAVORITES").performClick()
        compose.onNodeWithText("CHARTS").performClick()
        compose.onNodeWithText("Monthly").assertIsSelected()
        compose.onNodeWithText(range).assertExists()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Monthly").assertIsSelected()
        compose.onNodeWithText(range).assertExists()
        compose.onNodeWithText("Current").performClick()
        compose.onNodeWithText("Next").assertIsNotEnabled()
    }

    @Test
    fun calendarOpensTheSelectedDaysRecordsAndDisablesFutureDates() {
        val today = LocalDate.now()
        val selectedDate = today.withDayOfMonth(1).minusDays(1)
        runBlocking {
            db.mealLogDao().insert(com.example.cst438project1.database.MealLogEntry(
                userId = user.id, dateEpochDay = selectedDate.toEpochDay(), meal = Meal.LUNCH,
                food = FoodEntry("Test lunch", 420, 40, 20, 10)
            ))
        }
        compose.setContent { CST438Project1Theme { SignedInApp(user, db.mealLogDao()) } }
        compose.onNodeWithText("CHARTS").performClick()
        compose.onNodeWithText("Monthly").performClick()
        awaitText("Daily calories (kcal)")
        if (today.dayOfMonth < today.lengthOfMonth()) {
            compose.onNodeWithContentDescription("${today.plusDays(1)}: Future").assertIsNotEnabled()
        }
        compose.onNodeWithText("Previous").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithContentDescription("$selectedDate: 420 kcal").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("$selectedDate: 420 kcal").performScrollTo().performClick()
        compose.onNodeWithText("Daily").assertIsSelected()
        compose.onNodeWithText(selectedDate.toString()).assertExists()
        awaitText("420 kcal")
        compose.onNodeWithContentDescription("$selectedDate Lunch: 420 kcal").assertExists()
    }

    @Test
    fun addingAndDeletingFoodUpdatesTodayProfileAndCharts() {
        compose.setContent { CST438Project1Theme { SignedInApp(user, db.mealLogDao()) } }
        compose.onNodeWithText("FAVORITES").performClick()
        compose.onNodeWithText("Greek yogurt").performClick()
        compose.onNodeWithText("LUNCH").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasText("TODAY") and isSelected()).fetchSemanticsNodes().isNotEmpty()
        }
        awaitText("Greek yogurt")
        compose.onNodeWithText("PROFILE").performClick()
        awaitText("130 of 2,000 kcal")
        compose.onNodeWithText("CHARTS").performClick()
        val bar = "${LocalDate.now()} Lunch: 130 kcal"
        compose.waitUntil(10_000) {
            compose.onAllNodesWithContentDescription(bar).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription(bar).assertExists()
        compose.onNodeWithText("TODAY").performClick()
        awaitText("Greek yogurt")
        compose.onNodeWithText("Greek yogurt").performScrollTo().performTouchInput { longClick() }
        compose.onNodeWithText("Remove").performClick()
        compose.onNodeWithText("CHARTS").performClick()
        awaitText("No records for this period. Add food from Favorites to start tracking.")
        compose.onNodeWithText("0 kcal").assertExists()
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
