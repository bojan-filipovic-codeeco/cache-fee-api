package com.alsoug

import com.alsoug.transaction.transactionRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        transactionRoutes()
    }
}
