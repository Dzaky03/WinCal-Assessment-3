package com.dzaky3022.asesment1.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dzaky3022.asesment1.repository.WaterResultRepository
import com.dzaky3022.asesment1.ui.component.waterdrops.wave.WaterDropText
import com.dzaky3022.asesment1.ui.component.waterdrops.wave.WaveParams
import com.dzaky3022.asesment1.ui.model.User
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import com.dzaky3022.asesment1.ui.screen.form.FormScreen
import com.dzaky3022.asesment1.ui.screen.list.ListScreen
import com.dzaky3022.asesment1.ui.screen.list.ListViewModel
import com.dzaky3022.asesment1.ui.screen.visual.VisualScreen
import com.dzaky3022.asesment1.ui.theme.Poppins
import com.dzaky3022.asesment1.ui.theme.Water
import com.dzaky3022.asesment1.utils.ViewModelFactory
import com.dzaky3022.asesment1.waveGap

@Composable
fun NavGraph(
    navController: NavHostController,
    listViewModel: ListViewModel,
    repository: WaterResultRepository,
    localUser: User?,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val points = remember { screenWidth / waveGap }
    var selectedData by remember { mutableStateOf(WaterResultEntity()) }

    NavHost(
        navController = navController,
        startDestination = Screen.Form.withParams(null, false),
    ) {
        composable(Screen.Form.route,
            arguments = listOf(
                navArgument(KEY_DATA_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(KEY_USE_FAB) {
                    type = NavType.BoolType
                },
            ),
            enterTransition = {
                scaleIn(initialScale = 0.8f) + fadeIn()
            },
            exitTransition = {
                scaleOut(targetScale = 1.2f) + fadeOut()
            }
        ) {
            val dataId = it.arguments?.getString(KEY_DATA_ID) ?: ""
            val useFab = it.arguments?.getBoolean(KEY_USE_FAB) ?: false

            FormScreen(
                navController = navController,
                formViewModel = viewModel(
                    factory = ViewModelFactory(
                        localUser = localUser,
                        waterResultId = dataId,
                        repository = repository,
                        useFab = useFab,
                    )
                ),
                onNavigate = { data ->
                    selectedData = data
                }
            )
        }
        composable(Screen.Visual.route,
            enterTransition = {
                scaleIn(initialScale = 0.8f) + fadeIn()
            },
            exitTransition = {
                scaleOut(targetScale = 1.2f) + fadeOut()
            }
        ) {
            VisualScreen(
                modifier = Modifier,
                waveDurationInMills = 3000L,
                navController = navController,
                visualViewModel = viewModel(
                    factory = ViewModelFactory(
                        waterResultEntity = selectedData
                    )
                ),
            ) {
                WaterDropText(
                    modifier = Modifier,
                    align = Alignment.Center,
                    textStyle = TextStyle(
                        color = Water,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Poppins,
                    ),
                    waveParams = WaveParams(
                        pointsQuantity = points,
                        maxWaveHeight = 30f
                    )
                )
            }
        }
        composable(Screen.List.route,
            enterTransition = {
                scaleIn(initialScale = 0.8f) + fadeIn()
            },
            exitTransition = {
                scaleOut(targetScale = 1.2f) + fadeOut()
            }
        ) {
            ListScreen(
                navController = navController,
                listViewModel = listViewModel,
            )
        }
    }
}
