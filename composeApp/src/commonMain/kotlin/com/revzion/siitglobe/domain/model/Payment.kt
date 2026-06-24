package com.revzion.siitglobe.domain.model

import kotlinx.datetime.LocalDate

data class Payment(
    val id: String,
    val receiptNumber: String,
    val studentId: String,
    val courseId: String,
    val amount: Double,
    val paymentDate: LocalDate,
    val paymentMode: PaymentMode,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val remarks: String? = null,
    val createdAt: String = "",
)

enum class PaymentMode { CASH, ONLINE, CHEQUE, DD, UPI, CARD }

enum class PaymentStatus { COMPLETED, PENDING, FAILED }
