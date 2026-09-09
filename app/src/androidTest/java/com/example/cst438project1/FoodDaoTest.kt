package com.example.cst438project1

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.Food
import com.example.cst438project1.database.FoodDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var foodDao: FoodDao

    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        foodDao = db.foodDao()
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
        val food = Food(0, "Banana", 260, 0.4, 0.4, 0.4)
        foodDao.insertFood(food)
        val retrievedUser = foodDao.getFoodById(0)
        assertEquals(food, retrievedUser)

    }
}