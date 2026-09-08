package com.example.cst438project1.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}


// Render the page it's easier on my laptop
//@Preview(device = Devices.PIXEL_7, showSystemUi = true)
//@Composible
//fun LoginScreenPreview() {
//    AppTheme {
//        LoginScreen()
//    }
//}