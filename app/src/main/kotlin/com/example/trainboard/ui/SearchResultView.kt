package com.example.trainboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.trainboard.structures.Journey
import com.example.trainboard.utilities.LoadState
import com.example.trainboard.utilities.Padding
import com.example.trainboard.utilities.Typography

@Composable
fun ColumnScope.SearchResultView(searchState: LoadState<List<Journey>, String>) {
    when (searchState) {
        LoadState.Idle -> {
            Text(
                text = "Search for journeys between two stations.",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        LoadState.Loading -> {
            Text(
                text = "Searching for journeys...",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        is LoadState.Error -> {
            Text(
                text = "An error occurred while searching for journeys.",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        is LoadState.Success -> {
            val journeys = (searchState as LoadState.Success<List<Journey>>).data

            if (journeys.isEmpty()) {
                Text(
                    text = "No journeys found. Please try different stations.",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                return
            }

            Text(
                "Found ${journeys.size} journeys.",
                modifier = Modifier
                    .padding(Padding.Medium)
                    .fillMaxWidth(),
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    space = Padding.Small,
                    alignment = Alignment.CenterVertically,
                ),
            ) {
                items(journeys) { journey ->
                    JourneyCard(journey)
                }
            }
        }
    }
}
