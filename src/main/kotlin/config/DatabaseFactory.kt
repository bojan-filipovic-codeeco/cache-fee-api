package com.alsoug.config

import com.alsoug.transaction.db.TransactionTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = environment.config.property("postgres.url").getString()
        driverClassName = "org.postgresql.Driver"
        username = environment.config.property("postgres.user").getString()
        password = environment.config.property("postgres.password").getString()
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(TransactionTable)
    }
}
