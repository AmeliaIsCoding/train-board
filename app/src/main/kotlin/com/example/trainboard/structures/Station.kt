package com.example.trainboard.structures

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: Int,
    val name: String,
    val crs: String?,
)
