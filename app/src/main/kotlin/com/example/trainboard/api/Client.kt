package com.example.trainboard.api

import com.example.trainboard.BuildConfig
import com.example.trainboard.structures.FareSearchResult
import com.example.trainboard.structures.Station
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URI
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object Client {
    private val URL_BASE = URI("https://int-test1.tram.softwire-lner-dev.co.uk/v1/")

    private val _stations = MutableStateFlow<Set<Station>>(emptySet())
    val stations: StateFlow<Set<Station>> get() = _stations

    init {
        CoroutineScope(Dispatchers.IO).launch {
            client
                .get(URL_BASE.resolve("stations").toString())
                .body<Station.StationsResponse>()
                .let { it.stations.filter { station -> station.crs != null } }
                .let { _stations.value = it.toSet() }
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
        install(HttpCache)
        install(DefaultRequest) {
            header("x-api-key", BuildConfig.API_KEY)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getJourneyFares(
        originStation: Station,
        destinationStation: Station,
    ): FareSearchResult = client
        .get(URL_BASE.resolve("fares").toString()) {
            url {
                parameter("originStation", originStation.crs)
                parameter("destinationStation", destinationStation.crs)
                parameter("outboundDateTime", Clock.System.now().toString())
                parameter("inboundDateTime", null as String?)
                parameter("numberOfChildren", 0)
                parameter("numberOfAdults", 1)
            }
        }.body<FareSearchResult>()
}
