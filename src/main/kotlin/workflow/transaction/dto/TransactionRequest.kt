package com.alsoug.workflow.transaction.dto

import com.alsoug.transaction.AssetType
import com.alsoug.transaction.TransactionState
import com.alsoug.transaction.TransactionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(

    @SerialName("transaction_id")
    val transactionId: String,

    val amount: Double,

    val asset: String,

    @SerialName("asset_type")
    val assetType: AssetType,

    val type: TransactionType,
    val state: TransactionState,

    @SerialName("created_at")
    val createdAt: String
)


