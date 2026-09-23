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
    // `foodName` stores what the user types
    // `remember` keyword is used to keep the value when screen updates
    var foodName by remember { mutableStateOf("") }
    val breakfastFoods = remember { mutableStateListOf<FoodEntry>() }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // this adds the calories from every food in the breakfast list
    var totalCalories = 0
    for (food in breakfastFoods) {
        totalCalories += food.calories
    }

    // START OF THE MAIN COLUMN
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // a button to go back to previous page
        Button(onClick = onBack) {
            Text("Back")
        }

        //add some space after back button
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Add your breakfast!",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Enter a food and search FDC when you press the button.",
            style = MaterialTheme.typography.bodyMedium
        )

        //add some space after description
        Spacer(modifier = Modifier.height(16.dp))

        // food search bar- text bar where user can type
        OutlinedTextField(
            value = foodName,
            // runs everytime user types or deletes character
            // it = new text in the text field
            onValueChange = { foodName = it },
            label = { Text("Food name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // add some space
        Spacer(modifier = Modifier.height(8.dp))

        // search button
        Button(
            onClick = {
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
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Search Food")
        }

        // add space
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your breakfast",
            style = MaterialTheme.typography.titleLarge
        )

        // display value for total calories from the breakfastFoods
        Text(text = "Total calories: $totalCalories")

        // add some space
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

        // our vertically scrollable list
        LazyColumn {
            // goes through every item in breakfastFoods
            items(breakfastFoods) { food ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    onClick = { onAddFood(food) }
                ) {
                    // for each item in the list, show food name and calories in a card list layout
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = food.name)
                        Text(text = "${food.calories} calories")
                        Text(text = "Carbs ${food.carbs}g • Protein ${food.protein}g • Fat ${food.fat}g")
                    }
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
