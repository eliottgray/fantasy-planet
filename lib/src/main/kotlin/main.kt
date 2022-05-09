package com.eliottgray.kotlin

import com.eliottgray.kotlin.planet.FractalPlanet
import com.eliottgray.kotlin.planet.HexPlanet
import com.eliottgray.kotlin.planet.Planet
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() = runBlocking() {
    val seed = Random.nextDouble()
    val fractalPlanet = FractalPlanet.get(seed)
    val h3Resolution = 5
    val hexPlanet = HexPlanet.get(seed, h3Resolution)
    val tileDepth = 4

    // Testing writing H3-derived results.
    val h3Writer = H3Writer(h3Depth=1, planet=fractalPlanet)
    h3Writer.collectAndWrite("test_out.csv")

    val hexTimeMillis = measureTileWriteTime(hexPlanet, tileDepth)
    println("Hex: $hexTimeMillis")

    val fractalTimeMillis = measureTileWriteTime(fractalPlanet, tileDepth)
    println("Fractal: $fractalTimeMillis")
}

suspend fun measureTileWriteTime(planet: Planet, tileDepth: Int): Long = coroutineScope{
    measureTimeMillis {
        val mapTileWriter = MapTileWriter(tileDepth = tileDepth, planet = planet)
        mapTileWriter.collectAndWrite()
    }
}
