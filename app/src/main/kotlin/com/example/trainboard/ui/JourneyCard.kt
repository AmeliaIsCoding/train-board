package com.example.trainboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trainboard.structures.Journey
import com.example.trainboard.structures.Station
import com.example.trainboard.structures.Status
import com.example.trainboard.utilities.Colour
import com.example.trainboard.utilities.HourMinuteFormatter
import com.example.trainboard.utilities.Padding
import com.example.trainboard.utilities.Typography
import kotlinx.datetime.format
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun JourneyCard(journey: Journey, modifier: Modifier = Modifier) {
    Card(
        modifier
            .background(Colour.background)
            .height(80.dp),
    ) {
        Column(
            Modifier.padding(Padding.Medium),
            verticalArrangement = Arrangement.Center,
        ) {
            TimesAndStations(
                startTime = journey.departureTime,
                endTime = journey.arrivalTime,
                journeyDuration = journey.journeyDurationInMinutes.minutes,
                startStation = journey.originStation.name,
                endStation = journey.destinationStation.name,
                showStation = false,
                numberOfChanges = journey.legs.size - 1,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun TimesAndStations(
    startTime: Instant,
    endTime: Instant,
    journeyDuration: Duration,
    startStation: String,
    endStation: String,
    showStation: Boolean,
    numberOfChanges: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DisplayTime(
            time = startTime,
            station = startStation,
            show = showStation,
        )

        ArrowWithDuration(journeyDuration, numberOfChanges)

        DisplayTime(
            time = endTime,
            station = endStation,
            show = showStation,
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DisplayTime(
    time: Instant,
    station: String,
    show: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            time.format(HourMinuteFormatter),
            style = Typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        if (show) {
            Text(
                station,
                style = Typography.labelMedium,
            )
        }
    }
}

@Composable
fun ArrowWithDuration(
    duration: Duration,
    numberOfChanges: Int,
) {
    val colour = Colour.onBackground

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = duration.toComponents { minutes, _, _ ->
                "${minutes / 60}h ${minutes % 60}m"
            },
            style = Typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )

        Canvas(
            Modifier
                .fillMaxWidth(.3f)
                .height(5.dp),
        ) {
            drawLine(
                color = colour,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 4f,
            )

            val headSize = 10f // Arrowhead size in pixels

            drawLine(
                color = colour,
                start = Offset(size.width, center.y),
                end = Offset(size.width - headSize, center.y - headSize),
                strokeWidth = 4f,
            )
            drawLine(
                color = colour,
                start = Offset(size.width, center.y),
                end = Offset(size.width - headSize, center.y + headSize),
                strokeWidth = 4f,
            )
        }

        Text(
            text = if (numberOfChanges == 0) "Direct" else "Changes: $numberOfChanges",
            style = Typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun JourneyCardPreview() {
    JourneyCard(
        journey = Journey(
            departureTime = Instant.fromEpochMilliseconds(1700000000000),
            arrivalTime = Instant.fromEpochMilliseconds(1700000036000),
            journeyDurationInMinutes = 60,
            originStation = Station(
                id = 1,
                name = "London Kings Cross",
                crs = "KGX",
            ),
            destinationStation = Station(
                id = 2,
                name = "Edinburgh Waverley",
                crs = "EDB",
            ),
            journeyOptionToken = "",
            journeyId = "",
            status = Status.NORMAL,
            tickets = emptyList(),
            isFastestJourney = true,
            legs = emptyList(),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(Padding.Medium),
    )
}
