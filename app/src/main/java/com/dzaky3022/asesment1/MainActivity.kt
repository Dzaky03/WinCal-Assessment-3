package com.dzaky3022.asesment1

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.dzaky3022.asesment1.database.AppDatabase
import com.dzaky3022.asesment1.navigation.NavGraph
import com.dzaky3022.asesment1.network.WaterApi
import com.dzaky3022.asesment1.repository.WaterResultRepository
import com.dzaky3022.asesment1.ui.component.SyncStatusSnackbar
import com.dzaky3022.asesment1.ui.component.SyncStatusSnackbarHost
import com.dzaky3022.asesment1.ui.model.User
import com.dzaky3022.asesment1.ui.screen.dashboard.DashboardScreen
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.Water
import com.dzaky3022.asesment1.ui.theme.WhiteTitle
import com.dzaky3022.asesment1.ui.theme.WinCalTheme
import com.dzaky3022.asesment1.utils.DataStore
import com.dzaky3022.asesment1.utils.ViewModelFactory
import com.firebase.ui.auth.AuthUI
import kotlinx.coroutines.delay

const val waveGap = 30

class MainActivity : ComponentActivity() {

    private var syncManager: SyncManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val dataStore = DataStore(this.applicationContext)
        val roomDb = AppDatabase.getAppDb(this.applicationContext)
        val firebaseAuthUi = AuthUI.getInstance()

        setContent {
            WinCalTheme {
                val viewModel: AppViewModel = viewModel()
                val userFlow by viewModel.userFlow.collectAsState()
                var isSplashOver by remember { mutableStateOf(false) }

                // Snackbar host state for sync status
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    delay(500)
                    isSplashOver = true
                }

                if (!isSplashOver) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BackgroundDark),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            modifier = Modifier
                                .size(250.dp)
                                .offset(y = (-30).dp)
                                .align(Alignment.CenterHorizontally),
                            painter = painterResource(id = R.drawable.app_logo_2),
                            contentDescription = stringResource(R.string.app_logo),
                            colorFilter = ColorFilter.tint(
                                color = Water,
                                blendMode = BlendMode.SrcIn
                            )
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.initializing),
                            color = WhiteTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(24.dp))
                        CircularProgressIndicator(color = Water)
                    }
                } else {
                    // Wrap everything in Scaffold to show snackbar
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = BackgroundDark,
                        snackbarHost = {
                            SyncStatusSnackbarHost(
                                hostState = snackbarHostState
                            )
                        }
                    ) { paddingValues ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues.calculateTopPadding() - paddingValues.calculateTopPadding()),
                            color = BackgroundDark
                        ) {
                            val navController = rememberNavController()
                            if (userFlow == null) {
                                WaterApi.clear()
                                // Stop sync manager when no user
                                syncManager?.stopPeriodicSync()
                                syncManager = null

                                DashboardScreen(
                                    dashboardViewModel = viewModel(factory = ViewModelFactory(authUI = firebaseAuthUi)),
                                    navController = navController,
                                    authUI = firebaseAuthUi,
                                )
                            }

                            userFlow?.let { user ->
                                // Use user.uid as key to force recomposition when user changes
                                androidx.compose.runtime.key(user.uid) {

                                    val firebaseAuthUiNew = AuthUI.getInstance()

                                    val localUser = User(
                                        id = user.uid,
                                        nama = if (user.displayName.isNullOrEmpty()) "Guest" else user.displayName,
                                        email = user.email ?: "",
                                        photoUrl = user.photoUrl.toString(),
                                    )

                                    // Initialize API and repository together
                                    val repository = remember(user.uid) {
                                        Log.d("MainActivity", "User changed - uid: ${user.uid}, display name: ${user.displayName}")

                                        // Clear and reinitialize API for new user
                                        WaterApi.clear()
                                        WaterApi.initialize(user.uid)

                                        // Create repository after API is initialized
                                        WaterResultRepository(
                                            context = this@MainActivity,
                                            uid = user.uid,
                                            dao = roomDb.waterResultDao(),
                                            apiService = WaterApi.service,
                                        )
                                    }

                                    // Initialize SyncManager for the current user
                                    LaunchedEffect(user.uid) {
                                        // Stop previous sync manager if exists
                                        syncManager?.stopPeriodicSync()

                                        // Create new sync manager for current user
                                        syncManager = SyncManager(
                                            context = this@MainActivity,
//                                            repository = repository
                                        )

                                        // Trigger initial sync
                                        syncManager?.triggerImmediateSync()

                                        Log.d("MainActivity", "SyncManager initialized for user: ${user.uid}")
                                    }

                                    // Add sync status snackbar monitoring
                                    SyncStatusSnackbar(
                                        syncManager = syncManager,
                                        snackbarHostState = snackbarHostState
                                    )

                                    val factory = remember(user.uid) {
                                        ViewModelFactory(
                                            localUser = localUser,
                                            dataStore = dataStore,
                                            authUI = firebaseAuthUiNew,
                                            repository = repository,
                                        )
                                    }

                                    NavGraph(
                                        navController = navController,
                                        listViewModel = viewModel(factory = factory, key = user.uid),
                                        repository = repository,
                                        localUser = localUser,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up sync manager
        syncManager?.stopPeriodicSync()
        syncManager = null
    }
}