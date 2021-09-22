package com.eliottgray.kotlin

import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.math.*

fun main(args: Array<String>) = runBlocking() {
    val timeMillis = measureTimeMillis {
        val seed = Random.nextDouble()

        // Testing writing H3-derived results.
//        val writer = H3Writer(h3Depth=4, seed=seed)
//        writer.collectAndWrite("test_out.csv")

        // The client would be requesting map tiles. For now, lets write them to disk.
        collectAndWrite(seed)
    }
    print(timeMillis)
}

suspend fun collectAndWrite(seed: Double) = coroutineScope {

    val deferredResults: ArrayList<Deferred<MapTile>> = ArrayList()

    // Get everything but the top tile async.
    val maxDepth = 3
    for (z in 1..maxDepth) {
        val rowColCount = 2.0.pow(z).toInt()
        for (x in 0 until rowColCount){
            for (y in 0 until rowColCount){
                val result = async (Dispatchers.Default) {
                    MapTile(z, x, y, seed=seed)
                }
               deferredResults.add(result)
            }
        }
    }

    val topTile = MapTile(0, 0, 0, seed=seed)
    topTile.writePNG()
    launch (Dispatchers.IO) {
        for (mapTile in deferredResults.awaitAll()) {
            mapTile.writePNG(topTile)
        }
    }
}