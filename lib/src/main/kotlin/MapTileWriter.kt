package com.eliottgray.kotlin
import com.eliottgray.kotlin.planet.Planet
import kotlinx.coroutines.*
import java.lang.RuntimeException
import kotlin.math.pow

class MapTileWriter(val tileDepth: Int, val planet: Planet) {

    init {
        if (DEPTH_MINIMUM > tileDepth || tileDepth > DEPTH_MAXIMUM) {
            throw RuntimeException("Tile depth between $DEPTH_MINIMUM and $DEPTH_MAXIMUM expected, but was $tileDepth.")
        }
    }

    private companion object {
        private const val DEPTH_MINIMUM = 0
        private const val DEPTH_MAXIMUM = 20
    }

    suspend fun collectAndWrite() = coroutineScope {

        val deferredResults: ArrayList<Deferred<MapTile>> = ArrayList()

        // Get everything but the top level async.
        for (z in 0..tileDepth) {
            val rowColCount = 2.0.pow(z).toInt()
            for (x in 0 until rowColCount * 2) {
                for (y in 0 until rowColCount) {
                    val result = async(Dispatchers.Default) {
                        planet.getMapTile(MapTileKey(z, x, y, planet.seed))
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