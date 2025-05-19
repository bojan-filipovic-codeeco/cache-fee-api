package com.alsoug

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    routing {
        openAPI(path = "openapi")
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-MyCustomHeader")
        // NOTE: anyHost() is used in development only; restrict in production
        anyHost()
    }
}
