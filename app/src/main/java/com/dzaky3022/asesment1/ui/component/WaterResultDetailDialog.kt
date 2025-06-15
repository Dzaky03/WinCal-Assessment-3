package com.dzaky3022.asesment1.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.BackgroundLight
import com.dzaky3022.asesment1.ui.theme.Danger
import com.dzaky3022.asesment1.ui.theme.Success
import com.dzaky3022.asesment1.utils.roundUpTwoDecimals
import com.dzaky3022.asesment1.utils.toFormattedDate

@Composable
fun WaterResultDetailDialog(
    item: WaterResultEntity,
    onDismissRequest: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = BackgroundLight,
                contentColor = BackgroundDark
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Water Calculation Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BackgroundDark
                        )
                    }
                }

                // Image section
                if (!item.imageUrl.isNullOrEmpty() || !item.localImagePath.isNullOrEmpty()) {
                    val imageSource = when {
                        !item.localImagePath.isNullOrEmpty() -> "file://${item.localImagePath}"
                        !item.imageUrl.isNullOrEmpty() -> item.imageUrl
                        else -> null
                    }
                    
                    imageSource?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Water result image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Title section
                DetailSection(
                    title = "Title",
                    content = item.title ?: "Untitled"
                )

                // Description section
                if (!item.description.isNullOrEmpty()) {
                    DetailSection(
                        title = "Description",
                        content = item.description!!
                    )
                }

                // Calculation Details
                Text(
                    text = "Calculation Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BackgroundDark
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BackgroundDark.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow("Room Temperature", "${item.roomTemp ?: 0} ${item.tempUnit ?: "°C"}")
                        DetailRow("Weight", "${item.weight ?: 0} ${item.weightUnit ?: "kg"}")
                        DetailRow("Activity Level", "${item.activityLevel ?: "Not specified"}")
                        DetailRow("Drink Amount", "${item.drinkAmount ?: 0} ${item.waterUnit ?: "ml"}")
                        DetailRow("Gender", "${item.gender ?: "Not specified"}")
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = BackgroundDark.copy(alpha = 0.1f)
                        )
                        
                        DetailRow(
                            "Result Value", 
                            "${item.resultValue?.roundUpTwoDecimals() ?: 0} ml",
                            isResult = true
                        )
                        DetailRow(
                            "Percentage", 
                            "${item.percentage?.roundUpTwoDecimals() ?: 0}%",
                            isResult = true
                        )
                    }
                }

                // Timestamps
                if (item.createdAt != null || item.updatedAt != null) {
                    Text(
                        text = "Timestamps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BackgroundDark
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = BackgroundDark.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.createdAt?.let {
                                DetailRow("Created At", it.toFormattedDate(java.util.Locale.getDefault()))
                            }
                            item.updatedAt?.let {
                                DetailRow("Updated At", it.toFormattedDate(java.util.Locale.getDefault()))
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDelete(item.id) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Danger),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Danger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delete",
                            fontSize = 14.sp,
                            color = Danger
                        )
                    }

                    Button(
                        onClick = { onEdit(item.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Success
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = BackgroundDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 16.sp,
            color = BackgroundDark
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isResult: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = BackgroundDark.copy(alpha = 0.8f),
            fontWeight = if (isResult) FontWeight.Medium else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = if (isResult) Success else BackgroundDark,
            fontWeight = if (isResult) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}