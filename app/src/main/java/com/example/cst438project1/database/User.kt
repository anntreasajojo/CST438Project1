package com.example.cst438project1.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
    fun getId(): Int = id
    fun getUsername(): String = username
    fun getPassword(): String = password

    // Maybe used for debugging, that is why ID and Password are included.
    override fun toString(): String {
        return "ID: $id \n Username: $username \n Password: $password"
    }
}