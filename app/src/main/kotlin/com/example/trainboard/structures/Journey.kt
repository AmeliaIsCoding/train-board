package com.example.trainboard.structures

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@OptIn(ExperimentalTime::class)
data class Journey(
    val journeyOptionToken: String,
    val journeyId: String,
    @Serializable(with = Station.StationSerializer::class)
    val originStation: Station,
    @Serializable(with = Station.StationSerializer::class)
    val destinationStation: Station,
    val departureTime: @Contextual Instant,
    val arrivalTime: @Contextual Instant,
    val status: Status,
    val legs: List<JourneyLeg>,
    val tickets: List<Ticket>,
    val journeyDurationInMinutes: Int,
    val isFastestJourney: Boolean,
)
