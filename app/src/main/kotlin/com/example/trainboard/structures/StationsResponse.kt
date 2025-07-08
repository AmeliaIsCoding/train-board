package com.example.trainboard.structures

import kotlinx.serialization.Serializable

@Serializable
data class StationsResponse(
    val stations: List<Station>,
)
