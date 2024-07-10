package com.tugasakhir.udmrputra.data

data class TransactionStatus(
    val status_code: String,
    val transaction_id: String,
    val gross_amount: String,
    val currency: String,
    val order_id: String,
    val payment_type: String,
    val signature_key: String,
    val transaction_status: String,
    val fraud_status: String,
    val status_message: String,
    val merchant_id: String,
    val va_numbers: List<VANumber>,
    val payment_amounts: List<PaymentAmount>?, // Allow null
    val transaction_time: String,
    val settlement_time: String,
    val expiry_time: String
)

data class VANumber(
    val bank: String,
    val va_number: String
)

data class PaymentAmount(
    val payment_type: String,
    val amount: String
)