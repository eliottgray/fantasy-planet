package com.eliottgray.kotlin

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
        ?: true  // We want to default to demo behavior.
    if (isDemo) {
        log.info("Demo mode initializing.")
        val depth = environment.config.propertyOrNull("ktor.demo.depth")?.getString()?.toIntOrNull() ?: -1
        val demoSeed = 0.12345
        val writer = MapTileWriter(depth, demoSeed)
        // TODO: Don't block demo page while files are being written to disk.
        writer.collectAndWrite(demoSeed)
        log.info("Demo mode initialization complete.")
    }

    routing {
        get("/tiles/{seed}/{z}/{x}/{y}.png") {
            // TODO: Validate these params.
            val seed = call.parameters["seed"]!!.toDouble()
            val z = call.parameters["z"]!!.toInt()
            val x = call.parameters["x"]!!.toInt()
            val y = call.parameters["y"]!!.toInt()
            call.application.environment.log.debug("Requesting tile $z/$x/$y.png")
            if (isDemo) {
                val tileFile = File("web/tiles/$z/$x/$y.png")
                if (tileFile.exists()){
                    call.respondFile(tileFile)
                } else {
                    val errorMessage = "Demo tile not found: $z/$x/$y.png"
                    log.error(errorMessage)
                    call.respond(HttpStatusCode.NotFound, errorMessage)
                }
            } else {
                val mapTileKey = MapTileKey(z, x, y, seed)
                val mapTile = MapTileCache.getTile(mapTileKey)
                call.respondBytes(mapTile.pngByteArray, ContentType.Image.PNG, HttpStatusCode.OK)
            }
        }

        get("/") {
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
var seed = Math.random()
var maxDepth = 20;  // TODO: 20 is OSM lowest, choose based on size per pixel per zoom level.

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
