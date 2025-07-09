package com.example.trainboard.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trainboard.api.Client
import com.example.trainboard.structures.Journey
import com.example.trainboard.structures.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootView(modifier: Modifier = Modifier) {
    var departureStation by remember { mutableStateOf<Station?>(null) }
    var arrivalStation by remember { mutableStateOf<Station?>(null) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val journeys = remember { mutableStateListOf<Journey>() }
    var hasSearched by remember { mutableStateOf(false) }

    Box(
        modifier
            .padding(Padding.Large)
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            },
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = Padding.Small,
                alignment = Alignment.Bottom,
            ),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                if (journeys.isEmpty()) {
                    item {
                        Text(
                            text = if (hasSearched) {
                                "No journeys found. Please try different stations."
                            } else {
                                "Search for journeys between two stations."
                            },
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    return@LazyColumn
                }

                items(journeys.toList()) { journey ->
                    JourneyCard(journey)
                }
            }

            StationDropdown(label = "From") { departureStation = it }
            StationDropdown(label = "To") { arrivalStation = it }

            TextButton(
                enabled = departureStation != null && arrivalStation != null,
                onClick = {
                    hasSearched = true
                    handleSearch(departureStation, arrivalStation, scope, snackbarHostState) {
                        journeys.clear()
                        journeys.addAll(it)
                    }
                },
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
fun StationDropdown(
    label: String,
    onStationChange: (Station?) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val stations by Client.stations.collectAsState()
    var selectedStation: Station? by remember { mutableStateOf(null) }

    var textFieldWidth by remember { mutableStateOf(0.dp) }
    val focusRequester = remember { FocusRequester() }

    val filteredStations = remember {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                stations.toList()
            } else {
                stations.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = it
            if (isExpanded) focusRequester.requestFocus()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it

                if (searchQuery != selectedStation?.name) {
                    selectedStation = null
                    onStationChange(null)
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .onFocusEvent {
                    if (it.hasFocus) return@onFocusEvent

                    filteredStations
                        .value
                        .firstOrNull()
                        ?.takeIf { station -> station.name.lowercase() == searchQuery.lowercase() }
                        ?.let { station ->
                            onStationChange(station)
                            searchQuery = station.name
                            selectedStation = station
                            isExpanded = false
                        }
                }.onGloballyPositioned { textFieldWidth = it.size.width.dp },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isExpanded) },
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .focusable(false),
        ) {
            LazyColumn(
                Modifier
                    .width(textFieldWidth)
                    .height(300.dp),
            ) {
                items(filteredStations.value) { station ->
                    DropdownMenuItem(
                        text = { Text(station.name) },
                        onClick = {
                            searchQuery = station.name
                            onStationChange(station)
                            isExpanded = false
                        },
                    )
                }

                if (filteredStations.value.isEmpty()) {
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
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    callback: (List<Journey>) -> Unit,
) {
    requireNotNull(fromStation) { "Origin station not selected!" }
    requireNotNull(fromStation.crs) { "Origin station not valid!" }
    requireNotNull(toStation) { "Destination station not selected!" }
    requireNotNull(toStation.crs) { "Destination station not valid!" }

    if (fromStation == toStation) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "Origin and destination stations cannot be the same.",
                actionLabel = "OK",
                duration = SnackbarDuration.Short,
            )
        }
        return
    }

    scope.launch {
        Client
            .getJourneyFares(fromStation, toStation)
            .outboundJourneys
            .let(callback)
    }
}
