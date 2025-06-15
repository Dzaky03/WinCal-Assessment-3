package com.dzaky3022.asesment1.ui.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dzaky3022.asesment1.utils.Enums.ActivityLevel
import com.dzaky3022.asesment1.utils.Enums.Gender
import com.dzaky3022.asesment1.utils.Enums.TempUnit
import com.dzaky3022.asesment1.utils.Enums.WaterUnit
import com.dzaky3022.asesment1.utils.Enums.WeightUnit
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "water_results")
data class WaterResultEntity(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    override val uid: String? = null,
    override val title: String? = null,
    override val description: String? = null,
    override val roomTemp: Double? = null,
    override val tempUnit: TempUnit? = null,
    override val weight: Double? = null,
    override val weightUnit: WeightUnit? = null,
    override val activityLevel: ActivityLevel? = null,
    override val drinkAmount: Double? = null,
    override val waterUnit: WaterUnit? = null,
    override val resultValue: Double? = null,
    override val percentage: Double? = null,
    override val gender: Gender? = null,
    var imageUrl: String? = null,
    var localImagePath: String? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    val needsSync: Boolean = false, // For new items that need to be created on server
    val needsUpdate: Boolean = false, // For existing items that need to be updated on server
    val needsDelete: Boolean = false, // For items that need to be deleted on server
    val isSync: Boolean = false,
    val isDeleted: Boolean = false, // Soft delete flag
    val deleteImage: Boolean = false,
) : WaterResult
