package com.dzaky3022.asesment1.ui.screen.list

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dzaky3022.asesment1.R
import com.dzaky3022.asesment1.navigation.Screen
import com.dzaky3022.asesment1.ui.component.EmptyState
import com.dzaky3022.asesment1.ui.component.ProfileDialog
import com.dzaky3022.asesment1.ui.component.PullToRefreshContainer
import com.dzaky3022.asesment1.ui.component.WarningDialog
import com.dzaky3022.asesment1.ui.component.WaterResultDetailDialog
import com.dzaky3022.asesment1.ui.component.WaterResultItem
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.BackgroundLight
import com.dzaky3022.asesment1.ui.theme.Water
import com.dzaky3022.asesment1.utils.Enums
import com.firebase.ui.auth.AuthUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navController: NavHostController,
    listViewModel: ListViewModel,
) {
    val context = LocalContext.current
    val logoutStatus by listViewModel.logOutStatus.collectAsState()
    val listData by listViewModel.listData.collectAsState()
    val loadStatus by listViewModel.loadStatus.collectAsState()
    val deleteStatus by listViewModel.deleteStatus.collectAsState()
    val deleteAccountStatus by listViewModel.deleteAccountStatus.collectAsState()
    val userData by listViewModel.userData.collectAsState()
    val orientationView by listViewModel.orientationView.collectAsState()
    var showProfileDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false to "") }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showAlertExit by rememberSaveable { mutableStateOf(false) }

    // New state for showing detail dialog
    var showDetailDialog by rememberSaveable { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<WaterResultEntity?>(null) }

    if (showProfileDialog)
        userData?.let {
            ProfileDialog(
                user = it,
                onDismissRequest = { showProfileDialog = false },
                onLogout = {
                    showProfileDialog = false
                    listViewModel.logout(context)
                    navController.popBackStack(
                        route = navController.graph.startDestinationRoute ?: "",
                        inclusive = false
                    )
                }
            ) {
                showProfileDialog = false
                showDeleteAccountDialog = true
            }
        }

    if (showDeleteDialog.first)
        WarningDialog(
            onDismissRequest = {
                showDeleteDialog = false to showDeleteDialog.second
            }) {
            listViewModel.deleteData(showDeleteDialog.second, context)
            showDeleteDialog = false to ""
        }

    if (showDeleteAccountDialog)
        WarningDialog(
            label = stringResource(R.string.delete_your_account),
            onDismissRequest = {
                showDeleteAccountDialog = false
            }) {
            showDeleteAccountDialog = false
            listViewModel.deleteAccount(context)
        }

    // Detail dialog
    if (showDetailDialog && selectedItem != null) {
        WaterResultDetailDialog(
            item = selectedItem!!,
            onDismissRequest = {
                showDetailDialog = false
                selectedItem = null
            },
            onEdit = { id ->
                showDetailDialog = false
                selectedItem = null
                navController.navigate(Screen.Form.withParams(id))
            },
            onDelete = { id ->
                showDetailDialog = false
                selectedItem = null
                showDeleteDialog = true to id
            }
        )
    }

    BackHandler {
        showAlertExit = !showAlertExit
    }

    if (showAlertExit) {
        WarningDialog(
            label = "Quit App?", confirmationLabel = "Yes",
            onDismissRequest = { showAlertExit = false }
        ) {
            showAlertExit = false
            val activity = (context as? Activity)
            activity?.finish()
        }
    }

    LaunchedEffect(deleteStatus) {
        if (deleteStatus != Enums.ResponseStatus.Idle) {
            Toast.makeText(context, deleteStatus.message, Toast.LENGTH_SHORT).show()
            listViewModel.reset()
        }
    }

    LaunchedEffect(logoutStatus) {
        if (logoutStatus != Enums.ResponseStatus.Idle)
            Toast.makeText(context, logoutStatus.message, Toast.LENGTH_SHORT).show()

        if (logoutStatus == Enums.ResponseStatus.Success) {
            navController.popBackStack(
                route = navController.graph.startDestinationRoute ?: "",
                inclusive = false
            )
            listViewModel.reset()
        }
    }

    LaunchedEffect(deleteAccountStatus) {
        if (deleteAccountStatus != Enums.ResponseStatus.Idle)
            Toast.makeText(context, deleteAccountStatus.message, Toast.LENGTH_SHORT).show()

        if (deleteAccountStatus == Enums.ResponseStatus.Success) {
            navController.popBackStack(
                route = navController.graph.startDestinationRoute ?: "",
                inclusive = false
            )
            listViewModel.reset()
        }
    }

    LaunchedEffect(Unit) {
        listViewModel.onRefresh()
    }

    PullToRefreshContainer(
        onRefreshParams = {
            listViewModel.onRefresh()
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.list_calculation_results),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showProfileDialog = !showProfileDialog }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = stringResource(R.string.profile_icon),
                                tint = BackgroundDark,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Water),
                )
            },
            floatingActionButton = {
                FloatingActionButton(containerColor = BackgroundLight, onClick = {
                    navController.navigate(Screen.Form.withParams(useFab = true))
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_data_button),
                        tint = BackgroundDark
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        listViewModel.changeOrientationView(
                            if (orientationView == Enums.OrientationView.List) Enums.OrientationView.Grid else Enums.OrientationView.List
                        )
                    }) {
                        Icon(
                            imageVector = if (orientationView == Enums.OrientationView.List) Icons.Default.GridView else Icons.AutoMirrored.Filled.List,
                            contentDescription = "Toggle View"
                        )
                    }
                }
                if (listData.isNullOrEmpty() || loadStatus == Enums.ResponseStatus.Loading) {
                    EmptyState(
                        isLoading = loadStatus == Enums.ResponseStatus.Loading
                    )
                } else {
                    when (orientationView) {
                        Enums.OrientationView.List -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(listData!!) { item ->
                                    WaterResultItem(
                                        item = item,
                                        onEditOrRestore = { id ->
                                            navController.navigate(Screen.Form.withParams(id))
                                        },
                                        onDelete = { id ->
                                            showDeleteDialog = true to id
                                        }
                                    ) {
                                        // Show detail dialog instead of navigating
                                        selectedItem = item
                                        showDetailDialog = true
                                    }
                                }
                            }
                        }

                        Enums.OrientationView.Grid -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(listData!!) { item ->
                                    WaterResultItem(
                                        item = item,
                                        onEditOrRestore = { id ->
                                            navController.navigate(Screen.Form.withParams(id))
                                        },
                                        onDelete = { id ->
                                            showDeleteDialog = true to id
                                        }
                                    ) {
                                        selectedItem = item
                                        showDetailDialog = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
