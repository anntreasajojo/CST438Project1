package com.example.cst438project1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cst438project1.ui.theme.CST438Project1Theme

// Targets the rest of the app measures against.
data class Profile(
    val calorieGoal: Int = CALORIE_GOAL,
    val carbGoal: Int = 250,
    val proteinGoal: Int = 120,
    val fatGoal: Int = 65
)

@Composable
fun rememberProfile(): MutableState<Profile> = remember { mutableStateOf(Profile()) }

@Composable
fun ProfileScreen(
    profile: MutableState<Profile>,
    today: Macros = Macros(0, 0, 0, 0)
) {
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val current = profile.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 40.dp)
    ) {
        Text(
            text = "PROFILE",
            fontFamily = Mono,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = muted
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Daily targets",
            fontFamily = Display,
            fontSize = 34.sp,
            color = ink
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Today measures against these.",
            fontSize = 13.sp,
            color = muted
        )
        Spacer(Modifier.height(28.dp))
        Hairline()
        Spacer(Modifier.height(24.dp))
        GoalField("Calories", current.calorieGoal, Modifier.fillMaxWidth()) {
            profile.value = current.copy(calorieGoal = it)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoalField("Carbs g", current.carbGoal, Modifier.weight(1f)) {
                profile.value = current.copy(carbGoal = it)
            }
            GoalField("Protein g", current.proteinGoal, Modifier.weight(1f)) {
                profile.value = current.copy(proteinGoal = it)
            }
            GoalField("Fat g", current.fatGoal, Modifier.weight(1f)) {
                profile.value = current.copy(fatGoal = it)
            }
        }

        Spacer(Modifier.height(32.dp))
        Text(
            text = "TODAY",
            fontFamily = Mono,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = muted
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${today.calories.grouped()} of ${current.calorieGoal.grouped()} kcal",
            fontFamily = Mono,
            fontSize = 15.sp,
            color = ink
        )
        Spacer(Modifier.height(10.dp))
        ProgressRule(
            if (current.calorieGoal > 0) today.calories.toFloat() / current.calorieGoal else 0f
        )
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            MacroStat("Carbs", today.carbs, Modifier.weight(1f))
            VerticalHairline()
            MacroStat("Protein", today.protein, Modifier.weight(1f))
            VerticalHairline()
            MacroStat("Fat", today.fat, Modifier.weight(1f))
        }
    }
}

@Composable
private fun GoalField(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit
) {
    // Local text so the field can be cleared while typing instead of snapping to 0.
    var text by remember { mutableStateOf(value.toString()) }
    Field(
        value = text,
        onValueChange = {
            text = it.digits()
            onChange(text.toIntOrNull() ?: 0)
        },
        label = label,
        accent = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
        numeric = true
    )
}

@Composable
private fun Hairline() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun ProfileScreenPreview() {
    CST438Project1Theme {
        ProfileScreen(
            profile = rememberProfile(),
            today = Macros(1240, 148, 62, 41)
        )
    }
}
