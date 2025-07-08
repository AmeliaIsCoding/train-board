package com.example.trainboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.platform.LocalContext
import com.example.trainboard.ui.theme.TrainBoardTheme
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainBoardTheme {
                Scaffold(
                    content = { paddingValues ->
                        Box(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                        ) {
                            TrainSelectorScreen(modifier = Modifier.align(Alignment.Center))

                        }
                    },
                )
            }
        }
    }
}

@Composable
fun TrainSelectorScreen(modifier: Modifier = Modifier) {
    var selectedDepartureStation by remember { mutableStateOf("") }
    var selectedArrivalStation by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf(" ") }
    val stations = listOf("London Kings Cross", "Edinburgh", "Inverness", "Hull", "Darlington")
    val stationCodesMap = mapOf("London Kings Cross" to "KGX", "Edinburgh" to "EDB", "Inverness" to "INV", "Hull" to "HUL", "Darlington" to "DAR")

    Column(modifier.padding(20.dp)) {
        DropdownMenu(
            selectedDepartureStation,
            onStationSelected = { selectedDepartureStation = it
                url = CreateURL(
                    stationCodesMap[selectedDepartureStation].toString(),
                    stationCodesMap[selectedArrivalStation].toString()
                )
                isButtonEnabled = selectedDepartureStation.isNotEmpty() && selectedArrivalStation.isNotEmpty() && selectedArrivalStation != selectedDepartureStation
            },
            stations = stations,
            dropdownLabel = "From")
        DropdownMenu(
            selectedArrivalStation,
            onStationSelected = {
                selectedArrivalStation = it
                url = CreateURL(
                    stationCodesMap[selectedDepartureStation].toString(),
                    stationCodesMap[selectedArrivalStation].toString()
                )
                isButtonEnabled = selectedDepartureStation.isNotEmpty() && selectedArrivalStation.isNotEmpty() && selectedArrivalStation != selectedDepartureStation
            },
            stations = stations,
            dropdownLabel = "To")
        SubmitStations(url = url, isEnabled = isButtonEnabled)
    }


}


@Composable
fun DropdownMenu(selectedStation: String, onStationSelected: (String) -> Unit, stations: List<String>, dropdownLabel: String) {
    var mExpanded by remember { mutableStateOf(false) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column() {
        OutlinedTextField(
            readOnly = true,
            value = selectedStation,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    mTextFieldSize = coordinates.size.toSize()
                },
            label = {Text(dropdownLabel)},
            trailingIcon = {
                Icon(icon,"downArrow",
                    Modifier.clickable { mExpanded = !mExpanded })
            }
        )

        DropdownMenu(
            expanded = mExpanded,
            onDismissRequest = { mExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current){mTextFieldSize.width.toDp()})
        ) {
            stations.forEach { label ->
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        onStationSelected(label)
                        mExpanded = false
                    }
                )
            }
        }

    }
}

@Composable
fun SubmitStations(url: String, isEnabled: Boolean) {
    val context = LocalContext.current
    Button(
        enabled = isEnabled,
        onClick = {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }) {
        Text("View Live Departures ")
    }

}

fun CreateURL(toStation: String, fromStation: String): String {
   return "https://www.lner.co.uk/travel-information/travelling-now/live-train-times/depart/$toStation/$fromStation/#LiveDepResults"
}