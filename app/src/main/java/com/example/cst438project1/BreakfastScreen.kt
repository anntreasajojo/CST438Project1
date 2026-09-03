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

// food data class
// structure for storing one food item
data class BreakfastFood(
    val name: String,
    val calories: Int
)

@Composable
fun BreakfastScreen() {
    // `foodName` stores what the user types
    // `remember` keyword is used to keep the value when screen updates
    var foodName by remember { mutableStateOf("") }

    // temporary breakfast data
    // represent breakfast items already entered by the user
    // --this list should eventually populate from database--
    val breakfastFoods = listOf(
        BreakfastFood("Banana", 105),
        BreakfastFood("Oatmeal", 150)
    )

    // this adds the calories from every food in the breakfast list
    var totalCalories = 0

    for (food in breakfastFoods){
        totalCalories = totalCalories + food.calories
    }

    // START OF THE MAIN COLUMN
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // a button to go back to previous page
        Button(onClick = { }) {
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
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Food")
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

        // our vertically scrollable list 
        LazyColumn {
            // goes through every item in breakfastFoods
            items(breakfastFoods) { food ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    // for each item in the list, show food name and calories in a card list layout
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = food.name)
                        Text(text = "${food.calories} calories")
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