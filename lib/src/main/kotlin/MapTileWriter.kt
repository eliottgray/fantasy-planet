package com.eliottgray.kotlin
import kotlinx.coroutines.*
import kotlin.math.pow

class MapTileWriter(val tileDepth: Int, val seed: Double = Defaults.SEED) {

    suspend fun collectAndWrite(seed: Double) = coroutineScope {

        val planet = Planet(seed)
        val deferredResults: ArrayList<Deferred<MapTile>> = ArrayList()

        // Top tile is required first, to ensure consistent coloring of all other tiles.
        // TODO: Determine the elevation range using BOTH tiles.
        val topTile = MapTile(0, 0, 0, planet)
        val topTile2 = MapTile(0, 1, 0, planet)

        topTile.writePNG()
        topTile2.writePNG()

        // Get everything but the top level async.
        for (z in 1..tileDepth) {
            val rowColCount = 2.0.pow(z).toInt()
            for (x in 0 until rowColCount * 2) {
                for (y in 0 until rowColCount) {
                    val result = async(Dispatchers.Default) {
                        MapTile(z, x, y, planet, maxElev = topTile.maxElev, minElev = topTile.minElev)
                    }
                    deferredResults.add(result)
                }
            }
        }

        launch(Dispatchers.IO) {
            for (deferred in deferredResults) {
                deferred.await().writePNG()
            }
        }
    }
}