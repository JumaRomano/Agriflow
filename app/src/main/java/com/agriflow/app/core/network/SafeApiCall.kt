/**
 * Core helper component: SafeApiCall.
 */
package com.agriflow.app.core.network

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.google.gson.JsonParseException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend inline fun <reified T> safeApiCall(
    execute: suspend () -> Response<T>
): Result<T, DataError.Network> {
    return try {
        val response = execute()
        val body = response.body()

        when {
            response.isSuccessful && body != null -> Result.Success(body)
            response.isSuccessful && T::class == Unit::class -> {
                @Suppress("UNCHECKED_CAST")
                Result.Success(Unit as T)
            }
            response.isSuccessful -> Result.Error(DataError.Network.SERIALIZATION)
            else -> Result.Error(response.code().toNetworkError())
        }
    } catch (_: SocketTimeoutException) {
        Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (_: UnknownHostException) {
        Result.Error(DataError.Network.NO_INTERNET)
    } catch (_: IOException) {
        Result.Error(DataError.Network.NO_INTERNET)
    } catch (_: JsonParseException) {
        Result.Error(DataError.Network.SERIALIZATION)
    } catch (_: RuntimeException) {
        Result.Error(DataError.Network.UNKNOWN)
    }
}

fun Int.toNetworkError(): DataError.Network {
    return when (this) {
        404 -> DataError.Network.NOT_FOUND
        408 -> DataError.Network.REQUEST_TIMEOUT
        401, 403 -> DataError.Network.UNAUTHORIZED
        409 -> DataError.Network.CONFLICT
        413 -> DataError.Network.PAYLOAD_TOO_LARGE
        429 -> DataError.Network.TOO_MANY_REQUESTS
        in 500..599 -> DataError.Network.SERVER_ERROR
        else -> DataError.Network.UNKNOWN
    }
}
