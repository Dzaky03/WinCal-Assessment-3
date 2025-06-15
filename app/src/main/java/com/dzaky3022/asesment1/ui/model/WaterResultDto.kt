package com.dzaky3022.asesment1.ui.model

import android.net.Uri
import com.dzaky3022.asesment1.utils.Enums
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

@JsonClass(generateAdapter = true)
data class WaterResultDto(
    override val uid: String,
    override val title: String,
    override val description: String? = null,
    override val roomTemp: Double,
    override val tempUnit: Enums.TempUnit,
    override val weight: Double,
    override val weightUnit: Enums.WeightUnit,
    override val activityLevel: Enums.ActivityLevel,
    override val drinkAmount: Double,
    override val waterUnit: Enums.WaterUnit,
    override val resultValue: Double,
    override val percentage: Double,
    override val gender: Enums.Gender,
    val id: String,
    val imageUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : WaterResult

@JsonClass(generateAdapter = true)
data class WaterResultForm(
    override val uid: String? = null,
    override val title: String? = null,
    override val description: String? = null,
    override val roomTemp: Double? = null,
    override val tempUnit: Enums.TempUnit? = null,
    override val weight: Double? = null,
    override val weightUnit: Enums.WeightUnit? = null,
    override val activityLevel: Enums.ActivityLevel? = null,
    override val drinkAmount: Double? = null,
    override val waterUnit: Enums.WaterUnit? = null,
    override val resultValue: Double? = null,
    override val percentage: Double? = null,
    override val gender: Enums.Gender? = null,
    val imageUri: Uri? = null,
) : WaterResult

fun WaterResultDto.toEntity(): WaterResultEntity {
    val odt = OffsetDateTime.parse(if (this.createdAt!!.endsWith('Z')) this.createdAt else this.createdAt + "Z")
    val odtUpdated = OffsetDateTime.parse(if (this.updatedAt!!.endsWith('Z')) this.updatedAt else this.updatedAt + "Z")
    val createdAt: Instant = odt.toInstant()
    val updatedAt: Instant = odtUpdated.toInstant()
    return WaterResultEntity(
        id = this.id,
        uid = this.uid,
        title = this.title,
        description = this.description,
        roomTemp = this.roomTemp,
        tempUnit = this.tempUnit,
        weight = this.weight,
        weightUnit = this.weightUnit,
        drinkAmount = this.drinkAmount,
        activityLevel = this.activityLevel,
        waterUnit = this.waterUnit,
        resultValue = this.resultValue,
        percentage = this.percentage,
        gender = this.gender,
        imageUrl = this.imageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSync = true,
        needsSync = false,
    )
}