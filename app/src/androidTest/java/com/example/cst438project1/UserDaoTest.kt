package com.example.cst438project1

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.User
import com.example.cst438project1.database.UserDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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

    @Test
    fun insertUser() = runTest {
        val user = User(id = 1, username = "testuser", password = "password")
        userDao.insertUser(user)
        val retrievedUser = userDao.getUserById(1)
        assertEquals(user, retrievedUser)

    }
}