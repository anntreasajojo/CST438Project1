package com.example.cst438project1

import android.database.sqlite.SQLiteConstraintException
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cst438project1.database.Activity
import com.example.cst438project1.database.Goal
import com.example.cst438project1.database.Sex
import com.example.cst438project1.database.User
import com.example.cst438project1.database.UserDao
import com.example.cst438project1.ui.theme.CST438Project1Theme
import com.example.cst438project1.ui.theme.MorningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Validation lives outside the composables so it can be tested without a screen.
// Each returns the message to show, or null when the value is fine.

internal fun usernameError(value: String): String? = when {
    value.isEmpty() -> null
    value.length < 3 -> "At least 3 characters."
    value.length > 20 -> "At most 20 characters."
    !value.all { it.isLetterOrDigit() || it == '_' } -> "Letters, numbers and _ only."
    else -> null
}

internal fun passwordError(value: String): String? = when {
    value.isEmpty() -> null
    value.length < 8 -> "At least 8 characters."
    else -> null
}

internal fun confirmError(password: String, confirm: String): String? = when {
    confirm.isEmpty() -> null
    confirm != password -> "Passwords do not match."
    else -> null
}

// An empty field shows no message, so completeness has to check for content
// separately from checking for errors.
internal fun accountIsComplete(username: String, password: String, confirm: String) =
    username.isNotEmpty() && password.isNotEmpty() && confirm.isNotEmpty() &&
        usernameError(username) == null && passwordError(password) == null &&
        confirmError(password, confirm) == null

// Ranges wide enough not to argue with anyone, narrow enough to catch a typo
// that would throw the calorie estimate off by a factor of ten.
internal val AGE_RANGE = 13..120
internal val HEIGHT_RANGE = 100..250
internal val WEIGHT_RANGE = 30..300

internal fun rangeError(value: String, range: IntRange, unit: String): String? {
    if (value.isEmpty()) return null
    val number = value.toIntOrNull() ?: return "Numbers only."
    return if (number in range) null else "${range.first}-${range.last} $unit."
}

// Which of the four target fields the user has typed into. Those keep their
// value when the onboarding answers change.
private enum class Target { CALORIES, CARBS, PROTEIN, FAT }

