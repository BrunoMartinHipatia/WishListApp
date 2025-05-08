package com.example.wishlistapp.ui.theme.component

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.wishlistapp.R

// Define la familia tipográfica desde res/font
val MyFontFamily = FontFamily(
    Font(R.font.sans_bold, FontWeight.Normal) // usa tu archivo myfont.ttf
)

// Define la tipografía global
val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = MyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)
