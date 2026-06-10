package com.agriflow.app.features.payment

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {

    @POST("payments/stk-push")
    suspend fun initiateStkPush(
        @Body request: StkPushRequestDto
    ): Response<StkPushResponseDto>

    @POST("payments/mpesa/callback")
    suspend fun initiateCallback(
        @Body request: CallbackDto
    ): Response<CallbackResponseDto>

    @GET("payments/status/{checkoutRequestId}")
    suspend fun getPaymentStatusByCheckoutRequestId(
        @Path("checkoutRequestId") checkoutRequestId: String
    ): Response<PaymentDto>

    @GET("payments/my-payments")
    suspend fun getMyPayments(): Response<List<PaymentDto>>
}
