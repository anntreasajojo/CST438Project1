package com.example.cst438project1

import androidx.compose.foundation.layout.Column
// needed to create empty space between stuff
import androidx.compose.foundation.layout.Spacer
// needed to for an element to fill up all the space on the screen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
// this creates a vertical SCROLLABLE list
import androidx.compose.foundation.lazy.LazyColumn
// needed so that a kotlin list can be displayed inside a lazy column
import androidx.compose.foundation.lazy.items
// needed to use buttons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
// needed so we can have values that can change AND update the screen
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cst438project1.fdc.FdcRepository
import kotlinx.coroutines.launch

data class DinnerFood(
    val name: String,
    val calories: Int
)

fun calculateTotalCalories(foods: List<DinnerFood>): Int {
    var totalCalories = 0
    for (food in foods) {
        if (food.calories >= 0) {
            totalCalories += food.calories
        }
    }
    return totalCalories
}

@Composable
fun DinnerScreen(
    onBack: () -> Unit = {},
    onAddFood: (FoodEntry) -> Unit = {},
    repository: FdcRepository = remember { FdcRepository() },
    userId: Int = 0,
    favoriteDao: com.example.cst438project1.database.FavoriteDao? = null,
    foodDao: com.example.cst438project1.database.FoodDao? = null,
    onFavoriteAdded: () -> Unit = {}
) {
    var foodName by remember { mutableStateOf("") }
    val dinnerFoods = remember { mutableStateListOf<FoodEntry>() }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val totalCalories = calculateTotalCalories(
        dinnerFoods.map { food -> DinnerFood(food.name, food.calories) }
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Spacer(modifier = Modifier.height(16.dp))
        DinnerHeader()
        Spacer(modifier = Modifier.height(16.dp))
        DinnerSearchInput(
            foodName = foodName,
            onFoodNameChange = { foodName = it },
            loading = loading,
            onSearch = {
                scope.launch {
                    loading = true
                    errorMessage = null
                    val result = repository.searchFoods(foodName)
                    result.onSuccess { foods ->
                        dinnerFoods.clear()
                        dinnerFoods.addAll(foods)
                        if (foods.isEmpty() && foodName.isNotBlank()) {
                            errorMessage = "No foods matched that search."
                        }
                    }.onFailure { error ->
                        dinnerFoods.clear()
                        errorMessage = error.message ?: "Search failed."
                    }
                    loading = false
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your dinner", style = MaterialTheme.typography.titleLarge)
        Text(text = "Total calories: $totalCalories")
        Spacer(modifier = Modifier.height(8.dp))
        if (loading) {
            CircularProgressIndicator()
        } else {
            errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        DinnerFoodList(
            dinnerFoods = dinnerFoods,
            onAddFood = onAddFood,
            userId = userId,
            favoriteDao = favoriteDao,
            foodDao = foodDao,
            scope = scope,
            onFavoriteAdded = onFavoriteAdded
        )
    }
}

@Composable
private fun DinnerHeader() {
    Text("Add your dinner!", style = MaterialTheme.typography.headlineMedium)
    Text("Enter a food and keep track of your calories.",
        style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun DinnerSearchInput(
    foodName: String,
    onFoodNameChange: (String) -> Unit,
    loading: Boolean,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = foodName,
        onValueChange = onFoodNameChange,
        label = { Text("Food name") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onSearch,
        modifier = Modifier.fillMaxWidth(),
        enabled = !loading
    ) {
        Text("Search Food")
    }
}

@Composable
private fun DinnerFoodList(
    dinnerFoods: List<FoodEntry>,
    onAddFood: (FoodEntry) -> Unit,
    userId: Int,
    favoriteDao: com.example.cst438project1.database.FavoriteDao?,
    foodDao: com.example.cst438project1.database.FoodDao?,
    scope: kotlinx.coroutines.CoroutineScope,
    onFavoriteAdded: () -> Unit = {}
) {
    LazyColumn {
        items(dinnerFoods) { food ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                onClick = { onAddFood(food) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = food.name)
                    Text(text = "${food.calories} calories")
                    Text(text = "Carbs ${food.carbs}g • Protein ${food.protein}g • Fat ${food.fat}g")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                addToFavorites(food, userId, favoriteDao, foodDao)
                                onFavoriteAdded()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Favorites")
                    }
                }
            }
        }
    }
}

private suspend fun addToFavorites(
    food: FoodEntry,
    userId: Int,
    favoriteDao: com.example.cst438project1.database.FavoriteDao?,
    foodDao: com.example.cst438project1.database.FoodDao?
) {
    if (favoriteDao == null || foodDao == null || userId <= 0) return

    try {
        var foodId = food.foodId
        if (foodId == 0) {
            val dbFood = com.example.cst438project1.database.Food(
                name = food.name,
                calories = food.calories,
                fat = food.fat.toDouble(),
                protein = food.protein.toDouble(),
                carbs = food.carbs.toDouble()
            )
            foodId = foodDao.insertFood(dbFood).toInt()
        }

        val favorite = com.example.cst438project1.database.Favorite(
            userId = userId,
            foodId = foodId
        )
        favoriteDao.insertFavorite(favorite)
    } catch (e: IllegalStateException) {
        // Database error - silently fail
    }
}

// preview without running the app
@Preview(showBackground = true)
@Composable
fun dinnerScreenPreview() {
    DinnerScreen()
}
