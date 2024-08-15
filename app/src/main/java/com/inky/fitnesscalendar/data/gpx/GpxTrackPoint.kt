package com.inky.fitnesscalendar.data.gpx

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

@Serializable
data class GpxTrackPoint(
    val coordinate: Coordinate,
    @Serializable(DateSerializer::class)
    val time: Date
) {
    private object DateSerializer : KSerializer<Date> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

        override fun serialize(encoder: Encoder, value: Date) {
            encoder.encodeLong(value.time)
        }

        override fun deserialize(decoder: Decoder) = Date(decoder.decodeLong())
    }
}
