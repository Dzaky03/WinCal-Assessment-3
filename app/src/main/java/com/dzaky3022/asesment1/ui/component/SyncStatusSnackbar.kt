package com.dzaky3022.asesment1.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dzaky3022.asesment1.SyncEvent
import com.dzaky3022.asesment1.SyncManager
import com.dzaky3022.asesment1.SyncStatus
import com.dzaky3022.asesment1.SyncWorker

@Composable
fun SyncStatusSnackbar(
    syncManager: SyncManager?,
    snackbarHostState: SnackbarHostState,
) {
    // Collect sync status from SyncManager
    val syncEvent by syncManager?.syncStatusFlow?.collectAsStateWithLifecycle(
        initialValue = SyncEvent(SyncStatus.IDLE, "")
    ) ?: remember { mutableStateOf(SyncEvent(SyncStatus.IDLE, "")) }

    // Also observe WorkManager status for background sync
    val context = androidx.compose.ui.platform.LocalContext.current
    val workManager = WorkManager.getInstance(context)

    val workInfos by workManager.getWorkInfosByTagLiveData("periodic_sync_work").observeAsState(emptyList())

    // Handle work status changes
    LaunchedEffect(workInfos) {
        workInfos.forEach { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.RUNNING -> {
                    val message = workInfo.progress.getString(SyncWorker.KEY_SYNC_MESSAGE)
                        ?: "Syncing in background..."

                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
                WorkInfo.State.SUCCEEDED -> {
                    val syncedCount = workInfo.outputData.getInt(SyncWorker.KEY_SYNCED_COUNT, 0)
                    val message = workInfo.outputData.getString(SyncWorker.KEY_SYNC_MESSAGE)
                        ?: if (syncedCount > 0) "Synced successfully" else "All data up to date"

                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
                WorkInfo.State.FAILED -> {
                    val errorMessage = workInfo.outputData.getString(SyncWorker.KEY_ERROR_MESSAGE)
                        ?: "Sync failed"

                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Long
                    )
                }
                else -> { /* Handle other states if needed */ }
            }
        }
    }

    // Handle manual sync events
    LaunchedEffect(syncEvent) {
        when (syncEvent.status) {
            SyncStatus.SYNCING -> {
                snackbarHostState.showSnackbar(
                    message = syncEvent.message,
                    duration = SnackbarDuration.Short
                )
            }
            SyncStatus.SUCCESS -> {
                snackbarHostState.showSnackbar(
                    message = syncEvent.message,
                    duration = SnackbarDuration.Short
                )
            }
            SyncStatus.FAILED -> {
                snackbarHostState.showSnackbar(
                    message = syncEvent.message,
                    duration = SnackbarDuration.Long
                )
            }
            SyncStatus.IDLE -> {
                // Do nothing for idle state
            }
        }
    }
}

// Enhanced snackbar with custom styling for sync status
@Composable
fun SyncStatusSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        CustomSyncSnackbar(
            message = snackbarData.visuals.message,
            actionLabel = snackbarData.visuals.actionLabel,
            onActionClick = { snackbarData.performAction() },
            onDismiss = { snackbarData.dismiss() }
        )
    }
}

@Composable
private fun CustomSyncSnackbar(
    message: String,
    actionLabel: String?,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        message.contains("syncing", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        message.contains("success", ignoreCase = true) || message.contains("completed", ignoreCase = true) ->
            Color(0xFF4CAF50) // Green
        message.contains("failed", ignoreCase = true) || message.contains("error", ignoreCase = true) ->
            MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.inverseSurface
    }

    val contentColor = when {
        message.contains("syncing", ignoreCase = true) -> MaterialTheme.colorScheme.onPrimary
        message.contains("success", ignoreCase = true) || message.contains("completed", ignoreCase = true) ->
            Color.White
        message.contains("failed", ignoreCase = true) || message.contains("error", ignoreCase = true) ->
            MaterialTheme.colorScheme.onError
        else -> MaterialTheme.colorScheme.inverseOnSurface
    }

    Snackbar(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        action = actionLabel?.let {
            {
                TextButton(
                    onClick = onActionClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text(actionLabel)
                }
            }
        },
        dismissAction = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor
                )
            ) {
                Text("Dismiss")
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add loading indicator for syncing state
            if (message.contains("syncing", ignoreCase = true)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}