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
import kotlin.properties.Delegates

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

object Config {
    var isDemo by Delegates.notNull<Boolean>()
    var demoSeed by Delegates.notNull<Double>()
}

@Suppress("unused")
fun Application.module() = runBlocking {
    // FreeMarker is a Java templating engine.
    install(FreeMarker) {
        val templatesFolder = "templates"
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, templatesFolder)
    }
    // TODO: Validate config parameters, and terminate early if invalid.
    Config.isDemo = environment.config.propertyOrNull("ktor.demo.enabled")?.getString()?.toBooleanStrictOrNull()
        ?: true  // We want to avoid undesired computation by defaulting to demo behavior.
    Config.demoSeed = environment.config.propertyOrNull("ktor.demo.seed")?.getString()?.toDoubleOrNull() ?: 0.12345

    routing {
        get("/tiles/{seed}/{z}/{x}/{y}.png") {
            either <Pair<HttpStatusCode, String>, Unit> {
                val mapTileKey = buildMapTileKey(call.parameters).bind()
                val keyPath = "${mapTileKey.z}/${mapTileKey.x}/${mapTileKey.y}"
                log.debug("Requesting tile ${mapTileKey.seed}/$keyPath.png")
                val mapTile = MapTileCache.getTile(mapTileKey)
                call.respondBytes(mapTile.pngByteArray, ContentType.Image.PNG, HttpStatusCode.OK)
            }.mapLeft { errorPair ->
                log.error(errorPair.second)
                call.respond(errorPair.first, errorPair.second)
            }
        }

        get("/") {
            // TODO: Allow user to input this seed, and regenerate the map, rather than needing to hit 'refresh'.
            val randomOrDemoSeed = if (Config.isDemo) Config.demoSeed.toString() else Math.random().toString()
            val maxDepth = environment.config.propertyOrNull("ktor.max_depth")?.getString()?.toInt()
            val root = mapOf(
                "seed" to randomOrDemoSeed,
                "depth" to maxDepth
            )
            call.respond(FreeMarkerContent("index.ftl", root))
        }
    }
}

// TODO: Refactor planet to serve map tiles, rather than directly accessing MapTileCache.
private suspend fun getPlanet(callParameters: Parameters): Either<Pair<HttpStatusCode, String>, Planet> {
    return either {
        val seed = (callParameters["seed"]!!.toDoubleOrNull()?.right() ?: Pair(
            HttpStatusCode.BadRequest,
            "Seed must be a number."
        ).left()).bind()
        Planet.get(seed)
    }
}

private suspend fun buildMapTileKey(callParameters: Parameters): Either<Pair<HttpStatusCode, String>, MapTileKey> {
    return either {
        val seed = (callParameters["seed"]!!.toDoubleOrNull()?.right() ?: Pair(
            HttpStatusCode.BadRequest,
            "Seed must be a number."
        ).left()).flatMap { seed ->
            if (Config.isDemo && seed != Config.demoSeed) {
                Pair(
                    HttpStatusCode.BadRequest,
                    "Demo mode is active, but seed was not demo seed."
                ).left()
            } else {
                seed.right()
            }
        }.bind()
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
        MapTileKey(z, x, y, seed).validate().bind()
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
