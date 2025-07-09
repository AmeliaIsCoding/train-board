package com.example.trainboard.utilities

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
inline fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier =
    if (condition) {
        this.block()
    } else {
        this
    }
