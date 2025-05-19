package com.alsoug.transaction

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(

    val id: String,
    val amount: Double,
    val asset: String,
    val assetType: AssetType,
    val type: TransactionType,
    val state: TransactionState,
    val createdAt: String,
    val fee: Double,
    val rate: Double,
    val description: String
)