@Composable
fun RegisterScreen(
    dao: UserDao,
    onRegistered: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var onboarding by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var takenName by remember { mutableStateOf<String?>(null) }

    var age by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(Sex.MALE) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf(Activity.MODERATE) }
    var goal by remember { mutableStateOf(Goal.MAINTAIN) }

    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val edited = remember { mutableStateListOf<Target>() }

    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val bodyIsComplete = rangeError(age, AGE_RANGE, "") == null && age.isNotEmpty() &&
        rangeError(height, HEIGHT_RANGE, "") == null && height.isNotEmpty() &&
        rangeError(weight, WEIGHT_RANGE, "") == null && weight.isNotEmpty()

    // Recalculate whenever an answer changes, but never overwrite a field the
    // user has taken over.
    LaunchedEffect(age, sex, height, weight, activity, goal, bodyIsComplete, edited.size) {
        if (!bodyIsComplete) return@LaunchedEffect
        val suggested = targets(
            sex = sex,
            weightKg = weight.toInt(),
            heightCm = height.toInt(),
            age = age.toInt(),
            activity = activity,
            goal = goal
        )
        if (Target.CALORIES !in edited) calories = suggested.calorieGoal.toString()
        if (Target.CARBS !in edited) carbs = suggested.carbGoal.toString()
        if (Target.PROTEIN !in edited) protein = suggested.proteinGoal.toString()
        if (Target.FAT !in edited) fat = suggested.fatGoal.toString()
    }

    BackHandler(enabled = onboarding && !busy) { onboarding = false }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 40.dp)
    ) {
        Text(
            text = if (onboarding) "STEP 2 OF 2" else "STEP 1 OF 2",
            fontFamily = Mono,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (onboarding) "About you" else "Create an account",
            fontFamily = Display,
            fontSize = 34.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (onboarding) {
                "These set your daily targets. You can change them below."
            } else {
                "Your food log is kept on this phone."
            },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        Hairline()
        Spacer(Modifier.height(24.dp))

        if (onboarding) {
            OnboardingStep(
                age = age, onAge = { age = it.digits() },
                sex = sex, onSex = { sex = it },
                height = height, onHeight = { height = it.digits() },
                weight = weight, onWeight = { weight = it.digits() },
                activity = activity, onActivity = { activity = it },
                goal = goal, onGoal = { goal = it },
                calories = calories, carbs = carbs, protein = protein, fat = fat,
                onTarget = { target, text ->
                    if (target !in edited) edited.add(target)
                    when (target) {
                        Target.CALORIES -> calories = text
                        Target.CARBS -> carbs = text
                        Target.PROTEIN -> protein = text
                        Target.FAT -> fat = text
                    }
                },
                anyEdited = edited.isNotEmpty(),
                onRecalculate = { edited.clear() },
                enabled = !busy
            )

            Spacer(Modifier.height(28.dp))
            PrimaryButton(
                text = if (busy) "Creating account..." else "Create account",
                enabled = bodyIsComplete && calories.isNotEmpty() && !busy,
                onClick = {
                    busy = true
                    scope.launch {
                        // PBKDF2 is deliberately slow, so it does not run on the
                        // thread drawing the screen.
                        val salt = withContext(Dispatchers.Default) { PasswordHasher.newSalt() }
                        val hash = withContext(Dispatchers.Default) {
                            PasswordHasher.hash(password, salt)
                        }
                        try {
                            val id = dao.insertUser(
                                User(
                                    username = username,
                                    passwordHash = hash,
                                    salt = salt,
                                    age = age.toInt(),
                                    sex = sex,
                                    heightCm = height.toInt(),
                                    weightKg = weight.toInt(),
                                    activity = activity,
                                    goal = goal,
                                    calorieGoal = calories.toIntOrNull() ?: 0,
                                    carbGoal = carbs.toIntOrNull() ?: 0,
                                    proteinGoal = protein.toIntOrNull() ?: 0,
                                    fatGoal = fat.toIntOrNull() ?: 0
                                )
                            )
                            onRegistered(id.toInt())
                        } catch (e: SQLiteConstraintException) {
                            // Someone took the name between the check and here.
                            takenName = username
                            onboarding = false
                        } finally {
                            busy = false
                        }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { onboarding = false },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            AccountStep(
                username = username,
                onUsername = { username = it; takenName = null },
                password = password, onPassword = { password = it },
                confirm = confirm, onConfirm = { confirm = it },
                takenName = takenName
            )

            Spacer(Modifier.height(28.dp))
            PrimaryButton(
                text = "Next",
                enabled = accountIsComplete(username, password, confirm) &&
                    username != takenName && !busy,
                onClick = {
                    scope.launch {
                        // Catch the duplicate now rather than after the user has
                        // filled in the whole second step.
                        if (dao.countByUsername(username) > 0) {
                            takenName = username
                        } else {
                            onboarding = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun AccountStep(
    username: String,
    onUsername: (String) -> Unit,
    password: String,
    onPassword: (String) -> Unit,
    confirm: String,
    onConfirm: (String) -> Unit,
    takenName: String?
) {
    var reveal by remember { mutableStateOf(false) }

    LabelledField(
        value = username,
        onValueChange = onUsername,
        label = "Username",
        error = if (username == takenName && username.isNotEmpty()) {
            "That username is taken."
        } else {
            usernameError(username)
        }
    )
    Spacer(Modifier.height(16.dp))
    LabelledField(
        value = password,
        onValueChange = onPassword,
        label = "Password",
        error = passwordError(password),
        masked = !reveal
    )
    Spacer(Modifier.height(4.dp))
    TextButton(onClick = { reveal = !reveal }) {
        Text(
            text = if (reveal) "Hide passwords" else "Show passwords",
            fontSize = 12.sp,
            color = MorningAmber
        )
    }
    Spacer(Modifier.height(8.dp))
    LabelledField(
        value = confirm,
        onValueChange = onConfirm,
        label = "Re-enter password",
        error = confirmError(password, confirm),
        masked = !reveal
    )
}

@Composable
private fun OnboardingStep(
    age: String, onAge: (String) -> Unit,
    sex: Sex, onSex: (Sex) -> Unit,
    height: String, onHeight: (String) -> Unit,
    weight: String, onWeight: (String) -> Unit,
    activity: Activity, onActivity: (Activity) -> Unit,
    goal: Goal, onGoal: (Goal) -> Unit,
    calories: String, carbs: String, protein: String, fat: String,
    onTarget: (Target, String) -> Unit,
    anyEdited: Boolean,
    onRecalculate: () -> Unit,
    enabled: Boolean
) {
    LabelledField(
        value = age,
        onValueChange = onAge,
        label = "Age",
        error = rangeError(age, AGE_RANGE, "years"),
        numeric = true
    )
    Spacer(Modifier.height(20.dp))
    Choice("Sex", Sex.entries, sex, onSex) { it.readable() }

    Spacer(Modifier.height(20.dp))
    LabelledField(
        value = height,
        onValueChange = onHeight,
        label = "Height cm",
        error = rangeError(height, HEIGHT_RANGE, "cm"),
        numeric = true
    )
    Spacer(Modifier.height(16.dp))
    LabelledField(
        value = weight,
        onValueChange = onWeight,
        label = "Weight kg",
        error = rangeError(weight, WEIGHT_RANGE, "kg"),
        numeric = true
    )

    Spacer(Modifier.height(20.dp))
    Choice("Activity", Activity.entries, activity, onActivity) { it.readable() }
    Spacer(Modifier.height(20.dp))
    Choice("Goal", Goal.entries, goal, onGoal) { it.readable() }

    Spacer(Modifier.height(28.dp))
    Hairline()
    Spacer(Modifier.height(24.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "DAILY TARGETS",
            fontFamily = Mono,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (anyEdited) {
            TextButton(onClick = onRecalculate, enabled = enabled) {
                Text("Recalculate", fontSize = 12.sp, color = MorningAmber)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    LabelledField(
        value = calories,
        onValueChange = { onTarget(Target.CALORIES, it.digits()) },
        label = "Calories",
        error = null,
        numeric = true
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelledField(
            value = carbs,
            onValueChange = { onTarget(Target.CARBS, it.digits()) },
            label = "Carbs g",
            error = null,
            numeric = true,
            modifier = Modifier.weight(1f)
        )
        LabelledField(
            value = protein,
            onValueChange = { onTarget(Target.PROTEIN, it.digits()) },
            label = "Protein g",
            error = null,
            numeric = true,
            modifier = Modifier.weight(1f)
        )
        LabelledField(
            value = fat,
            onValueChange = { onTarget(Target.FAT, it.digits()) },
            label = "Fat g",
            error = null,
            numeric = true,
            modifier = Modifier.weight(1f)
        )
    }
}

// Field plus the message under it, so no caller has to lay that out again.
@Composable
private fun LabelledField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    numeric: Boolean = false,
    masked: Boolean = false
) {
    Column(modifier) {
        if (masked) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label, fontSize = 12.sp) },
                singleLine = true,
                isError = error != null,
                shape = RoundedCornerShape(4.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MorningAmber,
                    focusedLabelColor = MorningAmber,
                    cursorColor = MorningAmber,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Field(
                value = value,
                onValueChange = onValueChange,
                label = label,
                accent = MorningAmber,
                modifier = Modifier.fillMaxWidth(),
                numeric = numeric
            )
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = error,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// One row of pills. Wrapping means the same component handles three short
// options and five long ones.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Choice(
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    readable: (T) -> String
) {
    Text(
        text = label.uppercase(Locale.getDefault()),
        fontFamily = Mono,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(10.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val chosen = option == selected
            Box(
                Modifier
                    .padding(bottom = 8.dp)
                    .border(
                        width = 1.dp,
                        color = if (chosen) MorningAmber else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = if (chosen) MorningAmber.copy(alpha = 0.12f) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = readable(option),
                    fontSize = 13.sp,
                    color = if (chosen) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

internal fun Sex.readable() = when (this) {
    Sex.MALE -> "Male"
    Sex.FEMALE -> "Female"
    Sex.OTHER -> "Other"
}

internal fun Activity.readable() = when (this) {
    Activity.SEDENTARY -> "Sedentary"
    Activity.LIGHT -> "Light"
    Activity.MODERATE -> "Moderate"
    Activity.ACTIVE -> "Active"
    Activity.VERY_ACTIVE -> "Very active"
}

internal fun Goal.readable() = when (this) {
    Goal.LOSE -> "Lose weight"
    Goal.MAINTAIN -> "Maintain"
    Goal.GAIN -> "Gain weight"
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun RegisterScreenPreview() {
    CST438Project1Theme {
        // The preview has no database, so it shows the first step only.
        RegisterScreen(dao = NoopUserDao, onRegistered = {})
    }
}

// Lets the preview render without building a database.
private object NoopUserDao : UserDao {
    override suspend fun insertUser(user: User) = 1L
    override suspend fun getUserById(id: Int): User? = null
    override suspend fun getUserByUsername(username: String): User? = null
    override suspend fun countByUsername(username: String) = 0
    override suspend fun count() = 0
    override suspend fun latestUser(): User? = null
    override suspend fun deleteUser(user: User) = Unit
}
