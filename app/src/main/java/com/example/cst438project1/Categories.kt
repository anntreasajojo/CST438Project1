package com.example.cst438project1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// food data class
// structure for storing one food item
data class Food(
    val name: String,
    val calories: Int,
    val category: String
)

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

// order of categories to show up in
val categoryOrder = listOf("Breakfast", "Lunch", "Dinner", "Snacks")

@Composable
fun CategoriesScreen() {
    // stores which food the user tapped on
    var selectedFood by remember { mutableStateOf<Food?>(null) }

    // stores which category headers are currently expanded
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // a button to go back to previous page
        Button(onClick = { }) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Browse by category",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Tap a category to expand it, tap a food to see its details.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))
        // LazyColumn for a vertical scrolling list
        LazyColumn {
            for (category in categoryOrder) {
                val isExpanded = expandedCategories.contains(category)
                val foodsInCategory = foods.filter { it.category == category }

                // category header
                // tap to expand/collapse a drop-down menu
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCategories = if (isExpanded) {
                                    expandedCategories - category
                                } else {
                                    expandedCategories + category
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        //Purple color (primary) from Theme.kt in ui.theme folder
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(if (isExpanded) "-" else "+",
                        color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider()
                }

                // only show the foods in this category if it's expanded
                if (isExpanded) {
                    items(foodsInCategory) { food ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable { selectedFood = food }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = food.name)
                                    Text(text = "${food.category} • ${food.calories} calories")
                                }

                                // placeholder box for the food image, replace for
                                // API image once I figure out the API
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(horizontal = 8.dp)
                                        .background(Color.LightGray)

                                )
                            }
                        }
                    }
                }
            }
        }

        // show details for whichever food was tapped
        selectedFood?.let { food ->
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selected food",
                style = MaterialTheme.typography.titleLarge
            )
            Text(text = "Name: ${food.name}")
            Text(text = "Category: ${food.category}")
            Text(text = "Calories: ${food.calories}")
        }
    }
}

// preview without running the app
@Preview(showBackground = true)
@Composable
fun CategoriesScreenPreview() {
    CategoriesScreen()
}