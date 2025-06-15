package com.dzaky3022.asesment1.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dzaky3022.asesment1.database.WaterResultDao
import com.dzaky3022.asesment1.network.WaterResultApi
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import com.dzaky3022.asesment1.ui.model.toEntity
import com.dzaky3022.asesment1.utils.Enums.ActivityLevel
import com.dzaky3022.asesment1.utils.Enums.Gender
import com.dzaky3022.asesment1.utils.Enums.TempUnit
import com.dzaky3022.asesment1.utils.Enums.WaterUnit
import com.dzaky3022.asesment1.utils.Enums.WeightUnit
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import java.io.File

class WaterResultRepository(
    private val dao: WaterResultDao,
    private val apiService: WaterResultApi,
    private val uid: String,
    private val context: Context,
) {
    fun getAllWaterResults(): Flow<List<WaterResultEntity>> =
        dao.getAllByUser(uid)

    private suspend fun getAllByUserSync(): List<WaterResultEntity> =
        dao.getAllByUserSync(uid)

    suspend fun getDataById(id: String): WaterResultEntity? =
        dao.getById(id)

    suspend fun clearUserData(): Int =
        dao.deleteAllByUser(uid)

    private var lastSyncCount = 0

    // Helper function to show toast on main thread
    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            // Post to main thread to ensure toast is shown properly
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, message, duration).show()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to show toast: ${e.message}")
        }
    }

    suspend fun hasPendingSyncItems(): Boolean {
        return try {
            val unsyncedResults = dao.getUnsyncedResults()
            val unsyncedUpdates = dao.getUnsyncedUpdates()
            val unsyncedDeletes = dao.getUnsyncedDeletes()

            val totalPending = unsyncedResults.size + unsyncedUpdates.size + unsyncedDeletes.size
            Log.d(
                "Repository",
                "Pending sync items: $totalPending (${unsyncedResults.size} new, ${unsyncedUpdates.size} updates, ${unsyncedDeletes.size} deletes)"
            )

            totalPending > 0
        } catch (e: Exception) {
            Log.e("Repository", "Error checking pending sync items: ${e.message}", e)
            false
        }
    }

    fun getLastSyncCount(): Int = lastSyncCount

    suspend fun refreshFromNetwork() {
        var syncRunning = false
        try {
            // Step 1: Always sync pending local changes first
            val localEntities = this.getAllByUserSync() + dao.getUnsyncedDeletes()
            val pendingLocalItems = localEntities.filter {
                it.needsSync || it.needsUpdate || it.needsDelete
            }

            if (pendingLocalItems.isNotEmpty()) {
                syncRunning = true
                showToast("Syncing ${pendingLocalItems.size} items...")

                Log.d(
                    "Repository",
                    "Found ${pendingLocalItems.size} items that need syncing, syncing first..."
                )
                val syncedCount = syncPendingResults()
                Log.d("Repository", "Synced $syncedCount items before fetching server data")
            }

            // Step 2: Fetch fresh data from server AFTER syncing local changes
            val response = apiService.getAllWaterResults()
            if (response.success) {
                val networkEntities = response.data?.map { it.toEntity() } ?: emptyList()

                // Get the updated local entities after sync
                val updatedLocalEntities = this.getAllByUserSync()

                Log.d("Repository", "Processing ${networkEntities.size} items from server")

                // Step 3: Process server data against current local state
                networkEntities.forEach { networkEntity ->
                    val existingLocal = updatedLocalEntities.find { it.id == networkEntity.id }

                    if (existingLocal == null) {
                        // New item from server - insert as synced
                        dao.insert(networkEntity.copy(isSync = true, needsSync = false))
                        Log.d("Repository", "Inserted new item from server: ${networkEntity.id}")
                    } else if (!existingLocal.needsSync && !existingLocal.needsUpdate && !existingLocal.needsDelete) {
                        // Local item is fully synced - update with server data (server is source of truth)
                        dao.insert(networkEntity.copy(isSync = true, needsSync = false))
                        Log.d(
                            "Repository",
                            "Updated synced item with server data: ${networkEntity.id}"
                        )
                    } else {
                        // Local item still has pending changes - keep local version
                        Log.d("Repository", "Keeping local changes for item: ${networkEntity.id}")
                    }
                }

                // Step 4: Handle items that exist locally but not on server
                // (These might be items that were deleted on server by another client)
                val serverIds = networkEntities.map { it.id }.toSet()
                val localOnlyItems = updatedLocalEntities.filter {
                    it.id !in serverIds && it.isSync && !it.needsDelete && !it.needsSync
                }

                if (localOnlyItems.isNotEmpty()) {
                    Log.d(
                        "Repository",
                        "Found ${localOnlyItems.size} items that exist locally but not on server"
                    )
                    localOnlyItems.forEach { localItem ->
                        // Item was deleted on server - remove from local database
                        dao.delete(localItem)
                        Log.d(
                            "Repository",
                            "Removed item that was deleted on server: ${localItem.id}"
                        )

                        // Clean up local image file if exists
                        localItem.localImagePath?.let { path ->
                            try {
                                File(path).delete()
                                Log.d("Repository", "Cleaned up image file for deleted item: $path")
                            } catch (e: Exception) {
                                Log.w("Repository", "Failed to clean up image file: $path", e)
                            }
                        }
                    }
                }

                Log.d(
                    "Repository",
                    "Successfully refreshed from network with ${networkEntities.size} items"
                )

                // Show success toast only if sync was running
                if (syncRunning) {
                    showToast("Data synced successfully!", Toast.LENGTH_SHORT)
                }
            } else {
                Log.w("Repository", "Server response was not successful")
                if (syncRunning) {
                    showToast("Sync failed: Server error", Toast.LENGTH_LONG)
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error refreshing from network: ${e.message}", e)
            if (syncRunning) {
                showToast("Sync failed: ${e.message?.take(50) ?: "Unknown error"}", Toast.LENGTH_LONG)
            }
            // Don't rethrow - just log and continue with local data
        }
    }

    suspend fun createWaterResult(
        waterResultEntity: WaterResultEntity
    ): Boolean {
        val entity = waterResultEntity.copy(
            uid = uid,
            needsSync = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

        val isOffline = dao.insert(entity) != null

        // Try to sync immediately with toast notification
        val isOnline = try {
            showToast("Creating and syncing...")
            val syncCount = syncPendingResults()
            if (syncCount > 0) {
                showToast("Item created and synced!", Toast.LENGTH_SHORT)
            }
            syncCount > 0
        } catch (e: Exception) {
            showToast("Item saved locally, will sync later", Toast.LENGTH_SHORT)
            false
        }

        return isOnline || isOffline
    }

    suspend fun updateWaterResult(
        waterResultEntity: WaterResultEntity
    ): Boolean {
        val existing = dao.getById(waterResultEntity.id) ?: return false

        val updatedLocalImagePath = when {
            // If deleteImage is true, clear the local image path
            waterResultEntity.deleteImage -> null
            // If new image is provided, use it
            waterResultEntity.localImagePath != null -> waterResultEntity.localImagePath
            // Otherwise keep existing
            else -> existing.localImagePath
        }
        val updated = existing.copy(
            title = waterResultEntity.title ?: existing.title,
            description = waterResultEntity.description,
            roomTemp = waterResultEntity.roomTemp ?: existing.roomTemp,
            tempUnit = waterResultEntity.tempUnit ?: existing.tempUnit,
            weight = waterResultEntity.weight ?: existing.weight,
            weightUnit = waterResultEntity.weightUnit ?: existing.weightUnit,
            activityLevel = waterResultEntity.activityLevel ?: existing.activityLevel,
            drinkAmount = waterResultEntity.drinkAmount ?: existing.drinkAmount,
            waterUnit = waterResultEntity.waterUnit ?: existing.waterUnit,
            resultValue = waterResultEntity.resultValue ?: existing.resultValue,
            percentage = waterResultEntity.percentage ?: existing.percentage,
            gender = waterResultEntity.gender ?: existing.gender,
            localImagePath = updatedLocalImagePath,
            deleteImage = waterResultEntity.deleteImage,
            updatedAt = Instant.now(),
            needsUpdate = true, // Mark as needing server update
            isSync = false // No longer in sync with server
        )

        // If deleteImage is true and there was an existing local image, clean it up
        if (waterResultEntity.deleteImage && existing.localImagePath != null) {
            try {
                File(existing.localImagePath!!).delete()
                Log.d("Repository", "Cleaned up deleted local image: ${existing.localImagePath}")
            } catch (e: Exception) {
                Log.w("Repository", "Failed to clean up local image: ${existing.localImagePath}", e)
            }
        }

        val isOffline = dao.update(updated) > 0

        // Try to sync immediately with toast notification
        val isOnline = try {
            showToast("Updating and syncing...")
            val syncCount = syncPendingResults()
            if (syncCount > 0) {
                showToast("Item updated and synced!", Toast.LENGTH_SHORT)
            }
            syncCount > 0
        } catch (e: Exception) {
            showToast("Item updated locally, will sync later", Toast.LENGTH_SHORT)
            false
        }

        return isOnline || isOffline
    }

    suspend fun deleteWaterResult(id: String): Boolean {
        val existing = dao.getById(id) ?: return false

        if (existing.needsSync) {
            // Item was never synced to server, safe to delete locally
            val deleted = dao.delete(existing) > 0
            if (deleted) {
                showToast("Item deleted", Toast.LENGTH_SHORT)
            }
            return deleted
        } else {
            // Item exists on server, mark for deletion
            val markedForDeletion = existing.copy(
                isDeleted = true,
                needsDelete = true,
                updatedAt = Instant.now()
            )
            dao.update(markedForDeletion)

            // Try to sync deletion immediately
            try {
                showToast("Deleting and syncing...")
                val syncResult = syncPendingResults()
                Log.d("Repository", "Immediate sync attempt: $syncResult items synced")
                if (syncResult > 0) {
                    showToast("Item deleted and synced!", Toast.LENGTH_SHORT)
                }
            } catch (e: Exception) {
                Log.w("Repository", "Immediate sync failed, will retry later: ${e.message}")
                showToast("Item marked for deletion, will sync later", Toast.LENGTH_SHORT)
            }

            // Return true because we successfully marked for deletion
            // The actual server deletion will happen in background sync
            return true
        }
    }

    private suspend fun syncPendingResults(): Int {
        var syncedCount = 0
        lastSyncCount = 0

        try {
            // Sync new items (CREATE)
            val unsyncedResults = dao.getUnsyncedResults()
            Log.d("Repository", "Syncing ${unsyncedResults.size} unsynced results")

            for (entity in unsyncedResults) {
                try {
                    val response = apiService.addWaterResult(
                        title = (entity.title ?: "").toStrRequestBody(),
                        description = entity.description?.toStrRequestBody(),
                        roomTemp = (entity.roomTemp ?: 0.0).toNumRequestBody(),
                        tempUnit = (entity.tempUnit?.jsonValue
                            ?: TempUnit.Celsius.jsonValue).toStrRequestBody(),
                        weight = (entity.weight ?: 0.0).toNumRequestBody(),
                        weightUnit = (entity.weightUnit?.jsonValue
                            ?: WeightUnit.Kilogram.jsonValue).toStrRequestBody(),
                        activityLevel = (entity.activityLevel?.jsonValue
                            ?: ActivityLevel.Low.jsonValue).toStrRequestBody(),
                        drinkAmount = (entity.drinkAmount ?: 0.0).toNumRequestBody(),
                        waterUnit = (entity.waterUnit?.jsonValue
                            ?: WaterUnit.Ml.jsonValue).toStrRequestBody(),
                        resultValue = (entity.resultValue ?: 0.0).toNumRequestBody(),
                        percentage = (entity.percentage ?: 0.0).toNumRequestBody(),
                        gender = (entity.gender?.jsonValue
                            ?: Gender.Male.jsonValue).toStrRequestBody(),
                        // Updated: Use localImagePath directly instead of URI
                        image = if (entity.localImagePath.isNullOrEmpty()) null else createProperImagePart(
                            entity.localImagePath!!
                        )
                    )

                    if (response.success && response.data != null) {
                        val odt =
                            OffsetDateTime.parse(if (response.data.createdAt!!.endsWith('Z')) response.data.createdAt else response.data.createdAt + "Z")
                        val odtUpdated =
                            OffsetDateTime.parse(if (response.data.updatedAt!!.endsWith('Z')) response.data.updatedAt else response.data.updatedAt + "Z")
                        val createdAt: Instant = odt.toInstant()
                        val updatedAt: Instant = odtUpdated.toInstant()

                        val syncedEntity = entity.copy(
                            id = response.data.id,
                            isSync = true,
                            needsSync = false,
                            imageUrl = response.data.imageUrl,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )

                        dao.delete(entity)
                        dao.insert(syncedEntity)
                        syncedCount++
                        Log.d("Repository", "Successfully synced new item: ${syncedEntity.id}")

                        // Clean up local image file after successful sync
                        entity.localImagePath?.let { path ->
                            try {
                                File(path).delete()
                                Log.d("Repository", "Cleaned up local image file: $path")
                            } catch (e: Exception) {
                                Log.w("Repository", "Failed to clean up local image file: $path", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        "Repository",
                        "(CREATE) Sync failed for item ${entity.id}: ${e.message}",
                        e
                    )
                    // Continue with next item instead of crashing
                }
            }

            // Sync updates (UPDATE)
            val unsyncedUpdates = dao.getUnsyncedUpdates()
            Log.d("Repository", "Syncing ${unsyncedUpdates.size} unsynced updates")

            for (entity in unsyncedUpdates) {
                try {
                    val response = apiService.updateWaterResult(
                        resultId = entity.id,
                        title = (entity.title ?: "").toStrRequestBody(),
                        description = entity.description?.toStrRequestBody(),
                        roomTemp = (entity.roomTemp ?: 0.0).toNumRequestBody(),
                        tempUnit = (entity.tempUnit?.jsonValue
                            ?: TempUnit.Celsius.jsonValue).toStrRequestBody(),
                        weight = (entity.weight ?: 0.0).toNumRequestBody(),
                        weightUnit = (entity.weightUnit?.jsonValue
                            ?: WeightUnit.Kilogram.jsonValue).toStrRequestBody(),
                        activityLevel = (entity.activityLevel?.jsonValue
                            ?: ActivityLevel.Low.jsonValue).toStrRequestBody(),
                        drinkAmount = (entity.drinkAmount ?: 0.0).toNumRequestBody(),
                        waterUnit = (entity.waterUnit?.jsonValue
                            ?: WaterUnit.Ml.jsonValue).toStrRequestBody(),
                        resultValue = (entity.resultValue ?: 0.0).toNumRequestBody(),
                        percentage = (entity.percentage ?: 0.0).toNumRequestBody(),
                        gender = (entity.gender?.jsonValue
                            ?: Gender.Male.jsonValue).toStrRequestBody(),
                        deleteImage = entity.deleteImage.toString()
                            .toStrRequestBody(), // Add this line
                        image = if (entity.localImagePath.isNullOrEmpty()) null else createProperImagePart(
                            entity.localImagePath!!
                        )
                    )

                    if (response.success && response.data != null) {
                        val odtUpdated =
                            OffsetDateTime.parse(if (response.data.updatedAt!!.endsWith('Z')) response.data.updatedAt else response.data.updatedAt + "Z")
                        val updatedAt: Instant = odtUpdated.toInstant()

                        val syncedEntity = entity.copy(
                            isSync = true,
                            needsUpdate = false,
                            localImagePath = null,
                            deleteImage = false, // Reset deleteImage flag after successful sync
                            imageUrl = response.data.imageUrl,
                            updatedAt = updatedAt
                        )
                        dao.update(syncedEntity)
                        syncedCount++
                        Log.d("Repository", "Successfully synced update for item: ${entity.id}")

                        // Clean up local image file after successful sync if it was updated
                        entity.localImagePath?.let { path ->
                            // Only delete if it's different from the original image
                            if (!path.contains(entity.id)) {
                                try {
                                    File(path).delete()
                                    Log.d(
                                        "Repository",
                                        "Cleaned up updated local image file: $path"
                                    )
                                } catch (e: Exception) {
                                    Log.w(
                                        "Repository",
                                        "Failed to clean up updated local image file: $path",
                                        e
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        "Repository",
                        "(UPDATE) Sync failed for item ${entity.id}: ${e.message}",
                        e
                    )
                    // Continue with next item instead of crashing
                }
            }

            // Sync deletions (DELETE)
            val unsyncedDeletes = dao.getUnsyncedDeletes()
            Log.d("Repository", "Syncing ${unsyncedDeletes.size} unsynced deletions")

            for (entity in unsyncedDeletes) {
                try {
                    val response = apiService.deleteWaterResult(entity.id)

                    if (response.success) {
                        // Clean up local image file before deleting entity
                        entity.localImagePath?.let { path ->
                            try {
                                File(path).delete()
                                Log.d("Repository", "Cleaned up deleted image file: $path")
                            } catch (e: Exception) {
                                Log.w(
                                    "Repository",
                                    "Failed to clean up deleted image file: $path",
                                    e
                                )
                            }
                        }

                        dao.delete(entity)
                        syncedCount++
                        Log.d("Repository", "Successfully synced deletion for item: ${entity.id}")
                    }
                } catch (e: Exception) {
                    Log.e(
                        "Repository",
                        "(DELETE) Sync failed for item ${entity.id}: ${e.message}",
                        e
                    )
                    // Continue with next item instead of crashing
                }
            }

            lastSyncCount = syncedCount
            Log.d("Repository", "Sync completed. Total synced items: $syncedCount")

        } catch (e: Exception) {
            Log.e("Repository", "Sync process failed: ${e.message}", e)
            // Don't rethrow - just log and return what we managed to sync
        }

        return syncedCount
    }

    // Helper function to create RequestBody from String
    private fun String.toStrRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // Helper function to create RequestBody from Number
    private fun Number.toNumRequestBody(): RequestBody {
        return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // Proper image creation function
    private fun createProperImagePart(
        localImagePath: String
    ): MultipartBody.Part? {
        return try {
            val file = File(localImagePath)

            // Verify file exists and has content
            if (!file.exists() || file.length() == 0L) {
                Log.e("Repository", "Image file doesn't exist or is empty: $localImagePath")
                return null
            }

            Log.d(
                "Repository",
                "Using existing file: ${file.absolutePath}, size: ${file.length()} bytes"
            )

            // Create RequestBody with proper media type
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

            // Create MultipartBody.Part with proper form field name
            MultipartBody.Part.createFormData("image", file.name, requestFile)

        } catch (e: Exception) {
            Log.e("Repository", "Error creating image part from file: $localImagePath", e)
            null
        }
    }
}