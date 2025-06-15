package com.dzaky3022.asesment1.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dzaky3022.asesment1.SyncEvent
import com.dzaky3022.asesment1.SyncManager
import com.dzaky3022.asesment1.SyncStatus

@Composable
fun SyncStatusSnackbar(
    syncManager: SyncManager?,
//    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)

    // Track the last processed work ID to avoid duplicate snackbars
    var lastProcessedWorkId by remember { mutableStateOf<String?>(null) }

    // Observe both periodic and immediate sync work
    val periodicWorkInfos by workManager.getWorkInfosByTagLiveData("periodic_sync_work").observeAsState(emptyList())
    val immediateWorkInfos by workManager.getWorkInfosByTagLiveData("immediate_sync_work").observeAsState(emptyList())

    // Combine both work info lists
    val allWorkInfos = (periodicWorkInfos + immediateWorkInfos).distinctBy { it.id }

    // Handle work status changes
    LaunchedEffect(allWorkInfos) {
        // Get the most recent work info that has a state change
        val currentWorkInfo = allWorkInfos
            .filter { it.state != WorkInfo.State.ENQUEUED } // Skip enqueued state
            .maxByOrNull { it.runAttemptCount }

        currentWorkInfo?.let { workInfo ->
            // Only process if this is a new work or state change
            if (lastProcessedWorkId != workInfo.id.toString() + workInfo.state.name) {
                lastProcessedWorkId = workInfo.id.toString() + workInfo.state.name

                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
//                        val message = workInfo.progress.getString(SyncWorker.KEY_SYNC_MESSAGE)
//                            ?: "Syncing data..."

//                        snackbarHostState.showSnackbar(
//                            message = message,
//                            duration = SnackbarDuration.Short
//                        )
                    }
                    WorkInfo.State.SUCCEEDED -> {
//                        val syncedCount = workInfo.outputData.getInt(SyncWorker.KEY_SYNCED_COUNT, 0)
//                        val message = workInfo.outputData.getString(SyncWorker.KEY_SYNC_MESSAGE) ?: ""

                        // Only show success message if there's actually a message and items were synced
//                        if (message.isNotEmpty() && (syncedCount > 0 || message.contains("completed", ignoreCase = true))) {
//                            snackbarHostState.showSnackbar(
//                                message = message,
//                                duration = SnackbarDuration.Short
//                            )
//                        }
                    }
                    WorkInfo.State.FAILED -> {
//                        val errorMessage = workInfo.outputData.getString(SyncWorker.KEY_ERROR_MESSAGE)
//                            ?: workInfo.outputData.getString(SyncWorker.KEY_SYNC_MESSAGE)
//                            ?: "Sync failed"

//                        snackbarHostState.showSnackbar(
//                            message = errorMessage,
//                            duration = SnackbarDuration.Long
//                        )
                    }
                    WorkInfo.State.BLOCKED -> {
//                        snackbarHostState.showSnackbar(
//                            message = "Sync waiting for network connection",
//                            duration = SnackbarDuration.Short
//                        )
                    }
                    else -> { /* Handle other states if needed */ }
                }
            }
        }
    }

    // Collect sync status from SyncManager for manual syncs
    val syncEvent by syncManager?.syncStatusFlow?.collectAsStateWithLifecycle(
        initialValue = SyncEvent(SyncStatus.IDLE, "")
    ) ?: remember { mutableStateOf(SyncEvent(SyncStatus.IDLE, "")) }

    // Handle manual sync events (these are separate from WorkManager)
    LaunchedEffect(syncEvent) {
        when (syncEvent.status) {
            SyncStatus.SYNCING -> {
//                if (syncEvent.message.isNotEmpty()) {
//                    snackbarHostState.showSnackbar(
//                        message = syncEvent.message,
//                        duration = SnackbarDuration.Short
//                    )
//                }
            }
            SyncStatus.SUCCESS -> {
//                if (syncEvent.message.isNotEmpty()) {
//                    snackbarHostState.showSnackbar(
//                        message = syncEvent.message,
//                        duration = SnackbarDuration.Short
//                    )
//                }
            }
            SyncStatus.FAILED -> {
//                if (syncEvent.message.isNotEmpty()) {
//                    snackbarHostState.showSnackbar(
//                        message = syncEvent.message,
//                        duration = SnackbarDuration.Long
//                    )
//                }
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
        message.contains("syncing", ignoreCase = true) || message.contains("waiting", ignoreCase = true) ->
            MaterialTheme.colorScheme.primary
        message.contains("success", ignoreCase = true) || message.contains("completed", ignoreCase = true) ||
                message.contains("up to date", ignoreCase = true) ->
            Color(0xFF4CAF50) // Green
        message.contains("failed", ignoreCase = true) || message.contains("error", ignoreCase = true) ->
            MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.inverseSurface
    }

    val contentColor = when {
        message.contains("syncing", ignoreCase = true) || message.contains("waiting", ignoreCase = true) ->
            MaterialTheme.colorScheme.onPrimary
        message.contains("success", ignoreCase = true) || message.contains("completed", ignoreCase = true) ||
                message.contains("up to date", ignoreCase = true) ->
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
            if (message.contains("syncing", ignoreCase = true) || message.contains("waiting", ignoreCase = true)) {
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