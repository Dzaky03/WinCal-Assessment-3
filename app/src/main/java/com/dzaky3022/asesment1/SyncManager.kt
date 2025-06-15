package com.dzaky3022.asesment1

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.dzaky3022.asesment1.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import java.util.concurrent.TimeUnit

// Enum for sync status
enum class SyncStatus {
    SYNCING,
    SUCCESS,
    FAILED,
    IDLE
}

// Data class for sync events
data class SyncEvent(
    val status: SyncStatus,
    val message: String,
    val syncedCount: Int = 0,
    val error: String? = null
)

class SyncManager(
    context: Context,
//    private val repository: WaterResultRepository?
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val PERIODIC_SYNC_WORK = "periodic_sync_work"
        private const val IMMEDIATE_SYNC_WORK = "immediate_sync_work"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val workManager = WorkManager.getInstance(context)

    // Sync status flow
    private val _syncStatusFlow = MutableSharedFlow<SyncEvent>(replay = 0)
    val syncStatusFlow: SharedFlow<SyncEvent> = _syncStatusFlow.asSharedFlow()

    init {
        Log.d(TAG, "SyncManager initialized")
        // Start periodic sync
        startPeriodicSync()

        // Listen for network changes and sync when connected
        observeNetworkChanges()
    }

    private fun startPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresStorageNotLow(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.SECONDS,
            15, TimeUnit.SECONDS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(PERIODIC_SYNC_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )

        Log.d(TAG, "Periodic sync work scheduled (every 15 seconds)")
    }

    private fun observeNetworkChanges() {
        NetworkUtils.isConnected
            .onEach { isConnected ->
                if (isConnected) {
                    Log.d(TAG, "Network connected - triggering immediate sync")
                    triggerImmediateSync()
                } else {
                    Log.d(TAG, "Network disconnected")
                }
            }
            .launchIn(scope)
    }

    fun triggerImmediateSync() {
        // Cancel any existing immediate sync work to avoid duplicates
        workManager.cancelAllWorkByTag(IMMEDIATE_SYNC_WORK)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(IMMEDIATE_SYNC_WORK)
            .build()

        workManager.enqueue(immediateWorkRequest)
        Log.d(TAG, "Immediate sync work enqueued")
    }

//    fun manualSync() {
//        scope.launch {
//            try {
//                if (NetworkUtils.isNetworkAvailable(context)) {
//                    Log.d(TAG, "Starting manual sync...")
//
//                    // Emit syncing status
//                    _syncStatusFlow.emit(SyncEvent(
//                        status = SyncStatus.SYNCING,
//                        message = "Syncing data..."
//                    ))
//
//                    // Perform sync
//                    repository?.refreshFromNetwork()
//
//                    // Emit success status
//                    _syncStatusFlow.emit(SyncEvent(
//                        status = SyncStatus.SUCCESS,
//                        message = "Sync completed successfully"
//                    ))
//
//                    Log.d(TAG, "Manual sync completed")
//                } else {
//                    Log.d(TAG, "No network available for manual sync")
//
//                    // Emit failure status
//                    _syncStatusFlow.emit(SyncEvent(
//                        status = SyncStatus.FAILED,
//                        message = "No internet connection",
//                        error = "Network unavailable"
//                    ))
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Manual sync failed: ${e.message}", e)
//
//                // Emit failure status
//                _syncStatusFlow.emit(SyncEvent(
//                    status = SyncStatus.FAILED,
//                    message = "Sync failed",
//                    error = e.message
//                ))
//            }
//        }
//    }

    // Method to check for pending sync items and emit status
//    fun checkPendingSyncItems() {
//        scope.launch {
//            try {
//                repository?.let { repo ->
//                    // Check if there are any pending sync items
//                    val hasPendingItems = repo.hasPendingSyncItems()
//
//                    if (hasPendingItems) {
//                        Log.d(TAG, "Found pending sync items, triggering sync...")
//
//                        // Emit syncing status
//                        _syncStatusFlow.emit(SyncEvent(
//                            status = SyncStatus.SYNCING,
//                            message = "Syncing pending changes..."
//                        ))
//
//                        // Trigger immediate sync
//                        triggerImmediateSync()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error checking pending sync items: ${e.message}", e)
//            }
//        }
//    }

    // Method to emit sync status from WorkManager
//    fun emitSyncStatus(event: SyncEvent) {
//        scope.launch {
//            _syncStatusFlow.emit(event)
//        }
//    }

    fun stopPeriodicSync() {
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK)
        workManager.cancelAllWorkByTag(IMMEDIATE_SYNC_WORK)
        scope.coroutineContext.job.cancel()
        Log.d(TAG, "All sync work cancelled")
    }

//    fun getWorkInfos() = workManager.getWorkInfosByTagLiveData(PERIODIC_SYNC_WORK)
}