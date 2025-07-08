package com.example.trainboard.ui.theme

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trainboard.api.Client
import com.example.trainboard.structures.Station
import java.net.URI

val URL_REDIRECT =
    URI("https://www.lner.co.uk/travel-information/travelling-now/live-train-times/depart/")

object Padding {
    val Small = 8.dp

    val Medium = 16.dp

    val Large = 32.dp
}

val Colour: ColorScheme
    @Composable get() = MaterialTheme.colorScheme

val Typography: androidx.compose.material3.Typography
    @Composable get() = MaterialTheme.typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootView(modifier: Modifier = Modifier) {
    var stationFrom by remember { mutableStateOf<Station?>(null) }
    var stationTo by remember { mutableStateOf<Station?>(null) }
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = modifier.padding(Padding.Large),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Padding.Medium)) {
            StationSelect(label = "From") { stationFrom = it }
            StationSelect(label = "To") { stationTo = it }

            TextButton(
                enabled = stationFrom != null && stationTo != null,
                onClick = {
                    requireNotNull(stationFrom) { "Origin station not selected!" }
                    requireNotNull(stationFrom?.crs) { "Origin station not valid!" }
                    requireNotNull(stationTo) { "Destination station not selected!" }
                    requireNotNull(stationTo?.crs) { "Destination station not valid!" }
                    uriHandler.openUri(
                        URL_REDIRECT
                            .resolve("${stationFrom?.crs}/")
                            .resolve(stationTo?.crs)
                            .toString(),
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Colour.primaryContainer,
                    contentColor = Colour.onPrimaryContainer,
                ),
            ) {
                Text(
                    "Check",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelect(
    label: String,
    onStationChange: (Station) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    val stations by Client.stations.collectAsState()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedStation?.name ?: "Select a station",
            onValueChange = { },
            modifier = Modifier
                .clickable { expanded = !expanded }
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.animateContentSize(),
        ) {
            stations.toList().forEach { station ->
                DropdownMenuItem(
                    text = { Text(station.name) },
                    onClick = {
                        selectedStation = station
                        onStationChange(station)
                        expanded = false
                    },
                )
            }
        }
    }
}
