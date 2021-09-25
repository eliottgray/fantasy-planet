package com.eliottgray.kotlin
import kotlinx.coroutines.*
import kotlin.math.pow

class MapTileWriter(val tileDepth: Int, val seed: Double = Defaults.SEED) {

    suspend fun collectAndWrite(seed: Double) = coroutineScope {

        val planet = Planet(seed)
        val deferredResults: ArrayList<Deferred<MapTile>> = ArrayList()

        // Top tile is required first, to ensure consistent coloring of all other tiles.
        val topTile = MapTile(0, 0, 0, planet)
        topTile.writePNG()

        // Get everything but the top tile async.
        for (z in 1..tileDepth) {
            val rowColCount = 2.0.pow(z).toInt()
            for (x in 0 until rowColCount) {
                for (y in 0 until rowColCount) {
                    val result = async(Dispatchers.Default) {
                        MapTile(z, x, y, planet)
                    }
                    deferredResults.add(result)
                }
            }
        }

        launch(Dispatchers.IO) {
            for (mapTile in deferredResults.awaitAll()) {
                mapTile.writePNG()
            }
        }
    }
}