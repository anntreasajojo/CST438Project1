package com.example.cst438project1

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.Sex
import com.example.cst438project1.database.User
import com.example.cst438project1.database.UserDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        userDao = db.userDao()
    }

    @After
    fun closeDB() {
        try {
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun user(username: String = "testuser") = User(
        username = username,
        passwordHash = "hash-$username",
        salt = "salt-$username",
        age = 30,
        sex = Sex.MALE,
        heightCm = 180,
        weightKg = 75,
        activity = Activity.MODERATE,
        goal = Goal.MAINTAIN,
        calorieGoal = 2682,
        carbGoal = 268,
        proteinGoal = 201,
        fatGoal = 89
    )

    // Every column, including the enums, survives a write and a read.
    @Test
    fun insertUser() = runTest {
        val id = userDao.insertUser(user()).toInt()
        assertEquals(user().copy(id = id), userDao.getUserById(id))
    }

    // The id is what tells the caller who just registered.
    @Test
    fun insertUserReturnsGeneratedId() = runTest {
        val first = userDao.insertUser(user("first"))
        val second = userDao.insertUser(user("second"))
        assertEquals(first + 1, second)
        assertEquals("second", userDao.getUserById(second.toInt())?.username)
    }

    @Test
    fun countByUsername() = runTest {
        assertEquals(0, userDao.countByUsername("testuser"))
        userDao.insertUser(user())
        assertEquals(1, userDao.countByUsername("testuser"))
    }

    // The unique index, not the app, is what makes duplicate usernames impossible.
    @Test(expected = SQLiteConstraintException::class)
    fun duplicateUsernameIsRejected() = runTest {
        userDao.insertUser(user())
        userDao.insertUser(user())
    }

    @Test
    fun getUserByUsername() = runTest {
        userDao.insertUser(user())
        assertNotNull(userDao.getUserByUsername("testuser"))
        assertNull(userDao.getUserByUsername("nobody"))
    }

    // MainActivity uses this to decide whether to show the register screen.
    @Test
    fun countIsZeroUntilSomeoneRegisters() = runTest {
        assertEquals(0, userDao.count())
        userDao.insertUser(user())
        assertEquals(1, userDao.count())
    }

    @Test
    fun deleteUser() = runTest {
        val id = userDao.insertUser(user()).toInt()
        userDao.deleteUser(userDao.getUserById(id)!!)
        assertEquals(0, userDao.count())
    }
}
