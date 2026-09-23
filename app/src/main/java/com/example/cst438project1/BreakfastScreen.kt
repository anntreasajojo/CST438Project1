package com.example.cst438project1

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

// food data class
// structure for storing one food item
data class BreakfastFood(
    val name: String,
    val calories: Int
)

@Composable
fun BreakfastScreen(
    onBack: () -> Unit = {},
    onAddFood: (FoodEntry) -> Unit = {},
    repository: FdcRepository = remember { FdcRepository() }
) {
    var foodName by remember { mutableStateOf("") }
    val breakfastFoods = remember { mutableStateListOf<FoodEntry>() }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var totalCalories = 0
    for (food in breakfastFoods) {
        totalCalories += food.calories
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Add your breakfast!",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Enter a food and search FDC when you press the button.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        BreakfastSearchInput(
            foodName = foodName,
            onFoodNameChange = { foodName = it },
            loading = loading,
            onSearch = {
                scope.launch {
                    loading = true
                    errorMessage = null

                    val result = repository.searchFoods(foodName)

                    result.onSuccess { foods ->
                        breakfastFoods.clear()
                        breakfastFoods.addAll(foods)
                        if (foods.isEmpty() && foodName.isNotBlank()) {
                            errorMessage = "No foods matched that search."
                        }
                    }.onFailure { error ->
                        breakfastFoods.clear()
                        errorMessage = error.message ?: "Search failed."
                    }

                    loading = false
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your breakfast",
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

        BreakfastFoodList(breakfastFoods, onAddFood)
    }
}

@Composable
private fun BreakfastSearchInput(
    foodName: String,
    onFoodNameChange: (String) -> Unit,
    loading: Boolean,
    onSearch: () -> Unit
) {
    // Text input field where user enters the food name to search
    OutlinedTextField(
        value = foodName,
        onValueChange = onFoodNameChange,
        label = { Text("Food name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Search button - disabled while loading results from API
    Button(
        onClick = onSearch,
        modifier = Modifier.fillMaxWidth(),
        enabled = !loading
    ) {
        Text("Search Food")
    }
}

@Composable
private fun BreakfastFoodList(
    breakfastFoods: List<FoodEntry>,
    onAddFood: (FoodEntry) -> Unit
) {
    // Scrollable list showing search results; user clicks to add to breakfast
    LazyColumn {
        items(breakfastFoods) { food ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
fun BreakfastScreenPreview() {
    BreakfastScreen()
}
