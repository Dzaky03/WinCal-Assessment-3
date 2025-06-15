package com.dzaky3022.asesment1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dzaky3022.asesment1.R

val Poppins = FontFamily(
    Font(R.font.poppins_regular),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_semibold, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontSize = 12.sp,
        color = Color.Black,
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontSize = 14.sp,
        color = Color.Black,
    ),
    bodyLarge = TextStyle(
        fontFamily = Poppins,
        fontSize = 16.sp,
        color = Color.Black,
    ),
    labelSmall = TextStyle(
        fontFamily = Poppins,
        fontSize = 18.sp,
        color = Color.Black,
    ),
    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontSize = 20.sp,
        color = Color.Black,
    ),
    labelLarge = TextStyle(
        fontFamily = Poppins,
        fontSize = 22.sp,
        color = Color.Black,
    ),
    displaySmall = TextStyle(
        fontFamily = Poppins,
        fontSize = 24.sp,
        color = Color.Black,
    ),
    displayMedium = TextStyle(
        fontFamily = Poppins,
        fontSize = 26.sp,
        color = Color.Black,
    ),
    displayLarge = TextStyle(
        fontFamily = Poppins,
        fontSize = 28.sp,
        color = Color.Black,
    ),
    headlineLarge = TextStyle(
        fontFamily = Poppins,
        fontSize = 35.sp,
        color = Color.Black,
    ),
)