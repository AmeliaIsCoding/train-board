package com.example.trainboard.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    @SerialName("normal")
    NORMAL,

    @SerialName("delayed")
    DELAYED,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("fully_reserved")
    FULLY_RESERVED,
}
