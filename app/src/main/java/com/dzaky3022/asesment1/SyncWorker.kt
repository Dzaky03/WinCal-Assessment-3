package com.dzaky3022.asesment1

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dzaky3022.asesment1.database.AppDatabase
import com.dzaky3022.asesment1.network.WaterApi
import com.dzaky3022.asesment1.repository.WaterResultRepository
import com.dzaky3022.asesment1.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "SyncWorker"

        // Keys for work data output
        const val KEY_SYNC_STATUS = "sync_status"
        const val KEY_SYNC_MESSAGE = "sync_message"
        const val KEY_SYNCED_COUNT = "synced_count"
        const val KEY_ERROR_MESSAGE = "error_message"

        // Status values
        const val STATUS_SYNCING = "syncing"
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILED = "failed"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting sync work...")

            // Set initial syncing status
            setProgressAsync(workDataOf(
                KEY_SYNC_STATUS to STATUS_SYNCING,
                KEY_SYNC_MESSAGE to "Checking authentication..."
            ))

            // Check if user is authenticated
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.d(TAG, "No authenticated user, skipping sync")
                return Result.success(workDataOf(
                    KEY_SYNC_STATUS to STATUS_SUCCESS,
                    KEY_SYNC_MESSAGE to "No user authenticated"
                ))
            }

            // Update progress
            setProgressAsync(workDataOf(
                KEY_SYNC_STATUS to STATUS_SYNCING,
                KEY_SYNC_MESSAGE to "Checking network connectivity..."
            ))

            // Check network connectivity
            if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
                Log.d(TAG, "No network connection, will retry when network is available")
                return Result.retry()
            }

            // Update progress
            setProgressAsync(workDataOf(
                KEY_SYNC_STATUS to STATUS_SYNCING,
                KEY_SYNC_MESSAGE to "Initializing sync..."
            ))

            // Initialize API if not already done
            if (!WaterApi.isInitialized) {
                WaterApi.initialize(currentUser.uid)
                Log.d(TAG, "WaterApi initialized for user: ${currentUser.uid}")
            }

            // Get repository
            val database = AppDatabase.getAppDb(applicationContext)
            val repository = WaterResultRepository(
                context = applicationContext,
                uid = currentUser.uid,
                dao = database.waterResultDao(),
                apiService = WaterApi.service
            )

            // Check if there are pending sync items
            val hasPendingItems = repository.hasPendingSyncItems()

            if (!hasPendingItems) {
                Log.d(TAG, "No pending sync items found")
                return Result.success(workDataOf(
                    KEY_SYNC_STATUS to STATUS_SUCCESS,
                    KEY_SYNC_MESSAGE to "All data is up to date",
                    KEY_SYNCED_COUNT to 0
                ))
            }

            // Update progress
            setProgressAsync(workDataOf(
                KEY_SYNC_STATUS to STATUS_SYNCING,
                KEY_SYNC_MESSAGE to "Syncing data..."
            ))

            // Perform sync operations
            Log.d(TAG, "Refreshing data from network...")
            repository.refreshFromNetwork()

            // Get final sync count (this would need to be implemented in repository)
            val syncedCount = repository.getLastSyncCount()

            Log.d(TAG, "Sync work completed successfully, synced: $syncedCount items")

            Result.success(workDataOf(
                KEY_SYNC_STATUS to STATUS_SUCCESS,
                KEY_SYNC_MESSAGE to if (syncedCount > 0) "Synced $syncedCount items successfully" else "All data is up to date",
                KEY_SYNCED_COUNT to syncedCount
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Sync work failed: ${e.message}", e)

            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No internet connection"
                is java.net.SocketTimeoutException -> "Connection timeout"
                is java.io.IOException -> "Network error"
                else -> "Sync failed: ${e.message}"
            }

            // Return retry for recoverable errors, failure for permanent errors
            when (e) {
                is java.net.UnknownHostException,
                is java.net.SocketTimeoutException,
                is java.io.IOException -> {
                    Log.d(TAG, "Network error, will retry")
                    Result.retry()
                }
                else -> {
                    Log.e(TAG, "Non-recoverable error, failing")
                    Result.failure(workDataOf(
                        KEY_SYNC_STATUS to STATUS_FAILED,
                        KEY_SYNC_MESSAGE to errorMessage,
                        KEY_ERROR_MESSAGE to e.message
                    ))
                }
            }
        }
    }
}