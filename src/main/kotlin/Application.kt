package com.eliottgray.kotlin

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/tiles/{seed}/{z}/{x}/{y}.png") {
                // TODO: Validate these params.
                val seed = call.parameters["seed"]!!
                val z = call.parameters["z"]!!
                val x = call.parameters["x"]!!
                val y = call.parameters["y"]!!
                call.application.environment.log.info("Requesting tile $z/$x/$y.png")  // TODO: Debug rather than info.
                val byteArray = MapTileCache.getTile(MapTileKey(z.toInt(), x.toInt(), y.toInt(), seed.toDouble()))
                call.respondBytes(byteArray, ContentType.Image.PNG, HttpStatusCode.OK)
            }
            get("/") {
                // TODO: Serve statically instead?  Or describe the HTML in code instead?  Templating engine?  :shrug:
                val index = File("web/templates/index.html")
                call.respondFile(index)
            }
        }
    }.start(wait = true)
}