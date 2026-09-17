package com.example.cst438project1

import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.Sex
import com.example.cst438project1.database.User
import com.example.cst438project1.database.UserDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginLogicTest {
    private val password = "hunter2000"
    private val salt = PasswordHasher.newSalt()
    private val stored = User(
        id = 7, username = "doyoung",
        passwordHash = PasswordHasher.hash(password, salt), salt = salt,
        age = 30, sex = Sex.MALE, heightCm = 180, weightKg = 75,
        activity = Activity.MODERATE, goal = Goal.MAINTAIN,
        calorieGoal = 2000, carbGoal = 200, proteinGoal = 150, fatGoal = 67
    )

    // Only the lookup matters to login.
    private val dao = object : UserDao by NoopUserDao {
        override suspend fun getUserByUsername(username: String) =
            stored.takeIf { it.username == username }
    }

    private fun login(username: String, password: String) =
        runBlocking { authenticate(dao, username, password) }

    @Test
    fun theRightPasswordLogsIn() {
        assertEquals(7, login("doyoung", password)?.id)
    }

    @Test
    fun aWrongPasswordDoesNot() {
        assertNull(login("doyoung", "hunter2001"))
        assertNull(login("doyoung", password.uppercase()))
        assertNull(login("doyoung", "$password "))
    }

    @Test
    fun anUnknownUsernameDoesNot() {
        assertNull(login("nobody", password))
    }
}
