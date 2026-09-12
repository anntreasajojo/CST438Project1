package com.example.cst438project1

import android.content.Context
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.Sex
import com.example.cst438project1.database.User
import com.example.cst438project1.database.UserDao
import com.example.cst438project1.ui.theme.CST438Project1Theme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: UserDao
    private var registeredId: Int? = null

    private val password = "hunter2000"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.userDao()
        compose.setContent {
            CST438Project1Theme {
                RegisterScreen(dao = dao, onRegistered = { registeredId = it })
            }
        }
    }

    @After
    fun tearDown() = db.close()

    private fun typeAccount(name: String = "doyoung", confirmWith: String = password) {
        compose.onNodeWithText("Username").performTextInput(name)
        compose.onNodeWithText("Password").performTextInput(password)
        compose.onNodeWithText("Re-enter password").performTextInput(confirmWith)
    }

    // Next runs a query before it switches steps, so the second step is not on
    // screen the instant the click returns.
    private fun goToOnboarding() {
        compose.onNodeWithText("Next").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("About you").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun typeBody(age: String = "30", height: String = "180", weight: String = "75") {
        compose.onNodeWithText("Age").performScrollTo().performTextInput(age)
        compose.onNodeWithText("Height cm").performScrollTo().performTextInput(height)
        compose.onNodeWithText("Weight kg").performScrollTo().performTextInput(weight)
    }

    private fun stored(): User? = runBlocking { dao.latestUser() }

    @Test
    fun nextIsBlockedWhileThePasswordsDiffer() {
        typeAccount(confirmWith = "hunter2001")

        compose.onNodeWithText("Passwords do not match.").assertExists()
        compose.onNodeWithText("Next").assertIsNotEnabled()

        compose.onNodeWithText("Re-enter password").performTextClearance()
        compose.onNodeWithText("Re-enter password").performTextInput(password)

        compose.onNodeWithText("Passwords do not match.").assertDoesNotExist()
        compose.onNodeWithText("Next").assertIsEnabled()
    }

    @Test
    fun aTakenUsernameKeepsTheUserOnTheFirstStep() {
        runBlocking {
            dao.insertUser(
                User(
                    username = "doyoung", passwordHash = "x", salt = "y",
                    age = 30, sex = Sex.MALE, heightCm = 180, weightKg = 75,
                    activity = Activity.MODERATE, goal = Goal.MAINTAIN,
                    calorieGoal = 2000, carbGoal = 200, proteinGoal = 150, fatGoal = 67
                )
            )
        }

        typeAccount()
        compose.onNodeWithText("Next").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("That username is taken.")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Still on step one - the onboarding heading never appeared.
        compose.onNodeWithText("About you").assertDoesNotExist()
    }

    @Test
    fun targetsAreCalculatedFromTheOnboardingAnswers() {
        typeAccount()
        goToOnboarding()
        typeBody()
        compose.onNodeWithText("Create account").performScrollTo().performClick()
        compose.waitUntil(5_000) { registeredId != null }

        // Mifflin-St Jeor for a 30 year old male, 180cm, 75kg, moderately
        // active, maintaining.
        assertEquals(2682, stored()?.calorieGoal)
    }

    @Test
    fun anEditedTargetSurvivesLaterChanges() {
        typeAccount()
        goToOnboarding()
        typeBody()

        compose.onNodeWithText("Calories").performScrollTo().performTextClearance()
        compose.onNodeWithText("Calories").performTextInput("3000")

        // Changing an answer recalculates the fields the user has not touched.
        compose.onNodeWithText("Very active").performScrollTo().performClick()

        compose.onNodeWithText("Create account").performScrollTo().performClick()
        compose.waitUntil(5_000) { registeredId != null }

        val saved = stored()!!
        assertEquals(3000, saved.calorieGoal)
        // Carbs followed the new activity level instead of the edited calories.
        assertEquals(
            targets(Sex.MALE, 75, 180, 30, Activity.VERY_ACTIVE, Goal.MAINTAIN).carbGoal,
            saved.carbGoal
        )
    }

    @Test
    fun recalculateDropsTheManualEdit() {
        typeAccount()
        goToOnboarding()
        typeBody()

        compose.onNodeWithText("Calories").performScrollTo().performTextClearance()
        compose.onNodeWithText("Calories").performTextInput("3000")
        compose.onNodeWithText("Recalculate").performScrollTo().performClick()

        compose.onNodeWithText("Create account").performScrollTo().performClick()
        compose.waitUntil(5_000) { registeredId != null }

        assertEquals(2682, stored()?.calorieGoal)
    }

    @Test
    fun registeringStoresOneUserWithAHashedPassword() {
        typeAccount()
        goToOnboarding()
        typeBody()
        compose.onNodeWithText("Female").performScrollTo().performClick()
        compose.onNodeWithText("Lose weight").performScrollTo().performClick()
        compose.onNodeWithText("Create account").performScrollTo().performClick()
        compose.waitUntil(5_000) { registeredId != null }

        val saved = stored()!!
        assertEquals(1, runBlocking { dao.count() })
        assertEquals("doyoung", saved.username)
        assertEquals(Sex.FEMALE, saved.sex)
        assertEquals(Goal.LOSE, saved.goal)
        assertEquals(saved.id, registeredId)

        // The point of the whole exercise.
        assertNotEquals(password, saved.passwordHash)
        assertNull(usernameError(saved.username))
        assertEquals(true, PasswordHasher.verify(password, saved.salt, saved.passwordHash))
    }
}
