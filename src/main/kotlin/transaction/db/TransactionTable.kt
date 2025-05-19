package com.alsoug.transaction.db

import org.jetbrains.exposed.sql.Table

object TransactionTable : Table("transactions") {

    val id = varchar("id", 50)
    val amount = double("amount")
    val asset = varchar("asset", 50)
    val assetType = varchar("asset_type", 50)
    val type = varchar("type", 50)
    val state = varchar("state", 50)
    val createdAt = varchar("created_at", 50) // ISO-8601 String

    val fee = double("fee")
    val rate = double("rate")
    val description = text("description")

    override val primaryKey = PrimaryKey(id)
}
