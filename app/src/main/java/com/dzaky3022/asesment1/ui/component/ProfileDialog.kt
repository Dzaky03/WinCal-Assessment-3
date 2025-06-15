package com.dzaky3022.asesment1.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dzaky3022.asesment1.R
import com.dzaky3022.asesment1.ui.model.User
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.BackgroundLight
import com.dzaky3022.asesment1.ui.theme.Danger

@Composable
fun ProfileDialog(
    user: User,
    onDismissRequest: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = cardColors(containerColor = BackgroundLight, contentColor = BackgroundDark)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.baseline_account_circle_24),
                    modifier = Modifier.size(100.dp).clip(shape = CircleShape)
                )
                Text(
                    text = user.nama ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = user.email ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, BackgroundDark)
                    ) {
                        Text(
                            text = stringResource(R.string.close),
                            color = BackgroundDark,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Danger)
                    ) {
                        Text(
                            text = stringResource(R.string.logout),
                            color = Danger,
                            fontSize = 14.sp
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDeleteAccount,
                    colors = buttonColors(containerColor = Danger)
                ) {
                    Text(text = stringResource(R.string.delete_account), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
