package com.dzaky3022.asesment1.navigation

const val KEY_DATA_ID = "dataId"
const val KEY_USE_FAB = "fab"

sealed class Screen(val route: String) {
    data object Form : Screen("form/{$KEY_DATA_ID}?useFab={${KEY_USE_FAB}}") {
        fun withParams(id: String? = null, useFab: Boolean = false) =
            "form/$id?useFab=$useFab"
    }

    data object Visual : Screen("visual")

    data object List : Screen("list")
}