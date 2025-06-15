package com.dzaky3022.asesment1.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.dzaky3022.asesment1.R
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.BackgroundLight
import com.dzaky3022.asesment1.ui.theme.Danger
import com.dzaky3022.asesment1.ui.theme.Success
import com.dzaky3022.asesment1.ui.theme.Warning
import com.dzaky3022.asesment1.utils.WaterIntakeTitleGenerator
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class FormData(
    val title: String = "",
    val description: String? = null,
    val imageUri: Uri? = null,
    val localImagePath: String? = null,
    val deleteImage: Boolean = false
)

@Composable
fun FormDialog(
    isLoading: Boolean = false,
    title: String = "",
    description: String = "",
    imageUrl: String = "", // Remote image URL from API
    localImagePath: String = "", // Local image path for unsynced data
    deleteImage: Boolean = false, // Add this parameter to track deletion state
    generator: WaterIntakeTitleGenerator,
    onDismissRequest: () -> Unit,
    onConfirmation: (FormData) -> Unit
) {
    var formData by remember { mutableStateOf(FormData(title, description)) }
    var titleError by remember { mutableStateOf(false) }
    var useAutoTitle by remember { mutableStateOf(false) }

    // Image state management - clearer state tracking
    var imageState by remember {
        mutableStateOf(
            when {
                // If deleteImage is true, show as deleted regardless of other states
                deleteImage && imageUrl.isNotEmpty() && localImagePath.isNotEmpty() -> ImageState.Deleted
                localImagePath.isNotEmpty() -> ImageState.LocalImage(localImagePath)
                imageUrl.isNotEmpty() -> ImageState.RemoteImage(imageUrl)
                else -> ImageState.NoImage
            }
        )
    }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Save the image to internal storage
            val savedPath = saveImageToInternalStorage(context, it)
            if (savedPath != null) {
                // Update image state to new local image
                imageState = ImageState.NewLocalImage(savedPath)
                formData = formData.copy(
                    imageUri = Uri.parse("file://$savedPath"),
                    localImagePath = savedPath,
                    deleteImage = false // Reset delete flag when new image is selected
                )
            }
        }
    }

    fun validateForm(): Boolean {
        titleError = formData.title.trim().isEmpty()
        return !titleError
    }

    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = BackgroundLight,
                contentColor = BackgroundDark
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Fill in the required field",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )

                // Title Input
                Column {
                    Text(
                        text = "Title *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = BackgroundDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = formData.title,
                        onValueChange = {
                            formData = formData.copy(title = it)
                            titleError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = titleError,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (titleError) Danger else Success,
                            unfocusedBorderColor = if (titleError) Danger else BackgroundDark.copy(
                                alpha = 0.3f
                            )
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                        ),
                        enabled = !useAutoTitle
                    )
                    if (titleError) {
                        Text(
                            text = "This field is required",
                            color = Danger,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            useAutoTitle = !useAutoTitle
                            if (useAutoTitle) {
                                // Generate a new title when checkbox is checked
                                formData = formData.copy(title = generator.generateTitle())
                                titleError = false
                            }
                        }
                    ) {
                        Checkbox(
                            checked = useAutoTitle,
                            onCheckedChange = { checked ->
                                useAutoTitle = checked
                                if (checked) {
                                    // Generate a new title when checkbox is checked
                                    formData = formData.copy(title = generator.generateTitle())
                                    titleError = false
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Success,
                                uncheckedColor = BackgroundDark.copy(alpha = 0.6f),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-generate title for lazy users",
                            fontSize = 14.sp,
                            color = BackgroundDark.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Show generated title info
                    if (useAutoTitle) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✨ Title automatically generated! Uncheck to edit manually.",
                            fontSize = 12.sp,
                            color = Success,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Description Input
                Column {
                    Text(
                        text = "Description (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = BackgroundDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = formData.description ?: "",
                        onValueChange = {
                            formData = formData.copy(description = it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Success,
                            unfocusedBorderColor = BackgroundDark.copy(
                                alpha = 0.3f
                            )
                        ),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                        )
                    )
                }

                // Image Upload Section
                Column {
                    Text(
                        text = "Upload a photo of your drink! (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = BackgroundDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Show image preview based on current state
                    when (val currentState = imageState) {
                        is ImageState.NoImage, is ImageState.Deleted -> {
                            // Show upload button
                            DottedBorderBox(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                borderColor = Color.Gray
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Camera,
                                        contentDescription = "Upload image",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Upload",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        is ImageState.RemoteImage -> {
                            // Show remote image
                            ImagePreview(
                                imageSource = currentState.url,
                                isLocal = false,
                                onDelete = {
                                    imageState = ImageState.Deleted
                                    formData = formData.copy(
                                        imageUri = null,
                                        localImagePath = null,
                                        deleteImage = true
                                    )
                                },
                                onReplace = { imagePickerLauncher.launch("image/*") }
                            )
                        }

                        is ImageState.LocalImage -> {
                            // Show existing local image - only if file actually exists
                            val file = File(currentState.path)
                            if (file.exists()) {
                                ImagePreview(
                                    imageSource = "file://${currentState.path}",
                                    isLocal = true,
                                    onDelete = {
                                        // Clean up local file
                                        try {
                                            File(currentState.path).delete()
                                        } catch (e: Exception) {
                                            // Ignore deletion errors
                                        }
                                        imageState = ImageState.Deleted
                                        formData = formData.copy(
                                            imageUri = null,
                                            localImagePath = null,
                                            deleteImage = true
                                        )
                                    },
                                    onReplace = { imagePickerLauncher.launch("image/*") }
                                )
                            } else {
                                // File doesn't exist, show as deleted
                                imageState = ImageState.Deleted
                                formData = formData.copy(
                                    imageUri = null,
                                    localImagePath = null,
                                    deleteImage = true
                                )

                                // Show upload button
                                DottedBorderBox(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clickable { imagePickerLauncher.launch("image/*") },
                                    borderColor = Color.Gray
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Camera,
                                            contentDescription = "Upload image",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Upload",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        is ImageState.NewLocalImage -> {
                            // Show newly selected local image
                            ImagePreview(
                                imageSource = "file://${currentState.path}",
                                isLocal = true,
                                onDelete = {
                                    // Clean up local file
                                    try {
                                        File(currentState.path).delete()
                                    } catch (e: Exception) {
                                        // Ignore deletion errors
                                    }
                                    // Determine what state to go back to
                                    imageState = when {
                                        localImagePath.isNotEmpty() -> ImageState.Deleted // Had existing local image
                                        imageUrl.isNotEmpty() -> ImageState.Deleted // Had existing remote image
                                        else -> ImageState.NoImage // No previous image
                                    }
                                    formData = formData.copy(
                                        imageUri = null,
                                        localImagePath = null,
                                        deleteImage = localImagePath.isNotEmpty() || imageUrl.isNotEmpty()
                                    )
                                },
                                onReplace = { imagePickerLauncher.launch("image/*") }
                            )
                        }
                    }

                    // Helper text
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "*Supported files: JPG, PNG",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "*Maximum size: 5MB",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // Show status messages
                    when (imageState) {
                        is ImageState.Deleted -> {
                            Text(
                                text = "Image will be deleted",
                                fontSize = 12.sp,
                                color = Danger,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        is ImageState.NewLocalImage -> {
                            Text(
                                text = "New image will be uploaded",
                                fontSize = 12.sp,
                                color = Success,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        else -> {}
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, BackgroundDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontSize = 16.sp,
                            color = BackgroundDark
                        )
                    }

                    Button(
                        onClick = {
                            if (validateForm()) {
                                onConfirmation(formData)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Success
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading)
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        else
                            Text(
                                text = "Submit",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                    }
                }
            }
        }
    }
}

// Sealed class to represent different image states
sealed class ImageState {
    data object NoImage : ImageState()
    data class RemoteImage(val url: String) : ImageState()
    data class LocalImage(val path: String) : ImageState()
    data class NewLocalImage(val path: String) : ImageState()
    data object Deleted : ImageState()
}

@Composable
fun ImagePreview(
    imageSource: String,
    isLocal: Boolean,
    onDelete: () -> Unit,
    onReplace: () -> Unit
) {
    Box(modifier = Modifier.size(90.dp)) {
        Image(
            painter = rememberAsyncImagePainter(imageSource),
            contentDescription = "Selected image",
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onReplace() },
            contentScale = ContentScale.Crop
        )

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(
                    Danger.copy(alpha = 0.9f),
                    RoundedCornerShape(8.dp)
                )
                .size(30.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove image",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Indicator for local images
        if (isLocal) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        Warning.copy(alpha = 0.9f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(2.dp)
            ) {
                Text(
                    text = "Local",
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DottedBorderBox(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Gray,
    borderWidth: Float = 2f,
    dashLength: Float = 8f,
    gapLength: Float = 8f,
    cornerRadius: Float = 8f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength, gapLength), 0f
            )

            drawRoundRect(
                color = borderColor,
                style = Stroke(
                    width = borderWidth,
                    pathEffect = pathEffect
                ),
                cornerRadius = CornerRadius(cornerRadius)
            )
        }
        content()
    }
}

// Function to save image to internal storage immediately
fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) return null

        // Compress and resize the image
        val compressedBitmap = compressAndResizeImage(bitmap)

        // Create a unique filename
        val fileName = "water_result_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        // Save to internal storage
        val outputStream = FileOutputStream(file)
        val success = compressedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            85,
            outputStream
        )
        outputStream.flush()
        outputStream.close()

        if (success && file.exists()) {
            file.absolutePath
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun compressAndResizeImage(bitmap: Bitmap): Bitmap {
    val maxDimension = 1024
    val maxFileSize = 1 * 1024 * 1024 // 1MB

    // Resize if needed
    val resizedBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
        val scaleFactor = minOf(
            maxDimension.toFloat() / bitmap.width,
            maxDimension.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()

        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } else {
        bitmap
    }

    // Compress if needed
    val stream = ByteArrayOutputStream()
    var quality = 90

    do {
        stream.reset()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        quality -= 10
    } while (stream.size() > maxFileSize && quality > 30)

    return resizedBitmap
}