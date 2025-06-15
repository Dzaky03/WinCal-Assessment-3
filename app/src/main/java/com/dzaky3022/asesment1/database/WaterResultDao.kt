package com.dzaky3022.asesment1.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterResultDao {
    @Query("SELECT * FROM water_results WHERE uid = :uid AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllByUserSync(uid: String): List<WaterResultEntity>

    @Query("SELECT * FROM water_results WHERE uid = :uid AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllByUser(uid: String): Flow<List<WaterResultEntity>>

    @Query("SELECT * FROM water_results WHERE id = :id")
    suspend fun getById(id: String): WaterResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(waterResult: WaterResultEntity): Long?

    @Update
    suspend fun update(waterResult: WaterResultEntity): Int

    @Delete
    suspend fun delete(waterResult: WaterResultEntity): Int

    @Query("DELETE FROM water_results WHERE uid = :uid")
    suspend fun deleteAllByUser(uid: String): Int

    @Query("SELECT * FROM water_results WHERE needsSync = 1")
    suspend fun getUnsyncedResults(): List<WaterResultEntity>

    @Query("SELECT * FROM water_results WHERE needsUpdate = 1")
    suspend fun getUnsyncedUpdates(): List<WaterResultEntity>

    @Query("SELECT * FROM water_results WHERE needsDelete = 1")
    suspend fun getUnsyncedDeletes(): List<WaterResultEntity>
}