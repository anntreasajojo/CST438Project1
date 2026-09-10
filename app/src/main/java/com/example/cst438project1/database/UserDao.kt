package com.example.cst438project1.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    // Returns the generated row id, which is how the caller learns who just
    // registered. Throws on a duplicate username - the unique index is what
    // actually enforces it.
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun countByUsername(username: String): Int

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Delete
    suspend fun deleteUser(user: User)
}
