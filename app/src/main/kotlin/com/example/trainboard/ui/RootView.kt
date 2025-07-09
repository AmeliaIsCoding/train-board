package com.example.trainboard.ui.theme

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trainboard.api.Client
import com.example.trainboard.structures.Station
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootView(modifier: Modifier = Modifier) {
    var fromStation by remember { mutableStateOf<Station?>(null) }
    var toStation by remember { mutableStateOf<Station?>(null) }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier
            .padding(Padding.Large)
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = Padding.Small,
                alignment = Alignment.Bottom,
            ),
        ) {
            StationSelect(label = "From") { fromStation = it }
            StationSelect(label = "To") { toStation = it }

            TextButton(
                enabled = fromStation != null && toStation != null,
                onClick = { handleSearch(fromStation, toStation) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Colour.primaryContainer,
                    contentColor = Colour.onPrimaryContainer,
                ),
            ) {
                Text(
                    text = "Search",
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
    var searchQuery by remember { mutableStateOf("") }
    val stations by Client.stations.collectAsState()
    val focusRequester = remember { FocusRequester() }

    val filteredStations = remember(searchQuery, stations) {
        if (searchQuery.isBlank()) {
            stations
        } else {
            stations.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
            focusRequester.requestFocus()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .focusRequester(focusRequester)
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .onFocusEvent {
                    if (it.hasFocus) return@onFocusEvent

                    filteredStations
                        .firstOrNull()
                        ?.takeIf { station -> station.name.lowercase() == searchQuery.lowercase() }
                        ?.let { station ->
                            onStationChange(station)
                            searchQuery = station.name
                            expanded = false
                        }
                },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .animateContentSize()
                .focusable(false),
        ) {
            LazyColumn(
                Modifier
                    .width(LocalConfiguration.current.screenWidthDp.dp - Padding.Large * 2)
                    .height(300.dp),
            ) {
                items(filteredStations.toList()) { station ->
                    DropdownMenuItem(
                        text = { Text(station.name) },
                        onClick = {
                            searchQuery = station.name
                            onStationChange(station)
                            expanded = false
                        },
                    )
                }

                if (filteredStations.isEmpty()) {
                    item {
                        DropdownMenuItem(
                            text = { Text("No results") },
                            onClick = {},
                            enabled = false,
                        )
                    }
                }
            }
        }
    }
}

private fun handleSearch(
    fromStation: Station?,
    toStation: Station?,
) {
    requireNotNull(fromStation) { "Origin station not selected!" }
    requireNotNull(fromStation.crs) { "Origin station not valid!" }
    requireNotNull(toStation) { "Destination station not selected!" }
    requireNotNull(toStation.crs) { "Destination station not valid!" }

    if (fromStation == toStation) {
    }

    runBlocking {
        Client.getJourneyFares(fromStation, toStation)
    }
}
