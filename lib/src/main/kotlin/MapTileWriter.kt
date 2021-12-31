package com.eliottgray.kotlin
import kotlinx.coroutines.*
import java.lang.RuntimeException
import kotlin.math.pow

class MapTileWriter(val tileDepth: Int, val seed: Double = Defaults.SEED) {

    init {
        if (DEPTH_MINIMUM > tileDepth || tileDepth > DEPTH_MAXIMUM) {
            throw RuntimeException("Tile depth between $DEPTH_MINIMUM and $DEPTH_MAXIMUM expected, but was $tileDepth.")
        }
    }

    private companion object {
        private const val DEPTH_MINIMUM = 0
        private const val DEPTH_MAXIMUM = 20
    }

    suspend fun collectAndWrite(seed: Double) = coroutineScope {

        val planet = Planet.get(seed)
        val deferredResults: ArrayList<Deferred<MapTile>> = ArrayList()

        // Top tiles are required first, to ensure consistent coloring of all other tiles.
        val topTile = MapTile(0, 0, 0, planet)
        val topTile2 = MapTile(0, 1, 0, planet)
        val tileElevations = MapTileElevations.fromTopTiles(topTile, topTile2)

        topTile.writePNG()
        topTile2.writePNG()

        // Get everything but the top level async.
        for (z in 1..tileDepth) {
            val rowColCount = 2.0.pow(z).toInt()
            for (x in 0 until rowColCount * 2) {
                for (y in 0 until rowColCount) {
                    val result = async(Dispatchers.Default) {
                        MapTile(z, x, y, planet, tileElevations)
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