package com.example.cst438project1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.MealLogEntry
import com.example.cst438project1.database.Sex
import com.example.cst438project1.database.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry

internal fun chartTestUser(name: String = "chart-user") = User(
    username = name, passwordHash = "test-hash", salt = "test-salt", age = 30,
    sex = Sex.OTHER, heightCm = 170, weightKg = 70, activity = Activity.MODERATE,
    goal = Goal.MAINTAIN, calorieGoal = 2000, carbGoal = 250, proteinGoal = 120, fatGoal = 65
)

class MealLogDaoTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: AppDatabase
    private val databaseName = "chart-migration-test.db"

    @Before
    fun createDatabase() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDatabase() {
        db.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun isolatesUsersAndRangeAndDeletesOnlyTheSelectedDuplicate() = runBlocking {
        val user = db.userDao().insertUser(chartTestUser()).toInt()
        val other = db.userDao().insertUser(chartTestUser("other")).toInt()
        val dao = db.mealLogDao()
        val entry = MealLogEntry(userId = user, dateEpochDay = 100, meal = Meal.LUNCH,
            food = FoodEntry("Lunch", 500, 50, 20, 10))
        dao.insert(entry.copy(dateEpochDay = 99))
        val first = dao.insert(entry).toInt()
        val second = dao.insert(entry).toInt()
        dao.insert(entry.copy(dateEpochDay = 101))
        dao.insert(entry.copy(userId = other))
        assertEquals(listOf(first, second), dao.observe(user, 100, 101).first().map { it.id })
        dao.delete(other, first)
        assertEquals(2, dao.observe(user, 100, 101).first().size)
        dao.delete(user, first)
        assertEquals(listOf(second), dao.observe(user, 100, 101).first().map { it.id })
        assertEquals(1, dao.observe(other, 100, 101).first().size)
    }

    @Test
    fun upgradesVersionTwoWithoutDataLossAndPersistsNewRecords() = runBlocking {
        db.close()
        createVersionTwoDatabase()
        db = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
        assertEquals("existing", db.userDao().getUserById(1)?.username)
        assertEquals("existing-hash", db.userDao().getUserById(1)?.passwordHash)
        assertEquals("Banana", db.foodDao().getFoodById(1)?.name)
        assertEquals(emptyList<MealLogEntry>(), db.mealLogDao().observe(1, 0, 200).first())
        val entry = MealLogEntry(userId = 1, dateEpochDay = 100, meal = Meal.SNACKS,
            food = FoodEntry("Banana", 105, 27, 1, 0))
        db.mealLogDao().insert(entry)
        db.close()
        db = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
        assertEquals(entry.food, db.mealLogDao().observe(1, 100, 101).first().single().food)
        assertNotNull(db.userDao().getUserById(1))
    }

    private fun createVersionTwoDatabase() {
        context.deleteDatabase(databaseName)
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val schema = assets.open("com.example.cst438project1.database.AppDatabase/2.json")
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(databaseName), null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                old.execSQL(entity.getString("createSql")
                    .replace("\${TABLE_NAME}", entity.getString("tableName")))
                val indices = entity.optJSONArray("indices")
                for (item in 0 until (indices?.length() ?: 0)) {
                    old.execSQL(indices!!.getJSONObject(item).getString("createSql")
                        .replace("\${TABLE_NAME}", entity.getString("tableName")))
                }
            }
            val queries = schema.getJSONArray("setupQueries")
            for (index in 0 until queries.length()) old.execSQL(queries.getString(index))
            old.execSQL("INSERT INTO users VALUES " +
                "(1, 'existing', 'existing-hash', 'salt', 30, 'OTHER', 170, 70, " +
                "'MODERATE', 'MAINTAIN', 2000, 250, 120, 65)")
            old.execSQL("INSERT INTO foods VALUES (1, 'Banana', 105, 0.0, 1.0, 27.0)")
            old.version = 2
        }
    }
}
