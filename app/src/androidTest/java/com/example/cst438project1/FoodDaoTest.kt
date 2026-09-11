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
    fun insertFood() = runTest {
        val food = Food(1, "Banana", 260, 0.4, 0.4, 0.4)
        try {
            foodDao.insertFood(food)
            val retrievedFood = foodDao.getFoodById(1)
            assertEquals(food, retrievedFood)
            println("TEST: PASSED, insertFood()")
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun getFoodByName() = runTest {
        val food = Food(1, "Banana", 260, 0.4, 0.4, 0.4)
        try {
            foodDao.insertFood(food)
            val retrievedFood = foodDao.getFoodByName("Banana")
            assertEquals(food, retrievedFood)
            println("TEST: PASSED, getFoodByName()")
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}