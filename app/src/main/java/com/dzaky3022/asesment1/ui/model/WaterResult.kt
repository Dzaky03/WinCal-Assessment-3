package com.dzaky3022.asesment1.ui.model

import com.dzaky3022.asesment1.utils.Enums

interface WaterResult {
    val uid: String?
    val title: String?
    val description: String?
    val roomTemp: Double?
    val tempUnit: Enums.TempUnit?
    val weight: Double?
    val weightUnit: Enums.WeightUnit?
    val activityLevel: Enums.ActivityLevel?
    val drinkAmount: Double?
    val waterUnit: Enums.WaterUnit?
    val resultValue: Double?
    val percentage: Double?
    val gender: Enums.Gender?
}