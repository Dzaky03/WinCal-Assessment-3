package com.dzaky3022.asesment1.ui.screen.dashboard

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.AuthUI


class DashboardViewModel(
    private val authUI: AuthUI,
) : ViewModel() {

    fun signIn(): Intent {
        return authUI.createSignInIntentBuilder()
            .setIsSmartLockEnabled(false) // Disable Smart Lock
            .setAvailableProviders(
                arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
            )
            .build()
    }
}