package com.eliottgray.kotlin

import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) = runBlocking() {
    val timeMillis = measureTimeMillis {
        val seed = Random.nextDouble()

        // Testing writing H3-derived results.
//        val writer = H3Writer(h3Depth=4, seed=seed)
//        writer.collectAndWrite("test_out.csv")

        // The client would be requesting map tiles. For now, lets write them to disk.
        MapTile(0, 0, 0, seed=seed).writePNG("foo.png")
    }
    print(timeMillis)
}