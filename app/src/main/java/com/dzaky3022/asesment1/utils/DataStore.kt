package com.dzaky3022.asesment1.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "data_prefs")

class DataStore(private val context: Context) {
    private val isList = booleanPreferencesKey("is_list")

    val layoutFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[isList] ?: true
    }

    suspend fun saveLayout(orientationView: Enums.OrientationView) {
        context.dataStore.edit { preferences ->
            preferences[isList] = orientationView == Enums.OrientationView.List
        }
    }
}