package com.example.trainboard.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
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
            SearchResultView(searchState)

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
