package com.eliottgray.kotlin

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() = runBlocking {
    // FreeMarker is a Java templating engine.
    install(FreeMarker) {
        val templatesFolder = "templates"
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, templatesFolder)
    }
    val isDemo = environment.config.propertyOrNull("ktor.demo.enabled")?.getString()?.toBooleanStrictOrNull()
        ?: true  // We want to avoid undesired computation by defaulting to demo behavior.
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
            // TODO: Allow user to input this seed, and regenerate the map, rather than needing to hit 'refresh'.
            val randomOrDemoSeed = if (isDemo) demoSeed else Math.random()
            // TODO: Parameterize non-demo max zoom.
            val defaultMaxZoom = 18  // TODO: 20 is OSM lowest; is this a good maximum zoom?
            val maxDepth = if (isDemo) minOf(demoDepth, defaultMaxZoom) else defaultMaxZoom
            val root = mapOf(
                "seed" to randomOrDemoSeed,
                "depth" to maxDepth
            )
            call.respond(FreeMarkerContent("index.ftl", root))
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
