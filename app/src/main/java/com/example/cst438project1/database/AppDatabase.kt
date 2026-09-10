package com.example.cst438project1.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

// Room stores primitives, so the onboarding enums travel as their names.
class Converters {
    @TypeConverter fun fromSex(value: Sex): String = value.name
    @TypeConverter fun toSex(value: String): Sex = Sex.valueOf(value)

    @TypeConverter fun fromActivity(value: Activity): String = value.name
    @TypeConverter fun toActivity(value: String): Activity = Activity.valueOf(value)

    @TypeConverter fun fromGoal(value: Goal): String = value.name
    @TypeConverter fun toGoal(value: String): Goal = Goal.valueOf(value)
}

@Database(
    entities = [User::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

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
                    // Only test accounts exist so far, so a schema change wipes
                    // the database instead of costing a hand-written migration.
                    // Freeze the schema or write a real Migration before the demo.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
