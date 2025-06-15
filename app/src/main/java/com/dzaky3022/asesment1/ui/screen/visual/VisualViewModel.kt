package com.dzaky3022.asesment1.ui.screen.visual

import androidx.lifecycle.ViewModel
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VisualViewModel(
    selectedWaterResult: WaterResultEntity,
) : ViewModel() {

    private val _waterResult = MutableStateFlow<WaterResultEntity?>(null)
    val waterResult: StateFlow<WaterResultEntity?> = _waterResult

    init {
        _waterResult.value = selectedWaterResult
    }
}