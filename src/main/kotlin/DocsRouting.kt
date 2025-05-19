package com.alsoug

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureDocs() {
    val port = environment.config.property("ktor.deployment.port").getString()
    println("Swagger UI available at: http://localhost:$port/docs")

    routing {
        staticFiles("/docs", File("docs")) {
            default("index.html")
        }
    }
}
