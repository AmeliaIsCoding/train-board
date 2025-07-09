package com.example.trainboard.utilities

sealed class LoadState<out T, out E> {
    object Idle : LoadState<Nothing, Nothing>()

    object Loading : LoadState<Nothing, Nothing>()

    data class Success<T>(val data: T) : LoadState<T, Nothing>()

    data class Error<E>(val exception: E) : LoadState<Nothing, E>()
}
