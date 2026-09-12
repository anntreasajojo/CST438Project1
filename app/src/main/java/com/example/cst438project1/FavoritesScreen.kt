package com.example.cst438project1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cst438project1.ui.theme.CST438Project1Theme
import java.util.Locale

// Favorites are just foods you log often, so they reuse FoodEntry.
@Composable
fun rememberFavorites(): SnapshotStateList<FoodEntry> = remember {
    mutableStateListOf(
        FoodEntry("Greek yogurt", 130, 9, 17, 3),
        FoodEntry("Chicken breast", 284, 0, 53, 6),
        FoodEntry("Almonds, 1 oz", 164, 6, 6, 14)
    )
}

@Composable
fun FavoritesScreen(
    favorites: SnapshotStateList<FoodEntry>,
    onAddTo: (Meal, FoodEntry) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 40.dp)
    ) {
        Text(
            text = "FAVORITES",
            fontFamily = Mono,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Log again",
            fontFamily = Display,
            fontSize = 34.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Pick a food, then pick the meal it goes in.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))

        if (favorites.isEmpty()) {
            Text(
                text = "No favorites yet.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            favorites.forEach { favorite ->
                FavoriteRow(
                    favorite = favorite,
                    onAddTo = { meal -> onAddTo(meal, favorite) },
                    onRemove = { favorites.remove(favorite) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: FoodEntry,
    onAddTo: (Meal) -> Unit,
    onRemove: () -> Unit
) {
    var open by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { open = !open }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = favorite.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "C ${favorite.carbs}  P ${favorite.protein}  F ${favorite.fat}",
                    fontFamily = Mono,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${favorite.calories.grouped()} kcal",
                fontFamily = Mono,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        AnimatedVisibility(
            visible = open,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(Modifier.padding(bottom = 10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Meal.entries.forEach { meal ->
                        MealChip(
                            meal = meal,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onAddTo(meal)
                                open = false
                            }
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onRemove) {
                        Text(
                            text = "Remove",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

@Composable
private fun MealChip(meal: Meal, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .border(1.dp, meal.accent, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = meal.label.uppercase(Locale.getDefault()),
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = meal.accent
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun FavoritesScreenPreview() {
    CST438Project1Theme {
        FavoritesScreen(favorites = rememberFavorites())
    }
}
