package com.example.cst438project1.database

import android.content.Context
import androidx.room.Database
import androidx.room.AutoMigration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.cst438project1.Meal

// Room stores primitives, so the onboarding enums travel as their names.
class Converters {
    @TypeConverter fun fromMeal(value: Meal): String = value.name
    @TypeConverter fun toMeal(value: String): Meal = Meal.valueOf(value)

    @TypeConverter fun fromSex(value: Sex): String = value.name
    @TypeConverter fun toSex(value: String): Sex = Sex.valueOf(value)

    @TypeConverter fun fromActivity(value: Activity): String = value.name
    @TypeConverter fun toActivity(value: String): Activity = Activity.valueOf(value)

    @TypeConverter fun fromGoal(value: Goal): String = value.name
    @TypeConverter fun toGoal(value: String): Goal = Goal.valueOf(value)
}

@Database(
    entities = [User::class, Food::class, MealLogEntry::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 2, to = 3)],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun mealLogDao(): MealLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // Version 2 upgrades without losing accounts or food data.
                    // Unsupported older schemas are preserved, never wiped.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


// Render the page it's easier on my laptop
//@Preview(device = Devices.PIXEL_7, showSystemUi = true)
//@Composible
//fun LoginScreenPreview() {
//    AppTheme {
//        LoginScreen()
//    }
//}
