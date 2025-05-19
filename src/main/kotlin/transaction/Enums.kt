package com.alsoug.transaction

import kotlinx.serialization.Serializable

@Serializable
enum class AssetType {

    FIAT,
    CRYPTO
}

@Serializable
enum class TransactionType {

    MOBILE_TOP_UP,
    BANK_TRANSFER,
    CASH_OUT
}

@Serializable
enum class TransactionState {

    SETTLED_PENDING_FEE,
    COMPLETED
}
