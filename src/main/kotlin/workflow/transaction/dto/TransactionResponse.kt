package com.alsoug.workflow.transaction.dto

import com.alsoug.transaction.TransactionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponse(

    @SerialName("transaction_id")
    val transactionId: String,

    val amount: Double,

    val asset: String,
    val type: TransactionType,

    val fee: Double,
    val rate: Double,

    val description: String
)
