package com.example.trainboard.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object Padding {
    val Small = 8.dp

    val Large = 32.dp
}

val Colour: ColorScheme
    @Composable get() = MaterialTheme.colorScheme

val Typography: androidx.compose.material3.Typography
    @Composable get() = MaterialTheme.typography
