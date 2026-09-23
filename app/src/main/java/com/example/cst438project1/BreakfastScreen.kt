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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
// need this to create interface functions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
// needed so we can have values that can change AND update the screen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// needed to show a loading circle while API is searchin
import androidx.compose.material3.CircularProgressIndicator
// needed to create a list that UPDATES screen when things are added or removed
import androidx.compose.runtime.mutableStateListOf
// needed to connect the screen to the food search API
import com.example.cst438project1.fdc.FdcRepository
// needed to run code when the search changes
import androidx.compose.runtime.LaunchedEffect

// checks whether the user typed a actual food name ie not like numbers or emojojies
fun isValidFoodName(foodName: String): Boolean {
    // removes extra spaces and checks that something was entered
    return foodName.trim().isNotEmpty()
}

@Suppress("LongMethod")
@Composable
fun BreakfastScreen(
    onBack: () -> Unit = {},
    onAddFood: (FoodEntry) -> Unit = {},
    repository: FdcRepository = remember { FdcRepository() }

) {
    // `foodName` stores what the user types
    // `remember` keyword is used to keep the value when screen updates
    var foodName by remember { mutableStateOf("") }

    // stores food results returned by the API
    val breakfastFoods = remember { mutableStateListOf<FoodEntry>() }

    // keeping track of what state the screen is in
    // keeps track of whether the API is searching
    var loading by remember { mutableStateOf(false) }

    // an error message if the search fails
    var errorMessage by remember { mutableStateOf("") }

    // changes whenever the user presses the search button
    var searchRequest by remember { mutableStateOf(0) }

    // runs the API search whenever searchRequest changes
    LaunchedEffect(searchRequest) {
        // prevents a search when the screen first opens
        if (searchRequest == 0) return@LaunchedEffect

        // shows that the app is searching
        loading = true

        // removes an old error message
        errorMessage = ""

        // searches the API using the user's food name
        val result = repository.searchFoods(foodName)

        // runs when the search is successful
        result.onSuccess { foods ->
            breakfastFoods.clear()
            breakfastFoods.addAll(foods)

            if (foods.isEmpty()) {
                errorMessage = "No foods were found."
            }
        }

        // runs when the search fails
        result.onFailure { error ->
            breakfastFoods.clear()
            // gets the error message from the API
            val errorText = error.message

            // uses the API message if one exists
            if (errorText != null) {
                errorMessage = errorText
            } else {
                errorMessage = "Food search failed."
            }
        }

        // tells the screen the search is finished
        loading = false
    }


    // START OF THE MAIN COLUMN
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)) {

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
            text = "Enter a food and keep track of your calories.",
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
            modifier = Modifier.fillMaxWidth()
        )

        // add some space
        Spacer(modifier = Modifier.height(8.dp))

        // submit button
        Button(
            onClick = {
                // changes searchRequest and starts the API search
                searchRequest++
            },
            // only enables the button when a food name is entered
            enabled = isValidFoodName(foodName) && !loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Search Food") }

        // add space
        Spacer(modifier = Modifier.height(16.dp))

        // displays a loading circle while the API is searching
        if (loading) {
            CircularProgressIndicator()
        }

        // only displays the message when an error exists
        if (errorMessage != "") {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            text = "Search results",
            style = MaterialTheme.typography.titleLarge
        )

        // add some space
        Spacer(modifier = Modifier.height(8.dp))

        // our vertically scrollable list
        LazyColumn(
            // add space
            modifier = Modifier.weight(1f)
        ) {
            // displays each food returned by the API
            items(breakfastFoods) { food ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = food.name)
                        Text(text = "${food.calories} calories")
                        Text(text = "Carbs: ${food.carbs}g")
                        Text(text = "Protein: ${food.protein}g")
                        Text(text = "Fat: ${food.fat}g")

                        Spacer(modifier = Modifier.height(8.dp))

                        // sends the selected food to MainActivity
                        Button(
                            onClick = {
                                onAddFood(food)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Food")
                        }
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
    BreakfastScreen(
        onBack = {},
        onAddFood = {}
    )
}
