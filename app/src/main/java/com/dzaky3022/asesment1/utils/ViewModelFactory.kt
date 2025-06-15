package com.dzaky3022.asesment1.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dzaky3022.asesment1.repository.WaterResultRepository
import com.dzaky3022.asesment1.ui.model.User
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import com.dzaky3022.asesment1.ui.screen.dashboard.DashboardViewModel
import com.dzaky3022.asesment1.ui.screen.form.FormViewModel
import com.dzaky3022.asesment1.ui.screen.list.ListViewModel
import com.dzaky3022.asesment1.ui.screen.visual.VisualViewModel
import com.firebase.ui.auth.AuthUI

class ViewModelFactory(
    private val waterResultId: String? = null,
    private val useFab: Boolean? = null,
    private val localUser: User? = null,
    private val dataStore: DataStore? = null,
    private val waterResultEntity: WaterResultEntity? = null,
    private val repository: WaterResultRepository? = null,
    private val authUI: AuthUI? = null,
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(authUI!!) as T
        } else if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel(
                localUser!!,
                authUI!!,
                repository!!,
                dataStore!!,
            ) as T
        } else if (modelClass.isAssignableFrom(FormViewModel::class.java)) {
            return FormViewModel(
                waterResultId,
                repository!!,
                useFab,
            ) as T
        } else if (modelClass.isAssignableFrom(VisualViewModel::class.java)) {
            return VisualViewModel(
                waterResultEntity!!,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}