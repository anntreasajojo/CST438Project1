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

data class LunchFood(
    val name: String,
    val calories: Int
)

fun calculateTotalCalories(foods: List<LunchFood>): Int = foods.sumOf { it.calories }

@Composable
fun LunchScreen(
    onBack: () -> Unit = {},
    onAddFood: (FoodEntry) -> Unit = {},
    repository: FdcRepository = remember { FdcRepository() }
) {
    var foodName by remember { mutableStateOf("") }
    val lunchFoods = remember { mutableStateListOf<FoodEntry>() }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val totalCalories = lunchFoods.sumOf { it.calories }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Add your lunch!",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Enter a food and keep track of your calories.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LunchSearchInput(
            foodName = foodName,
            onFoodNameChange = { foodName = it },
            loading = loading,
            onSearch = {
                scope.launch {
                    loading = true
                    errorMessage = null

                    val result = repository.searchFoods(foodName)

                    result.onSuccess { foods ->
                        lunchFoods.clear()
                        lunchFoods.addAll(foods)
                        if (foods.isEmpty() && foodName.isNotBlank()) {
                            errorMessage = "No foods matched that search."
                        }
                    }.onFailure { error ->
                        lunchFoods.clear()
                        errorMessage = error.message ?: "Search failed."
                    }

                    loading = false
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your lunch",
            style = MaterialTheme.typography.titleLarge
        )

        Text(text = "Total calories: $totalCalories")

        Spacer(modifier = Modifier.height(8.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        LunchFoodList(lunchFoods, onAddFood)
    }
}

@Composable
private fun LunchSearchInput(
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
private fun LunchFoodList(
    lunchFoods: List<FoodEntry>,
    onAddFood: (FoodEntry) -> Unit
) {
    LazyColumn {
        items(lunchFoods) { food ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                onClick = { onAddFood(food) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = food.name)
                    Text(text = "${food.calories} calories")
                    Text(text = "Carbs ${food.carbs}g • Protein ${food.protein}g • Fat ${food.fat}g")
                }
            }
        }
    }
}

// preview without running the app
@Preview(showBackground = true)
@Composable
fun LunchScreenPreview() {
    LunchScreen()
}
