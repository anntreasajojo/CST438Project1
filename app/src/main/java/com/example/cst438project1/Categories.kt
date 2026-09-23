package com.example.cst438project1

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
    val category: String,
    val carbs: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0
)

// random data, change for API stuff later.
val foods = listOf(
    Food("Eggs", 70, "Breakfast", carbs = 0, protein = 6, fat = 5),
    Food("Oatmeal", 150, "Breakfast", carbs = 27, protein = 5, fat = 3),
    Food("Chicken Sandwich", 450, "Lunch", carbs = 40, protein = 30, fat = 15),
    Food("Caesar Salad", 500, "Lunch", carbs = 20, protein = 10, fat = 40),
    Food("Spaghetti", 300, "Dinner", carbs = 55, protein = 10, fat = 5),
    Food("Salmon", 200, "Dinner", carbs = 0, protein = 28, fat = 10),
    Food("Apple", 95, "Snacks", carbs = 25, protein = 0, fat = 0),
    Food("Protein Shake", 150, "Snacks", carbs = 10, protein = 25, fat = 3)
)

// order of categories to show up in
val categoryOrder = listOf("Breakfast", "Lunch", "Dinner", "Snacks")

// converts Food from the list into a FoodEntry that FavoritesScreen understands
fun Food.toFoodEntry() = FoodEntry(
    name = this.name,
    calories = this.calories,
    carbs = this.carbs,
    protein = this.protein,
    fat = this.fat
)

// clickable header row for each category with a +/- toggle
@Composable
private fun CategoryHeader(category: String, isExpanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // purple color (primary) from Theme.kt in ui.theme folder
        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            if (isExpanded) "-" else "+",
            color = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider()
}

// card row for a single food item with heart button
@Composable
private fun FoodCard(
    food: Food,
    isFavorited: Boolean,
    onTap: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onTap() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = food.name)
                Text(text = "${food.category} • ${food.calories} calories")
            }

            // heart icon button to add/remove from favorites
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    // filled heart if favorited, outline if not
                    imageVector = if (isFavorited) Icons.Filled.Favorite
                    else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorited) "Remove from favorites"
                    else "Add to favorites",
                    // red if favorited, gray if not
                    tint = if (isFavorited) Color.Red else Color.Gray
                )
            }
        }
    }
}

// shows nutritional details for the food the user tapped on
@Composable
private fun SelectedFoodDetail(food: Food) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "Selected food", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = "Name: ${food.name}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "Category: ${food.category}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "Calories: ${food.calories}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "Carbs: ${food.carbs}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "Protein: ${food.protein}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "Fat: ${food.fat}")
}

@Composable
fun CategoriesScreen(
    onBack: () -> Unit = {},
    // favorites list from MainActivity so both screens share the same data
    favorites: SnapshotStateList<FoodEntry> = rememberFavorites()
) {
    // stores which food the user tapped on
    var selectedFood by remember { mutableStateOf<Food?>(null) }

    // stores which category headers are currently expanded
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // a button to go back to previous page
        Button(onClick = onBack) { Text("Back") }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Browse by category", style = MaterialTheme.typography.headlineMedium)

        Text(
            text = "Tap a category to expand it, tap a food to see its details.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // LazyColumn for a vertical scrolling list
        LazyColumn {
            for (category in categoryOrder) {
                // true if this specific category is in the expanded set
                val isExpanded = expandedCategories.contains(category)
                // only the foods that belong to this category
                val foodsInCategory = foods.filter { it.category == category }

                // one row per category, this is the clickable header
                item {
                    CategoryHeader(
                        category = category,
                        isExpanded = isExpanded,
                        onToggle = {
                            // toggles this category in/out of the expanded set when tapped
                            expandedCategories = if (isExpanded) {
                                expandedCategories - category
                            } else {
                                expandedCategories + category
                            }
                        }
                    )
                }

                // only actually adds these rows to the list if the category is expanded
                if (isExpanded) {
                    items(foodsInCategory) { food ->
                        // check if this food is already in the favorites list
                        val isFavorited = favorites.any { it.name == food.name }
                        FoodCard(
                            food = food,
                            isFavorited = isFavorited,
                            onTap = { selectedFood = food },
                            onFavoriteToggle = {
                                if (isFavorited) {
                                    // remove it if already favorited
                                    favorites.removeAll { it.name == food.name }
                                } else {
                                    // add it if not favorited yet
                                    favorites.add(food.toFoodEntry())
                                }
                            }
                        )
                    }
                }
            }
        }

        // only runs this block if something's actually been tapped
        // show details for whichever food was tapped
        selectedFood?.let { food ->
            SelectedFoodDetail(food)
        }
    }
}

// preview without running the app
@Preview(showBackground = true)
@Composable
fun CategoriesScreenPreview() {
    CategoriesScreen()
}

