package com.eliottgray.kotlin

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) = runBlocking {

    val isDemo = environment.config.propertyOrNull("ktor.demo.enabled")?.getString()?.toBooleanStrictOrNull()
        ?: true  // We want to default to demo behavior to be safe, and avoid undesired computation.
    val demoDepth = environment.config.propertyOrNull("ktor.demo.depth")?.getString()?.toIntOrNull() ?: -1
    val demoSeed = environment.config.propertyOrNull("ktor.demo.seed")?.getString()?.toDoubleOrNull() ?: 0.12345
    if (isDemo) {
        log.info("Demo mode initializing.")
        val writer = MapTileWriter(demoDepth, demoSeed)
        writer.collectAndWrite(demoSeed)
        log.info("Demo mode initialization complete.")
    }

    routing {
        get("/tiles/{seed}/{z}/{x}/{y}.png") {
            buildMapTileKey(call.parameters)
                .flatMap { mapTileKey -> mapTileKey.validate() }
                .flatMap { mapTileKey ->
                    val keyPath = "${mapTileKey.z}/${mapTileKey.x}/${mapTileKey.y}"
                    log.debug("Requesting tile ${mapTileKey.seed}/$keyPath.png")
                    if (isDemo) {
                        val demoTileFile = File("web/tiles/$keyPath.png")
                        if (demoTileFile.exists()){
                            call.respondFile(demoTileFile).right()
                        } else {
                            Pair(HttpStatusCode.NotFound, "Demo tile not found: $keyPath.png").left()
                        }
                    } else {
                        val mapTile = MapTileCache.getTile(mapTileKey)
                        call.respondBytes(mapTile.pngByteArray, ContentType.Image.PNG, HttpStatusCode.OK).right()
                    }
                }.mapLeft { errorPair ->
                    log.error(errorPair.second)
                    call.respond(errorPair.first, errorPair.second)
                }
        }

        get("/") {
            val randomOrDemoSeed = if (isDemo) demoSeed.toString() else "Math.random()"
            val maxDepth = if (isDemo && demoDepth < 20) demoDepth.toString() else 20.toString()
            call.respondHtml(HttpStatusCode.OK) {
                lang = "en"
                head {
                    meta { charset = "utf-8" }
                    script(type = ScriptType.textJavaScript) {
                        src="https://cesium.com/downloads/cesiumjs/releases/1.85/Build/Cesium/Cesium.js"
                    }
                    link {
                        href="https://cesium.com/downloads/cesiumjs/releases/1.85/Build/Cesium/Widgets/widgets.css"
                        rel="stylesheet"
                    }
                    title {
                        +"Fantasy Planet"
                    }
                }
                body {
                    div {
                        id="cesiumContainer"
                    }
                    script(type = ScriptType.textJavaScript) {
                        unsafe {
                            raw("""
// TODO: Allow user to input this seed, and regenerate the map, rather than needing to hit 'refresh'.
var seed = $randomOrDemoSeed
var maxDepth = $maxDepth;  // TODO: 20 is OSM lowest, choose based on size per pixel per zoom level.

const viewer = new Cesium.Viewer('cesiumContainer', {
    // Base layers include helpfully pre-populated, but unnecessary for our use case, real world data.
    // Some of the available layers additionally require a Cesium Ion API key, and trigger a nag.
    baseLayerPicker : false,

    // Geocoder relates to real world data, and also triggers nag regarding api key.
    geocoder: false,

    // TODO: enable a default spinning animation.
    animation: false,

    // Local test.
    imageryProvider: new Cesium.UrlTemplateImageryProvider({
      url : '/tiles/{seed}/{z}/{x}/{y}.png',
      maximumLevel: maxDepth,
      tilingScheme : new Cesium.GeographicTilingScheme(),
      customTags : {
        seed: function(imageryProvider, x, y, level) {
          return seed
        }
      }
    })
});    
"""
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun buildMapTileKey(callParameters: Parameters): Either<Pair<HttpStatusCode, String>, MapTileKey> {
    return either {
        val seed = (callParameters["seed"]!!.toDoubleOrNull()?.right() ?: Pair(
            HttpStatusCode.BadRequest,
            "Seed must be a number."
        ).left()).bind()
        val z = (callParameters["z"]!!.toIntOrNull()?.right() ?: Pair(
            HttpStatusCode.BadRequest,
            "Z must be a number."
        ).left()).bind()
        val x = (callParameters["x"]!!.toIntOrNull()?.right() ?: Pair(
            HttpStatusCode.BadRequest,
            "X must be a number."
        ).left()).bind()
        val y = (callParameters["y"]!!.toIntOrNull()?.right() ?: Pair(
            HttpStatusCode.BadRequest,
            "Y must be a number."
        ).left()).bind()
        MapTileKey(z, x, y, seed)
    }
}

private fun MapTileKey.validate(): Either<Pair<HttpStatusCode, String>, MapTileKey> {
    return if (this.isValid()) {
        this.right()
    } else {
        val pair: Pair<HttpStatusCode, String> = Pair(
            HttpStatusCode.BadRequest,
            "Invalid tile request: ${this.z}/${this.x}/${this.y}.png"
            )
        pair.left()
    }
}
