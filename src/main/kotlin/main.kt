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
        val writer = MapTileWriter(tileDepth = 3, seed=seed)
        writer.collectAndWrite(seed)
    }
    print(timeMillis)
}
