package com.example.trainboard.structures

import kotlinx.serialization.Serializable

@Serializable
data class FareSearchResult(
    val outboundJourneys: List<Journey>,
    val inboundJourneys: List<Journey>? = null,
)
