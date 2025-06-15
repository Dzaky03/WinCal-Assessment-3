package com.dzaky3022.asesment1.ui.screen.list

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzaky3022.asesment1.R
import com.dzaky3022.asesment1.repository.WaterResultRepository
import com.dzaky3022.asesment1.ui.model.User
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import com.dzaky3022.asesment1.utils.DataStore
import com.dzaky3022.asesment1.utils.Enums
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListViewModel(
    private val localUser: User,
    private val authUI: AuthUI,
    private val repository: WaterResultRepository,
    private val dataStore: DataStore,
) : ViewModel() {

    private val _listData = MutableStateFlow<List<WaterResultEntity>?>(null)
    val listData: StateFlow<List<WaterResultEntity>?> = _listData

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _loadStatus = MutableStateFlow(Enums.ResponseStatus.Idle)
    val loadStatus: StateFlow<Enums.ResponseStatus> = _loadStatus

    private val _deleteStatus = MutableStateFlow(Enums.ResponseStatus.Idle)
    val deleteStatus: StateFlow<Enums.ResponseStatus> = _deleteStatus

    private val _logOutStatus = MutableStateFlow(Enums.ResponseStatus.Idle)
    val logOutStatus: StateFlow<Enums.ResponseStatus> = _logOutStatus

    private val _deleteAccountStatus = MutableStateFlow(Enums.ResponseStatus.Idle)
    val deleteAccountStatus: StateFlow<Enums.ResponseStatus> = _deleteAccountStatus

    private val _orientationView = MutableStateFlow(Enums.OrientationView.List)
    val orientationView: StateFlow<Enums.OrientationView> = _orientationView

    init {
        _userData.value = localUser
        getList()
        getOrientationView()
    }

    private fun getList() {
//        _userData.value.let {
        _loadStatus.value = Enums.ResponseStatus.Loading
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllWaterResults().collect {
                _listData.value = it
                _loadStatus.value = Enums.ResponseStatus.Success
            }
//                repository.getAllWaterResults().collect { waterResults ->
//                    _listData.value = waterResults
//                    _loadStatus.value = Enums.ResponseStatus.Idle
//                    Log.d("ListVM", "list: $waterResults")
//                }
        }
//        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            repository.refreshFromNetwork()
            getList()
        }
    }

    fun deleteData(waterResultId: String, context: Context) {
        _deleteStatus.value = Enums.ResponseStatus.Loading.apply { updateMessage(context.getString(R.string.waiting)) }
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.deleteWaterResult(waterResultId)
            if (response)
                _deleteStatus.value =
                    Enums.ResponseStatus.Success.apply { updateMessage(context.getString(R.string.delete_data_success)) }
            else
                _deleteStatus.value =
                    Enums.ResponseStatus.Failed.apply { updateMessage(context.getString(R.string.delete_data_failed)) }
            getList()
        }
    }

    fun logout(context: Context) {
        _logOutStatus.value =
            Enums.ResponseStatus.Loading.apply { updateMessage(context.getString(R.string.waiting)) }
        viewModelScope.launch {
            authUI.signOut(context)
            dataStore.clearUser()
//            if (dataStore.firstOrNull().isNullOrEmpty())
//                _logOutStatus.value = Enums.ResponseStatus.Success.apply {
//                    updateMessage(
//                        context.getString(
//                            R.string.logout_success
//                        )
//                    )
//                }
//            else
//                _logOutStatus.value = Enums.ResponseStatus.Failed.apply {
//                    updateMessage(
//                        context.getString(
//                            R.string.logout_failed
//                        )
//                    )
//                }

        }
    }

    fun deleteAccount(context: Context) {
        _deleteAccountStatus.value =
            Enums.ResponseStatus.Loading.apply { updateMessage(context.getString(R.string.waiting)) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.clearUserData()

                // Delete Firebase account
                authUI.delete(context).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _deleteAccountStatus.value = Enums.ResponseStatus.Success.apply {
                            updateMessage(
                                context.getString(R.string.delete_account_success),
                            )
                        }
                        Log.d("Auth", "User account deleted and data cleared.")
                    } else {
                        _deleteAccountStatus.value = Enums.ResponseStatus.Failed.apply {
                            updateMessage(
                                context.getString(R.string.delete_account_failed),
                            )
                        }
                        Log.e("Auth", "Failed to delete user", task.exception)
                    }
                }
            } catch (e: Exception) {
                _deleteAccountStatus.value = Enums.ResponseStatus.Failed.apply {
                    updateMessage(
                        context.getString(R.string.delete_account_failed),
                    )
                }
                Log.e("Auth", "Failed to clear user data", e)
            }
        }
    }

    private fun getOrientationView() {
        viewModelScope.launch {
            dataStore.layoutFlow.collect {
                _orientationView.value =
                    if (it) Enums.OrientationView.List else Enums.OrientationView.Grid
            }
        }
    }

    fun changeOrientationView(orientationView: Enums.OrientationView) {
        viewModelScope.launch {
            dataStore.saveLayout(orientationView)
            getOrientationView()
        }
    }

    fun reset() {
        _logOutStatus.value = Enums.ResponseStatus.Idle
        _deleteAccountStatus.value = Enums.ResponseStatus.Idle
        _deleteStatus.value = Enums.ResponseStatus.Idle
    }

    override fun onCleared() {
        super.onCleared()
        _logOutStatus.value = Enums.ResponseStatus.Idle
        _deleteAccountStatus.value = Enums.ResponseStatus.Idle
        _deleteStatus.value = Enums.ResponseStatus.Idle
        _loadStatus.value = Enums.ResponseStatus.Idle
    }
}