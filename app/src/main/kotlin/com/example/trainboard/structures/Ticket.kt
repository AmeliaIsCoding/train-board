package com.example.trainboard.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ticket(
    val ticketOptionToken: String,
    val priceInPennies: Int,
    @SerialName("ticketType")
    val type: Type,
    @SerialName("ticketClass")
    val `class`: Class,
    @SerialName("ticketCategory")
    val category: Category,
    val numberOfTickets: Int,
    @SerialName("isCheapestTicket")
    val isCheapest: Boolean,
) {
    enum class Type {
        @SerialName("single")
        SINGLE,

        @SerialName("return")
        RETURN,
    }

    enum class Class {
        @SerialName("standard")
        STANDARD,

        @SerialName("first")
        FIRST,
    }

    enum class Category {
        @SerialName("advance")
        ADVANCE,

        @SerialName("anytime")
        ANYTIME,

        @SerialName("off_peak")
        OFF_PEAK,
    }
}
