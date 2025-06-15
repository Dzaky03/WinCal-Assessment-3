package com.dzaky3022.asesment1.ui.screen.form

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzaky3022.asesment1.R
import com.dzaky3022.asesment1.repository.WaterResultRepository
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import com.dzaky3022.asesment1.utils.Enums
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FormViewModel(
    private val waterResultId: String? = null,
    private val repository: WaterResultRepository,
    private val useFab: Boolean? = false,
) : ViewModel() {

    private val _isUpdate = MutableStateFlow(false)
    val isUpdate: StateFlow<Boolean> = _isUpdate

    private val _insertStatus = MutableStateFlow(Enums.ResponseStatus.Idle)
    val insertStatus: StateFlow<Enums.ResponseStatus> = _insertStatus

    private val _updateStatus = MutableStateFlow(Enums.ResponseStatus.Idle)
    val updateStatus: StateFlow<Enums.ResponseStatus> = _updateStatus

    private val _data = MutableStateFlow<WaterResultEntity?>(null)
    val data: StateFlow<WaterResultEntity?> = _data

    private val _isDataExist = MutableStateFlow(false)
    val isDataExist: StateFlow<Boolean> = _isDataExist

    private val _useFAB = MutableStateFlow(false)
    val useFAB: StateFlow<Boolean> = _useFAB

    init {
        checkIfUserHasData()
        if (!waterResultId.isNullOrEmpty()) {
            getData()
            _isUpdate.value = true
        } else {
            _isUpdate.value = false
        }
        _useFAB.value = useFab!!
    }

    fun insert(
        context: Context,
        waterResultEntity: WaterResultEntity,
    ) {
        _insertStatus.value =
            Enums.ResponseStatus.Loading.apply { updateMessage("Insert Data: Loading") }
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.createWaterResult(
                waterResultEntity,
            )
            if (response)
                _insertStatus.value = Enums.ResponseStatus.Success.apply {
                    updateMessage(
                        context.getString(
                            R.string.insert_data_success
                        )
                    )
                    Log.d("DashboardVM", "success inserted data")
                }
            else
                _insertStatus.value = Enums.ResponseStatus.Failed.apply {
                    updateMessage(
                        context.getString(
                            R.string.insert_data_failed
                        )
                    )
                }
        }
    }


    fun updateData(
        context: Context,
        waterResultEntity: WaterResultEntity,
    ) {
        _updateStatus.value =
            Enums.ResponseStatus.Loading.apply { updateMessage("Update Data: Loading") }
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.updateWaterResult(
                waterResultEntity,
            )
            if (response)
                _updateStatus.value = Enums.ResponseStatus.Success.apply {
                    updateMessage(
                        context.getString(
                            R.string.update_data_success
                        )
                    )
                    Log.d("FormVM", "success update data")
                }
            else
                _updateStatus.value = Enums.ResponseStatus.Failed.apply {
                    updateMessage(
                        context.getString(R.string.update_data_failed)
                    )
                    Log.d("FormVM", "failed update data")
                }
        }
    }

    private fun checkIfUserHasData() {
        viewModelScope.launch {
            val response = repository.getAllWaterResults().collect {
                _isDataExist.value = it.isNotEmpty()
            }
            Log.d("FormVM", "function: CheckIFHasData, response: $response")
        }
    }

    private fun getData() {
        viewModelScope.launch(Dispatchers.IO) {
            _data.value = waterResultId?.let { repository.getDataById(it) }
            Log.d("FormVM", "fetched data: ${_data.value}")
        }
    }

    fun reset() {
        _insertStatus.value = Enums.ResponseStatus.Idle
        _updateStatus.value = Enums.ResponseStatus.Idle
    }
}