/**
 * Repository interface for managing data transactions related to Payment.
 */
package com.agriflow.app.features.payment

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result

interface PaymentRepository {
    suspend fun initiateStkPush(
        orderId: String,
        phoneNumber: String,
        amount: Double
    ): Result<StkPushResponseDto, DataError.Network>

    suspend fun initiateCallback(
        request: CallbackDto
    ): Result<CallbackResponseDto, DataError.Network>

    suspend fun getPaymentStatusByCheckoutRequestId(
        checkoutRequestId: String
    ): Result<PaymentDto, DataError.Network>

    suspend fun getMyPayments(): Result<List<PaymentDto>, DataError.Network>
}
