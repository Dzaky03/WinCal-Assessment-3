package com.dzaky3022.asesment1

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.dzaky3022.asesment1.utils.NetworkUtils
import com.dzaky3022.asesment1.repository.WaterResultRepository
import kotlinx.coroutines.job
import java.util.concurrent.TimeUnit

class SyncManager(
    private val context: Context,
    private val repository: WaterResultRepository?
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val PERIODIC_SYNC_WORK = "periodic_sync_work"
        private const val IMMEDIATE_SYNC_WORK = "immediate_sync_work"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val workManager = WorkManager.getInstance(context)

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
            .setRequiresBatteryNotLow(false) // Allow sync even on low battery
            .setRequiresStorageNotLow(false) // Allow sync even on low storage
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.SECONDS,  // Every 1 minute
            15, TimeUnit.SECONDS  // Flex interval
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
            ExistingPeriodicWorkPolicy.UPDATE, // Replace existing work
            periodicWorkRequest
        )

        Log.d(TAG, "Periodic sync work scheduled (every 1 minute)")
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

    fun manualSync() {
        scope.launch {
            try {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    Log.d(TAG, "Starting manual sync...")
                    repository?.refreshFromNetwork()
                    Log.d(TAG, "Manual sync completed")
                } else {
                    Log.d(TAG, "No network available for manual sync")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Manual sync failed: ${e.message}", e)
            }
        }
    }

    fun stopPeriodicSync() {
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK)
        workManager.cancelAllWorkByTag(IMMEDIATE_SYNC_WORK)
        scope.coroutineContext.job.cancel() // Cancel coroutine scope
        Log.d(TAG, "All sync work cancelled")
    }

    fun getWorkInfos() = workManager.getWorkInfosByTagLiveData(PERIODIC_SYNC_WORK)
}