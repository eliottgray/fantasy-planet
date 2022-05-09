package com.eliottgray.kotlin

import com.eliottgray.kotlin.planet.Planet
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() = runBlocking() {
    val timeMillis = measureTimeMillis {
        val seed = Random.nextDouble()

        // Testing writing H3-derived results.
        val h3Writer = H3Writer(h3Depth=1, seed=seed)
        h3Writer.collectAndWrite("test_out.csv")

        // Testing by writing map tiles to disk.
        val planet = Planet.get(seed)
        val mapTileWriter = MapTileWriter(tileDepth = 2, planet = planet)
        mapTileWriter.collectAndWrite()
    }
    print(timeMillis)
}
