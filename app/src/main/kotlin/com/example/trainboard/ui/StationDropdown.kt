package com.example.trainboard.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.trainboard.api.Client
import com.example.trainboard.structures.Station

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
