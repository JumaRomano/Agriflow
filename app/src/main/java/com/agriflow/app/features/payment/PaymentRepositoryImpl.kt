/**
 * Repository implementation of [PaymentRepository] managing remote and local data operations.
 */
package com.agriflow.app.features.payment

import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val paymentApi: PaymentApi
) : PaymentRepository {

    override suspend fun initiateStkPush(
        orderId: String,
        phoneNumber: String,
        amount: Double
    ): Result<StkPushResponseDto, DataError.Network> {
        return safeApiCall {
            paymentApi.initiateStkPush(
                StkPushRequestDto(
                    orderId = orderId,
                    phoneNumber = phoneNumber,
                    amount = amount
                )
            )
        }
    }

    override suspend fun initiateCallback(
        request: CallbackDto
    ): Result<CallbackResponseDto, DataError.Network> {
        return safeApiCall {
            paymentApi.initiateCallback(request)
        }
    }

    override suspend fun getPaymentStatusByCheckoutRequestId(
        checkoutRequestId: String
    ): Result<PaymentDto, DataError.Network> {
        return safeApiCall {
            paymentApi.getPaymentStatusByCheckoutRequestId(checkoutRequestId)
        }
    }

    override suspend fun getMyPayments(): Result<List<PaymentDto>, DataError.Network> {
        return safeApiCall {
            paymentApi.getMyPayments()
        }
    }
}
