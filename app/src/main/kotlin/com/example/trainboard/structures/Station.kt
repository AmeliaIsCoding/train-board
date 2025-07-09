package com.example.trainboard.structures

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure

@Serializable
data class Station(
    val id: Int,
    val name: String,
    val crs: String?,
) {
    @Serializable
    data class StationsResponse(
        val stations: List<Station>,
    )

    @OptIn(ExperimentalSerializationApi::class)
    class StationSerializer : KSerializer<Station> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Station") {
            element<String>("displayName")
            element<String>("crs")
            element<String>("nlc")
        }

        override fun serialize(encoder: Encoder, value: Station) =
            encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, value.name)
                encodeStringElement(descriptor, 1, value.crs ?: "")
                encodeStringElement(descriptor, 2, "")
            }

        override fun deserialize(decoder: Decoder): Station {
            val decoder = decoder.beginStructure(descriptor)
            var displayName = ""
            var crs = ""

            loop@ while (true) {
                when (val index = decoder.decodeElementIndex(descriptor)) {
                    0 -> displayName = decoder.decodeStringElement(descriptor, index)
                    1 -> crs = decoder.decodeStringElement(descriptor, index)
                    2 -> decoder.decodeStringElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break@loop
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }

            decoder.endStructure(descriptor)
            return Station(0, displayName, crs)
        }
    }
}
