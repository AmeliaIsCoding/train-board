package com.example.trainboard.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

object Padding {
    val Small = 8.dp

    val Medium = 16.dp

    val Large = 32.dp
}

val Colour: ColorScheme
    @Composable get() = MaterialTheme.colorScheme

val Typography: Typography
    @Composable get() = MaterialTheme.typography

val HourMinuteFormatter = DateTimeComponents.Format {
    hour(padding = Padding.ZERO)
    char(':')
    minute(padding = Padding.ZERO)
}
