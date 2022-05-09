package com.eliottgray.kotlin

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.eliottgray.kotlin.planet.Planet
import com.eliottgray.kotlin.planet.HexPlanet
import com.eliottgray.kotlin.planet.FractalPlanet
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
    var demoEnabled by Delegates.notNull<Boolean>()
    var demoSeed by Delegates.notNull<Double>()
    var hexEnabled by Delegates.notNull<Boolean>()
    var hexResolution by Delegates.notNull<Int>()
}

@Suppress("unused")
fun Application.module() = runBlocking {
    // FreeMarker is a Java templating engine.
    install(FreeMarker) {
        val templatesFolder = "templates"
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, templatesFolder)
    }
    // TODO: Validate config parameters, and terminate early if invalid.
    Config.demoEnabled = environment.config.propertyOrNull("ktor.demo.enabled")?.getString()?.toBooleanStrictOrNull()
        ?: true  // We want to avoid undesired computation by defaulting to demo behavior.
    Config.demoSeed = environment.config.propertyOrNull("ktor.demo.seed")?.getString()?.toDoubleOrNull() ?: 0.12345
    Config.hexEnabled = environment.config.propertyOrNull("ktor.hex.enabled")?.getString()?.toBooleanStrictOrNull() ?: false
    Config.hexResolution = environment.config.propertyOrNull("ktor.hex.resolution")?.getString()?.toIntOrNull() ?: 0
    log.info("Demo enabled: ${Config.demoEnabled}")
    log.info("Demo seed: ${Config.demoSeed}")
    log.info("Hex enabled: ${Config.hexEnabled}")
    log.info("Hex resolution: ${Config.hexResolution}")

    routing {
        get("/tiles/{seed}/{z}/{x}/{y}.png") {
            either <Pair<HttpStatusCode, String>, Unit> {
                val mapTileKey = buildMapTileKey(call.parameters).bind()
                log.debug("Requesting tile ${mapTileKey.seed}/${mapTileKey.z}/${mapTileKey.x}/${mapTileKey.y}.png")
                val planet = getPlanet(call.parameters).bind()
                val mapTile = planet.getMapTile(mapTileKey)
                call.respondBytes(mapTile.pngByteArray, ContentType.Image.PNG, HttpStatusCode.OK)
            }.mapLeft { errorPair ->
                log.error(errorPair.second)
                call.respond(errorPair.first, errorPair.second)
            }
        }

        get("/") {
            // TODO: Allow user to input this seed, and regenerate the map, rather than needing to hit 'refresh'.
            val randomOrDemoSeed = if (Config.demoEnabled) Config.demoSeed.toString() else Math.random().toString()
            val maxDepth = environment.config.propertyOrNull("ktor.max_depth")?.getString()?.toInt()
            val root = mapOf(
                "seed" to randomOrDemoSeed,
                "depth" to maxDepth
            )
            call.respond(FreeMarkerContent("index.ftl", root))
        }
    }
}

private suspend fun getPlanet(callParameters: Parameters): Either<Pair<HttpStatusCode, String>, Planet> {
    return either {
        val seed = (callParameters["seed"]!!.toDoubleOrNull()?.right() ?: Pair(
            HttpStatusCode.BadRequest,
            "Seed must be a number."
        ).left()).bind()
        if (Config.hexEnabled) {
            HexPlanet.get(seed, Config.hexResolution)
        } else {
            FractalPlanet.get(seed)
        }
    }
}

private suspend fun buildMapTileKey(callParameters: Parameters): Either<Pair<HttpStatusCode, String>, MapTileKey> {
    return either {
        val seed = (callParameters["seed"]!!.toDoubleOrNull()?.right() ?: Pair(
            HttpStatusCode.BadRequest,
            "Seed must be a number."
        ).left()).flatMap { seed ->
            if (Config.demoEnabled && seed != Config.demoSeed) {
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
