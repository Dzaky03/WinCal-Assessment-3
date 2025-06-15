package com.dzaky3022.asesment1.ui.screen.dashboard

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dzaky3022.asesment1.R
import com.dzaky3022.asesment1.navigation.Screen
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.IconBackgroundGray
import com.dzaky3022.asesment1.ui.theme.Water
import com.dzaky3022.asesment1.ui.theme.WhiteCaption
import com.dzaky3022.asesment1.ui.theme.WhiteTitle
import com.dzaky3022.asesment1.utils.Enums.ResponseStatus
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    authUI: AuthUI,
) {
    val context = LocalContext.current
    var authStatus by remember { mutableStateOf(ResponseStatus.Idle) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isPressed2 by remember { mutableStateOf(false) }

    val contract = FirebaseAuthUIActivityResultContract()
    val launcher = rememberLauncherForActivityResult(contract) {
        authStatus = if (it.resultCode == Activity.RESULT_OK) {
            ResponseStatus.Success.apply {
                updateMessage(
                    context.getString(
                        R.string.login_success
                    )
                )
            }
        } else {
            ResponseStatus.Failed.apply {
                updateMessage(
                    context.getString(R.string.login_failed)
                )
            }
        }

    }

    LaunchedEffect(authStatus) {
        if (authStatus != ResponseStatus.Idle) {
            Toast.makeText(context, authStatus.message, Toast.LENGTH_SHORT).show()
            if (authStatus == ResponseStatus.Success) {
                navController.navigate(Screen.Form.withParams())
            }
            authStatus = ResponseStatus.Idle
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = BackgroundDark,
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name_complete),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteTitle
                )
                Text(
                    text = stringResource(R.string.app_desc),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WhiteCaption
                )
            }
            Image(
                modifier = Modifier
                    .size(250.dp)
                    .offset(y = (-30).dp)
                    .align(Alignment.CenterHorizontally)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed2 = true
                                awaitRelease()
                                isPressed2 = false
                            }
                        )
                    },
                painter = painterResource(id = R.drawable.app_logo_2),
                contentDescription = "App Logo",
                colorFilter = ColorFilter.tint(
                    color = if (isPressed2) Color.White else Water,
                    blendMode = BlendMode.SrcIn
                )
            )
            Button (
                    modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                authUI.signOut(context).addOnCompleteListener {
                    launcher.launch(dashboardViewModel.signIn())
                }
            },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isPressed) Color.White else Water,
                disabledContainerColor = IconBackgroundGray
            ),
            interactionSource = interactionSource,
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.login_with_google),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(10.dp))
                Image(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.google__g__logo),
                    contentDescription = stringResource(R.string.google_logo),
                )
            }
        }
        }
    }
}

@Preview
@Composable
private fun DashboardPrev() {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Continue with Google"
        )
        Spacer(Modifier.width(4.dp))
        Image(
            painter = painterResource(R.drawable.google__g__logo),
            contentDescription = stringResource(R.string.google_logo),
        )
    }
}