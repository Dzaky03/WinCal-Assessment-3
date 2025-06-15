package com.dzaky3022.asesment1.ui.model

data class BaseResponse<T>(
    val code: Int,
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val meta: Meta? = null,
)
