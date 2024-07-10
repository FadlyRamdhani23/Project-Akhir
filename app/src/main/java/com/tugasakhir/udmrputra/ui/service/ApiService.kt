package com.tugasakhir.udmrputra.ui.service

import com.tugasakhir.udmrputra.data.TransactionStatus
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("check-transaction/order/{orderId}")
    fun getTransactionStatusByOrderId(@Path("orderId") orderId: String): Call<TransactionStatus>

    @GET("check-transaction/transaction/{transactionId}")
    fun getTransactionStatusByTransactionId(@Path("transactionId") transactionId: String): Call<TransactionStatus>
}
