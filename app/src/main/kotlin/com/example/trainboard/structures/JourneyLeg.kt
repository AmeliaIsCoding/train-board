package com.example.trainboard.structures

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
sealed class JourneyLeg {
    companion object {
        val Module = SerializersModule {
            polymorphic(JourneyLeg::class) {
                subclass(Trip::class)
                subclass(Transfer::class)
            }
        }
    }

    abstract val legId: String

    abstract val mode: Mode

    abstract val origin: Station

    abstract val destination: Station

    abstract val durationInMinutes: Int

    @OptIn(ExperimentalTime::class)
    @Serializable
    @SerialName("trip")
    data class Trip(
        override val legId: String,
        override val mode: Mode,
        override val origin:
            @Serializable(Station.StationSerializer::class)
            Station,
        override val destination:
            @Serializable(Station.StationSerializer::class)
            Station,
        override val durationInMinutes: Int,
        val departureDateTime: @Contextual Instant,
        val arrivalDateTime: @Contextual Instant,
    ) : JourneyLeg()

    @Serializable
    @SerialName("transfer")
    data class Transfer(
        override val legId: String,
        override val mode: Mode,
        override val origin:
            @Serializable(Station.StationSerializer::class)
            Station,
        override val destination:
            @Serializable(Station.StationSerializer::class)
            Station,
        override val durationInMinutes: Int,
    ) : JourneyLeg()

    @Serializable
    enum class Mode {
        SCHEDULED_BUS,
        REPLACEMENT_BUS,
        BUS,
        TRAIN,
        FERRY,
        WALK,
        UNDERGROUND,
        TAXI,
        METRO,
        TRAMLINK,
        PLATFORM_CHANGE,
        CHECK_IN_TIME,
        HOVERCRAFT,
        TRANSFER,
        TRAM,
        DLR,
        LU,
        DLR_LU,
        WALK_TUBE,
        WALK_DLR,
        WALK_TUBE_DLR,
        UNKNOWN,
    }
}
