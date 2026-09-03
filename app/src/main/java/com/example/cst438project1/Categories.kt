package com.example.cst438project1

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
data class Food(val name: String,
                val calories: Int,
                val category: String)

// random data, change for API stuff later.
val foods = listOf(
    Food("Eggs", 70, "Breakfast"),
    Food("Oatmeal", 150, "Breakfast"),
    Food("Chicken Sandwich", 450, "Lunch"),
    Food("Caesar Salad", 500, "Lunch"),
    Food("Spaghetti", 300, "Dinner"),
    Food("Salmon", 200, "Dinner"),
    Food("Apple", 95, "Snacks"),
    Food("Protein Shake", 150, "Snacks")
)

@Composable
fun CategoriesScreen() {
    var selectedFood by remember { mutableStateOf<Food?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        for (food in foods) {
            Text(
                text = "${food.category}: ${food.name} (${food.calories} cal)",
                modifier = Modifier.clickable { selectedFood = food }
            )
        }

        selectedFood?.let { food ->
            Text(text = "\nSelected: ${food.name}")
            Text(text = "Category: ${food.category}")
            Text(text = "Calories: ${food.calories}")
        }
    }
}