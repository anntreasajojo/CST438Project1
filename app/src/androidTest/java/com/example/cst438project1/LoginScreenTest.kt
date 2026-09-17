package com.example.cst438project1

import android.content.Context
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.Sex
import com.example.cst438project1.database.User
import com.example.cst438project1.ui.theme.CST438Project1Theme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var db: AppDatabase
    private var loggedIn: User? = null
    private var userId = 0

    private val password = "hunter2000"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.userDao()
        val salt = PasswordHasher.newSalt()
        userId = runBlocking {
            dao.insertUser(
                User(
                    username = "doyoung",
                    passwordHash = PasswordHasher.hash(password, salt), salt = salt,
                    age = 30, sex = Sex.MALE, heightCm = 180, weightKg = 75,
                    activity = Activity.MODERATE, goal = Goal.MAINTAIN,
                    calorieGoal = 2000, carbGoal = 200, proteinGoal = 150, fatGoal = 67
                )
            ).toInt()
        }
        compose.setContent {
            CST438Project1Theme {
                LoginScreen(dao = dao, onLoggedIn = { loggedIn = it }, onRegister = {})
            }
        }
    }

    @After
    fun tearDown() = db.close()

    private fun logIn(name: String, pass: String) {
        compose.onNodeWithText("Username").performTextInput(name)
        compose.onNodeWithText("Password").performTextInput(pass)
        compose.onNodeWithText("Log in").performClick()
    }

    private fun waitForFailure() = compose.waitUntil(5_000) {
        compose.onAllNodesWithText(LOGIN_FAILED).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun loginIsBlockedUntilBothFieldsAreFilled() {
        compose.onNodeWithText("Log in").assertIsNotEnabled()
        compose.onNodeWithText("Username").performTextInput("doyoung")
        compose.onNodeWithText("Log in").assertIsNotEnabled()
        compose.onNodeWithText("Password").performTextInput(password)
        compose.onNodeWithText("Log in").assertIsEnabled()
    }

    @Test
    fun theRightPasswordLogsIn() {
        logIn("doyoung", password)
        compose.waitUntil(5_000) { loggedIn != null }

        assertEquals(userId, loggedIn?.id)
    }

    @Test
    fun aWrongPasswordShowsTheMessageAndStaysPut() {
        logIn("doyoung", "hunter2001")
        waitForFailure()

        assertNull(loggedIn)
    }

    @Test
    fun anUnknownUsernameGetsTheSameMessage() {
        logIn("nobody", password)
        waitForFailure()

        assertNull(loggedIn)
    }

    @Test
    fun typingAgainClearsTheMessage() {
        logIn("doyoung", "hunter2001")
        waitForFailure()

        compose.onNodeWithText("Password").performTextInput("x")

        compose.onNodeWithText(LOGIN_FAILED).assertDoesNotExist()
    }
}
