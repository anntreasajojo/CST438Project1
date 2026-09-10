package com.example.cst438project1.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Sex { MALE, FEMALE, OTHER }

// Multipliers applied to BMR to get maintenance calories.
enum class Activity(val multiplier: Double) {
    SEDENTARY(1.2),
    LIGHT(1.375),
    MODERATE(1.55),
    ACTIVE(1.725),
    VERY_ACTIVE(1.9)
}

// Calories added to or removed from maintenance.
enum class Goal(val calorieShift: Int) {
    LOSE(-500),
    MAINTAIN(0),
    GAIN(500)
}

// One account. The onboarding answers are kept alongside the targets they
// produced, so the targets can be recalculated later if the user edits their
// profile.
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    // PBKDF2 output and its salt, both Base64. Never the password itself.
    val passwordHash: String,
    val salt: String,
    val age: Int,
    val sex: Sex,
    val heightCm: Int,
    val weightKg: Int,
    val activity: Activity,
    val goal: Goal,
    // Calculated at registration, then editable by the user.
    val calorieGoal: Int,
    val carbGoal: Int,
    val proteinGoal: Int,
    val fatGoal: Int
) {
    // Credentials stay out of logs and crash reports.
    override fun toString() = "User(id=$id, username=$username)"
}
