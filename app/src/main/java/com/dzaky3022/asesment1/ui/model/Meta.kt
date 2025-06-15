package com.dzaky3022.asesment1.ui.model

import com.squareup.moshi.Json

data class Meta (
    @Json(name = "current_page")
    val currentPage: Int,
    val from: Int,
    @Json(name = "last_page")
    val lastPage: Int,
    @Json(name = "per_page")
    val perPage: Int,
    val to: Int,
    val total: Int,
)