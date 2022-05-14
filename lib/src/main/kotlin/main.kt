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

/**
 * Profiling Fantasy-Planet:
 * - The current usage of the planet class is to build map tiles on-demand, by a UI that displays them.
 * - Map tile query follows a specific pattern:
 *   - Viewing a point on a globe, and viewing all map tiles around that point (at varying zoom levels)
 *   - Viewing part (or all) of a map, and viewing all map tiles at the same zoom level within the window.
 *   - Zooming in to a point on a globe, increasing the resolution of the tiles being queried (over a pre-queried area)
 *   - Zooming in to a point on a map, increasing the resolution of the tiles being queried (over a pre-queried area)
 *
 *
 * Given the above, it is necessary to:
 *   - Identify specific patterns of queries to emulate, i.e. what tiles to query in what order, for a given seed
 *   - Automate the above pattern for random seeds
 *   - Collect time taken
 *   - Collect total CPU used
 *   - Collect total memory used
 *
 * Once the above are built, it is possible to collect metrics describing a specific build, and optimize.
 *
 * TODO:
 * 1) Save application logs to file (tile requests are already being logged)
 * 2) Boot application repeatedly, collecting logs for the following use cases, for both globe and map states:
 *    a) Refreshing repeatedly
 *    b) Panning around at a wide zoom.
 *    c) Panning around at an inner zoom.
 *    d) Zooming in deep.
 * 3) Parse logs programmatically to build a set of instructions for a profiling function.
 * 4) Build a function to perform tests defined by above instructions
 */