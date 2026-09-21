package com.example.cst438project1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import com.example.cst438project1.database.MealLogDao
import com.example.cst438project1.database.MealLogEntry
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

@Composable
internal fun rememberToday(): LocalDate {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var today by remember { mutableStateOf(LocalDate.now()) }
    DisposableEffect(context, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) today = LocalDate.now()
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                today = LocalDate.now()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        lifecycle.addObserver(observer)
        onDispose {
            context.unregisterReceiver(receiver)
            lifecycle.removeObserver(observer)
        }
    }
    return today
}

@Composable
internal fun MealLogContent(
    dao: MealLogDao,
    userId: Int,
    start: LocalDate,
    end: LocalDate,
    content: @Composable (List<MealLogEntry>) -> Unit
) {
    var retry by remember { mutableStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    key(dao, userId, start, end, retry) {
        val result by produceState<Result<List<MealLogEntry>>?>(null, lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeLog(dao, userId, start, end) { value = it }
            }
        }
        when {
            result == null -> CircularProgressIndicator()
            result!!.isFailure -> Column {
                Text("Could not load your food records.")
                TextButton(onClick = { retry++ }) { Text("Retry") }
            }
            else -> content(result!!.getOrThrow())
        }
    }
}

// Room can report IO, migration, or decoding failures; none mean an empty log.
@Suppress("TooGenericExceptionCaught")
private suspend fun observeLog(
    dao: MealLogDao,
    userId: Int,
    start: LocalDate,
    end: LocalDate,
    onResult: (Result<List<MealLogEntry>>) -> Unit
) {
    try {
        dao.observe(userId, start.toEpochDay(), end.toEpochDay()).collect {
            onResult(Result.success(it))
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        onResult(Result.failure(error))
    }
}
