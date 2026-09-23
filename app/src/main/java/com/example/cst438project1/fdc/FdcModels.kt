package com.example.cst438project1.fdc

import com.example.cst438project1.FoodEntry
import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

data class FdcSearchResponse(
    @SerializedName("foods")
    val foods: List<FdcFood> = emptyList()
)

data class FdcFood(
    @SerializedName("fdcId")
    val fdcId: Int,
    @SerializedName("description")
    val description: String,
    @SerializedName("brandOwner")
    val brandOwner: String? = null,
    @SerializedName("foodNutrients")
    val foodNutrients: List<FdcNutrient> = emptyList()
)

data class FdcNutrient(
    @SerializedName("nutrientName")
    val nutrientName: String? = null,
    @SerializedName("value")
    val value: Double? = null
)

fun FdcFood.toFoodEntry(): FoodEntry {
    val nutrientsByName = foodNutrients.associateBy { it.nutrientName.orEmpty() }
    fun grams(name: String): Int = nutrientsByName[name]?.value?.roundToInt()?.coerceAtLeast(0) ?: 0

    return FoodEntry(
        // Keep the existing app model simple: name plus the macros used on meal cards.
        name = listOfNotNull(description.takeIf { it.isNotBlank() }, brandOwner?.takeIf { it.isNotBlank() })
            .joinToString(" - "),
        calories = grams("Energy"),
        carbs = grams("Carbohydrate, by difference"),
        protein = grams("Protein"),
        fat = grams("Total lipid (fat)")
    )
}
