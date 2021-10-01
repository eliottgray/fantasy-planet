package com.eliottgray.kotlin

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    routing {
        get("/tiles/{seed}/{z}/{x}/{y}.png") {
            // TODO: Validate these params.
            val seed = call.parameters["seed"]!!.toDouble()
            val z = call.parameters["z"]!!.toInt()
            val x = call.parameters["x"]!!.toInt()
            val y = call.parameters["y"]!!.toInt()
            call.application.environment.log.debug("Requesting tile $z/$x/$y.png")
            val mapTileKey = MapTileKey(z, x, y, seed)
            val mapTile = MapTileCache.getTile(mapTileKey)
            call.respondBytes(mapTile.pngByteArray, ContentType.Image.PNG, HttpStatusCode.OK)
        }
        get("/") {
            // TODO: Serve statically instead?  Or describe the HTML in code instead?  Templating engine?  :shrug:
            val index = File("web/templates/index.html")
            call.respondFile(index)
        }
    }
}
