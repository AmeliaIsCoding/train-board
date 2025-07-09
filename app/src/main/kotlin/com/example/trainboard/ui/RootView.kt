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
import com.example.trainboard.utilities.Colour
import com.example.trainboard.utilities.LoadState
import com.example.trainboard.utilities.Padding
import com.example.trainboard.utilities.Typography
import com.example.trainboard.utilities.applyIf
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootView(modifier: Modifier = Modifier) {
    var departureStation by remember { mutableStateOf<Station?>(null) }
    var arrivalStation by remember { mutableStateOf<Station?>(null) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchState: LoadState<List<Journey>, String> by remember {
        mutableStateOf(LoadState.Idle)
    }

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
                verticalArrangement = Arrangement.spacedBy(
                    space = Padding.Small,
                    alignment = Alignment.CenterVertically,
                ),
            ) {
                when (searchState) {
                    LoadState.Idle -> {
                        item {
                            Text(
                                text = "Search for journeys between two stations.",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    LoadState.Loading -> {
                        item {
                            Text(
                                text = "Searching for journeys...",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            Text(
                                text = "An error occurred while searching for journeys.",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    is LoadState.Success -> {
                        val journeys = (searchState as LoadState.Success<List<Journey>>).data

                        if (journeys.isEmpty()) {
                            item {
                                Text(
                                    text = "No journeys found. Please try different stations.",
                                    style = Typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            return@LazyColumn
                        }

                        items(journeys) { journey ->
                            JourneyCard(journey)
                        }
                    }
                }
            }

            StationDropdown(label = "From") { departureStation = it }
            StationDropdown(label = "To") { arrivalStation = it }

            TextButton(
                onClick = {
                    scope.launch {
                        if (!checkCanSearch(departureStation, arrivalStation, snackbarHostState)) {
                            return@launch
                        }

                        focusManager.clearFocus()
                        searchState = LoadState.Loading

                        // Smart cast not possible, but contract in `checkCanSearch` hopefully adds
                        // safety. https://kotlinlang.org/docs/typecasts.html#smart-cast-prerequisites
                        handleSearch(departureStation!!, arrivalStation!!) {
                            searchState = LoadState.Success(it)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .applyIf(searchState is LoadState.Loading) { this.shimmer() },
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

@OptIn(ExperimentalContracts::class)
private suspend fun checkCanSearch(
    fromStation: Station?,
    toStation: Station?,
    snackbarHostState: SnackbarHostState,
): Boolean {
    contract {
        returns(true) implies (fromStation != null && toStation != null)
    }

    if (fromStation == null) {
        snackbarHostState.showSnackbar(
            message = "Origin station not selected or invalid.",
            actionLabel = "OK",
            duration = SnackbarDuration.Short,
        )

        return false
    }

    if (toStation == null) {
        snackbarHostState.showSnackbar(
            message = "Destination station not selected or invalid.",
            actionLabel = "OK",
            duration = SnackbarDuration.Short,
        )

        return false
    }

    requireNotNull(fromStation.crs) { "[Impossible] Origin station does not have a CRS." }
    requireNotNull(toStation.crs) { "[Impossible] Destination station does not have a CRS." }

    if (fromStation == toStation) {
        snackbarHostState.showSnackbar(
            message = "Origin and destination stations cannot be the same.",
            actionLabel = "OK",
            duration = SnackbarDuration.Short,
        )

        return false
    }

    return true
}

private suspend fun handleSearch(
    fromStation: Station,
    toStation: Station,
    callback: (List<Journey>) -> Unit,
) = Client
    .getJourneyFares(fromStation, toStation)
    .outboundJourneys
    .let(callback)
