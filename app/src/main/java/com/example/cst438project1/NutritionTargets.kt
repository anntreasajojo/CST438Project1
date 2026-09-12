package com.example.cst438project1

import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.Sex
import kotlin.math.roundToInt

// Share of the calorie target each macro carries, and the calories a gram of it
// provides.
private const val CARB_SHARE = 0.40
private const val PROTEIN_SHARE = 0.30
private const val FAT_SHARE = 0.30
private const val CALORIES_PER_CARB_GRAM = 4
private const val CALORIES_PER_PROTEIN_GRAM = 4
private const val CALORIES_PER_FAT_GRAM = 9

// A deficit applied to someone small can land somewhere no one should eat, so
// the target stops here regardless of the inputs.
private const val CALORIE_FLOOR = 1200

// Mifflin-St Jeor: calories burned at rest.
fun bmr(sex: Sex, weightKg: Int, heightCm: Int, age: Int): Double =
    10.0 * weightKg + 6.25 * heightCm - 5.0 * age + when (sex) {
        Sex.MALE -> 5.0
        Sex.FEMALE -> -161.0
        // Halfway between the two, so the estimate leans neither way.
        Sex.OTHER -> -78.0
    }

// Resting burn scaled by how much the person moves, shifted by what they are
// trying to do, then split into macros.
fun targets(
    sex: Sex,
    weightKg: Int,
    heightCm: Int,
    age: Int,
    activity: Activity,
    goal: Goal
): Profile {
    val calories = ((bmr(sex, weightKg, heightCm, age) * activity.multiplier).roundToInt() +
        goal.calorieShift).coerceAtLeast(CALORIE_FLOOR)

    return Profile(
        calorieGoal = calories,
        carbGoal = (calories * CARB_SHARE / CALORIES_PER_CARB_GRAM).roundToInt(),
        proteinGoal = (calories * PROTEIN_SHARE / CALORIES_PER_PROTEIN_GRAM).roundToInt(),
        fatGoal = (calories * FAT_SHARE / CALORIES_PER_FAT_GRAM).roundToInt()
    )
}
