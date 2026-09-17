package com.example.cst438project1

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.Entries
import com.example.cst438project1.database.EntriesDao
import com.example.cst438project1.database.Food
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.Sex
import com.example.cst438project1.database.User
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntriesDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var entriesDao: EntriesDao

    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        entriesDao = db.entriesDao()
    }

    @After
    fun closeDB() {
        db.close()
    }

    @Test
    fun insertEntryAndReadByUserId() = runTest {
        val userId = db.userDao().insertUser(
            User(
                username = "entryuser",
                passwordHash = "hash-entryuser",
                salt = "salt-entryuser",
                age = 30,
                sex = Sex.MALE,
                heightCm = 180,
                weightKg = 75,
                activity = Activity.MODERATE,
                goal = Goal.MAINTAIN,
                calorieGoal = 2200,
                carbGoal = 275,
                proteinGoal = 180,
                fatGoal = 70
            )
        ).toInt()

        val foodId = db.foodDao().insertFood(
            Food(
                id = 1,
                name = "Banana",
                calories = 105,
                fat = 0.3,
                protein = 1.3,
                carbs = 27.0
            )
        ).toInt()

        val entry = Entries(
            userId = userId,
            foodId = foodId,
            date = "2026-09-16",
            quantity = 1.0,
            counter = 1
        )

        val insertedId = entriesDao.insertEntry(entry).toInt()
        val retrieved = entriesDao.getEntriesByUserId(userId)

        assertEquals(insertedId, retrieved.first().id)
        assertEquals(userId, retrieved.first().userId)
        assertEquals(foodId, retrieved.first().foodId)
    }
}
