package com.alsoug

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val method = call.request.httpMethod.value
            val uri = call.request.uri
            "HTTP $method - $uri"
        }
    }
}
