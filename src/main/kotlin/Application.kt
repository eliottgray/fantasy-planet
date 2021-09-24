package com.eliottgray.kotlin

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
//            get("/") {
//                call.respondText("Hello, world!")
//            }
            static("tiles") {
                files("web/tiles/")
            }
            static("/") {
                files("web/templates/")
                default("web/templates/index.html")
            }
        }
    }.start(wait = true)
}