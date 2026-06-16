/**
 * Represents the interface [Result] providing core functionality within the application.
 */
package com.agriflow.app.core.util

// Using 'out' for covariance so we can pass specific types up the chain
sealed interface Result<out D, out E : com.agriflow.app.core.util.Error> {
    data class Success<out D, out E : com.agriflow.app.core.util.Error>(val data: D) : Result<D, E>
    data class Error<out D, out E : com.agriflow.app.core.util.Error>(val error: E) : Result<D, E>
}


inline fun <T, E : Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E : Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map { }
}

typealias EmptyResult<E> = Result<Unit, E>
