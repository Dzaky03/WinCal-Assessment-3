package com.dzaky3022.asesment1

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
        const val WORK_NAME = "periodic_sync_work"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting sync work...")

            // Check if user is authenticated
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.d(TAG, "No authenticated user, skipping sync")
                return Result.success()
            }

            // Check network connectivity
            if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
                Log.d(TAG, "No network connection, will retry when network is available")
                return Result.retry()
            }

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

            // Perform sync operations
            Log.d(TAG, "Refreshing data from network...")
            repository.refreshFromNetwork()

            Log.d(TAG, "Sync work completed successfully")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Sync work failed: ${e.message}", e)

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
                    Result.failure()
                }
            }
        }
    }
}