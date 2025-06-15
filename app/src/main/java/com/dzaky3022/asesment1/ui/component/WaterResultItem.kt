package com.dzaky3022.asesment1.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.dzaky3022.asesment1.R
import com.dzaky3022.asesment1.ui.model.WaterResultEntity
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.BackgroundLight
import com.dzaky3022.asesment1.ui.theme.Danger
import com.dzaky3022.asesment1.ui.theme.Gray
import com.dzaky3022.asesment1.ui.theme.Success
import com.dzaky3022.asesment1.ui.theme.Water
import com.dzaky3022.asesment1.utils.roundUpTwoDecimals
import com.dzaky3022.asesment1.utils.toFormattedDate

@Composable
fun WaterResultItem(
    item: WaterResultEntity,
    onEditOrRestore: (String) -> Unit,
    onDelete: (String) -> Unit,
    onTap: () -> Unit,
    isGridLayout: Boolean = false
) {
    if (isGridLayout) {
        GridWaterResultItem(
            item = item,
            onTap = onTap
        )
    } else {
        ListWaterResultItem(
            item = item,
            onEditOrRestore = onEditOrRestore,
            onDelete = onDelete,
            onTap = onTap
        )
    }
}

@Composable
private fun GridWaterResultItem(
    item: WaterResultEntity,
    onTap: () -> Unit
) {
    Card(
        onClick = onTap,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = cardColors(containerColor = BackgroundLight)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Image section with fallback
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    val imageSource = when {
                        !item.localImagePath.isNullOrEmpty() -> "file://${item.localImagePath}"
                        !item.imageUrl.isNullOrEmpty() -> item.imageUrl
                        else -> null
                    }

                    if (imageSource != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageSource),
                            contentDescription = "Water result image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay for better text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )
                    } else {
                        // Elegant fallback design
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Water.copy(alpha = 0.1f),
                                            Water.copy(alpha = 0.3f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Water.copy(alpha = 0.2f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = "Water icon",
                                        tint = Water,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Content section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title ?: "Untitled",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )

                    // Result with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${item.resultValue?.roundUpTwoDecimals() ?: 0} ml",
                            fontSize = 12.sp,
                            color = Success,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    item.createdAt?.let {
                        Text(
                            text = it.toFormattedDate(java.util.Locale.getDefault()),
                            fontSize = 10.sp,
                            color = Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListWaterResultItem(
    item: WaterResultEntity,
    onEditOrRestore: (String) -> Unit,
    onDelete: (String) -> Unit,
    onTap: () -> Unit
) {
    Card(
        onClick = onTap,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = cardColors(containerColor = BackgroundLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val imageSource = when {
                    !item.localImagePath.isNullOrEmpty() -> "file://${item.localImagePath}"
                    !item.imageUrl.isNullOrEmpty() -> item.imageUrl
                    else -> null
                }

                if (imageSource != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageSource),
                        contentDescription = "Water result image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Water.copy(alpha = 0.1f),
                                        Water.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Water.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Water icon",
                                    tint = Water,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title ?: "Untitled",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        color = Gray,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Result and date row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${item.resultValue?.roundUpTwoDecimals() ?: 0} ml",
                            fontSize = 12.sp,
                            color = Success,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    item.createdAt?.let {
                        Text(
                            text = it.toFormattedDate(java.util.Locale.getDefault()),
                            fontSize = 12.sp,
                            color = Gray
                        )
                    }
                }
            }

            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { onEditOrRestore(item.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = BackgroundDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onDelete(item.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = Danger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}