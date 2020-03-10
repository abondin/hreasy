package ru.abondin.hreasy.platform

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFluxSecurity
@EnableWebFlux
class HreasyPlatformApplication

fun main(args: Array<String>) {
    runApplication<HreasyPlatformApplication>(*args)
}

inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
