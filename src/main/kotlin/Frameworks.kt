package com.alsoug

import com.alsoug.config.restate.restateModule
import com.alsoug.di.appModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(appModule, restateModule)
    }
}
