package com.example.cst438project1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cst438project1.database.User
import com.example.cst438project1.database.UserDao
import com.example.cst438project1.ui.theme.CST438Project1Theme
import com.example.cst438project1.ui.theme.MorningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Kept out of the composable so it can be tested without a screen.
// Returns the account when the password matches, null otherwise.
internal suspend fun authenticate(dao: UserDao, username: String, password: String): User? {
    val user = dao.getUserByUsername(username)
    // PBKDF2 is slow on purpose, so it does not run on the thread drawing the screen.
    return withContext(Dispatchers.Default) {
        if (user == null) {
            // Hash anyway, so an unknown username takes as long as a wrong
            // password and response time does not reveal which names exist.
            PasswordHasher.hash(password, PasswordHasher.newSalt())
            null
        } else {
            user.takeIf { PasswordHasher.verify(password, it.salt, it.passwordHash) }
        }
    }
}

internal const val LOGIN_FAILED = "Username or password is incorrect."

@Composable
fun LoginScreen(
    dao: UserDao,
    onLoggedIn: (User) -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var failed by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 40.dp)
    ) {
        LoginHeader()

        // No sign-up rules here: they would only tell a guesser what a valid
        // name looks like. Editing either field clears a failed attempt.
        LabelledField(
            value = username,
            onValueChange = { username = it; failed = false },
            label = "Username",
            error = null
        )
        Spacer(Modifier.height(16.dp))
        LabelledField(
            value = password,
            onValueChange = { password = it; failed = false },
            label = "Password",
            // One message for both cases, so it does not say which one was wrong.
            error = if (failed) LOGIN_FAILED else null,
            masked = true
        )

        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            text = if (busy) "Logging in..." else "Log in",
            enabled = username.isNotEmpty() && password.isNotEmpty() && !busy,
            onClick = {
                busy = true
                scope.launch {
                    try {
                        val user = authenticate(dao, username, password)
                        if (user == null) failed = true else onLoggedIn(user)
                    } finally {
                        busy = false
                    }
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onRegister,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create an account", fontSize = 13.sp, color = MorningAmber)
        }
    }
}

@Composable
private fun LoginHeader() {
    Text(
        text = "LOG IN",
        fontFamily = Mono,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = "Welcome back",
        fontFamily = Display,
        fontSize = 34.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "Use the account you created on this phone.",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(28.dp))
    Hairline()
    Spacer(Modifier.height(24.dp))
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CST438Project1Theme {
        LoginScreen(dao = NoopUserDao, onLoggedIn = {}, onRegister = {})
    }
}

// Field plus the message under it, so no caller has to lay that out again.
@Composable
internal fun LabelledField(
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

@Composable
internal fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
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
