package com.example.cst438project1

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert.*
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.Favorite
import com.example.cst438project1.database.FavoriteDao
import kotlinx.coroutines.test.runTest

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var favoriteDao: FavoriteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        favoriteDao = db.favoriteDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertFavoriteAndReadByUserId() = runTest {
        val favorite = Favorite(userId = 1, foodId = 1)
        favoriteDao.insertFavorite(favorite)
        val retrievedFavorites = favoriteDao.getFavoritesByUserId(1)
        assertEquals(1, retrievedFavorites.size)
        assertEquals(favorite.userId, retrievedFavorites[0].userId)
        assertEquals(favorite.foodId, retrievedFavorites[0].foodId)
        println("TEST: PASSED, insertFavoriteAndReadByUserId()")
    }
}