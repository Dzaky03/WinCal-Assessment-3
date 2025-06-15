package com.dzaky3022.asesment1.utils

import android.content.Context
import androidx.annotation.StringRes
import com.dzaky3022.asesment1.R
import com.squareup.moshi.Json

class Enums {
    enum class ActivityLevel(
        @StringRes val labelResId: Int,
        val value: Double,
        val jsonValue: String,
    ) {
        @Json(name = "LOW")
        Low(R.string.low, 35.0, "LOW"),

        @Json(name = "MEDIUM")
        Medium(R.string.medium, 40.0, "MEDIUM"),

        @Json(name = "HIGH")
        High(R.string.high, 45.0, "HIGH");

        fun getLabel(context: Context): String = context.getString(labelResId)
    }

    enum class TempUnit(
        val symbol: String, val jsonValue: String,
    ) {
        @Json(name = "CELSIUS")
        Celsius("°C", "CELSIUS"),

        @Json(name = "FAHRENHEIT")
        Fahrenheit("°F", "FAHRENHEIT"),

        @Json(name = "KELVIN")
        Kelvin("K", "KELVIN");
    }

    enum class WeightUnit(
        val symbol: String, val jsonValue: String,
    ) {
        @Json(name = "KILOGRAM")
        Kilogram("kg", "KILOGRAM"),

        @Json(name = "POUND")
        Pound("lbs", "KILOGRAM"),
    }

    enum class Gender(
        @StringRes val labelResId: Int, val jsonValue: String,
    ) {
        @Json(name = "MALE")
        Male(R.string.male, "MALE"),

        @Json(name = "FEMALE")
        Female(R.string.female, "FEMALE");

        fun getLabel(context: Context): String = context.getString(labelResId)
    }

    enum class WaterUnit(
        val jsonValue: String,
    ) {
        @Json(name = "ML")
        Ml("ML"),

        @Json(name = "OZ")
        Oz("OZ"),

        @Json(name = "GLASSES")
        Glasses("GLASSES"),
    }

    enum class Direction {
        Horizontal,
    }

    enum class ScreenState {
        FirstScreen,
        SecondScreen,
        ThirdScreen,
    }

    enum class OrientationView {
        List,
        Grid,
    }

    enum class ResponseStatus(var message: String = "") {
        SuccessOffline,
        Success,
        Failed,
        Idle,
        Loading;

        fun updateMessage(message: String = "") {
            this.message = message
        }
    }
}