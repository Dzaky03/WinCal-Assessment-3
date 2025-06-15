package com.dzaky3022.asesment1

import android.app.Application
import android.util.Log
import com.dzaky3022.asesment1.utils.NetworkUtils
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen

class MyApp : Application() {

    companion object {
        private const val TAG = "WinCalApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application starting...")

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize NetworkUtils to start monitoring network changes
        NetworkUtils.initialize(this)
        AndroidThreeTen.init(this)

        Log.d(TAG, "Application initialized")
    }

    override fun onTerminate() {
        super.onTerminate()
        // Clean up NetworkUtils when app terminates
        NetworkUtils.cleanup()
    }
}