package com.eliottgray.kotlin

import kotlinx.coroutines.runBlocking
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

//        val maxDepth = 1
//        for (z in 0..maxDepth) {
//            println(z)
//            val rowColCount = 4.0.pow(z).toInt() - 1
//
//            print(rowColCount)
//            for (x in 0..rowColCount){
//                for (y in 0..rowColCount){
////                    println("$z $x $y")
//                    MapTile(z, x, y, seed=seed).writePNG()
//                }
//            }
//        }
        MapTile(0, 0, 0, seed=seed).writePNG()
        MapTile(1, 0, 0, seed=seed).writePNG()
        MapTile(1, 0, 1, seed=seed).writePNG()
        MapTile(1, 1, 0, seed=seed).writePNG()
        MapTile(1, 1, 1, seed=seed).writePNG()
        MapTile(2, 0, 0, seed=seed).writePNG()
        MapTile(2, 0, 1, seed=seed).writePNG()
        MapTile(2, 0, 2, seed=seed).writePNG()
        MapTile(2, 0, 3, seed=seed).writePNG()
        MapTile(2, 1, 0, seed=seed).writePNG()
        MapTile(2, 1, 1, seed=seed).writePNG()
        MapTile(2, 1, 2, seed=seed).writePNG()
        MapTile(2, 1, 3, seed=seed).writePNG()
        MapTile(2, 2, 0, seed=seed).writePNG()
        MapTile(2, 2, 1, seed=seed).writePNG()
        MapTile(2, 2, 2, seed=seed).writePNG()
        MapTile(2, 2, 3, seed=seed).writePNG()
        MapTile(2, 3, 0, seed=seed).writePNG()
        MapTile(2, 3, 1, seed=seed).writePNG()
        MapTile(2, 3, 2, seed=seed).writePNG()
        MapTile(2, 3, 3, seed=seed).writePNG()
        MapTile(3, 0, 0, seed=seed).writePNG()
        MapTile(3, 0, 1, seed=seed).writePNG()
        MapTile(3, 0, 2, seed=seed).writePNG()
        MapTile(3, 0, 3, seed=seed).writePNG()
        MapTile(3, 0, 4, seed=seed).writePNG()
        MapTile(3, 0, 5, seed=seed).writePNG()
        MapTile(3, 0, 6, seed=seed).writePNG()
        MapTile(3, 0, 7, seed=seed).writePNG()
        MapTile(3, 1, 0, seed=seed).writePNG()
        MapTile(3, 1, 1, seed=seed).writePNG()
        MapTile(3, 1, 2, seed=seed).writePNG()
        MapTile(3, 1, 3, seed=seed).writePNG()
        MapTile(3, 1, 4, seed=seed).writePNG()
        MapTile(3, 1, 5, seed=seed).writePNG()
        MapTile(3, 1, 6, seed=seed).writePNG()
        MapTile(3, 1, 7, seed=seed).writePNG()
        MapTile(3, 2, 0, seed=seed).writePNG()
        MapTile(3, 2, 1, seed=seed).writePNG()
        MapTile(3, 2, 2, seed=seed).writePNG()
        MapTile(3, 2, 3, seed=seed).writePNG()
        MapTile(3, 2, 4, seed=seed).writePNG()
        MapTile(3, 2, 5, seed=seed).writePNG()
        MapTile(3, 2, 6, seed=seed).writePNG()
        MapTile(3, 2, 7, seed=seed).writePNG()
        MapTile(3, 3, 0, seed=seed).writePNG()
        MapTile(3, 3, 1, seed=seed).writePNG()
        MapTile(3, 3, 2, seed=seed).writePNG()
        MapTile(3, 3, 3, seed=seed).writePNG()
        MapTile(3, 3, 4, seed=seed).writePNG()
        MapTile(3, 3, 5, seed=seed).writePNG()
        MapTile(3, 3, 6, seed=seed).writePNG()
        MapTile(3, 3, 7, seed=seed).writePNG()
        MapTile(3, 4, 0, seed=seed).writePNG()
        MapTile(3, 4, 1, seed=seed).writePNG()
        MapTile(3, 4, 2, seed=seed).writePNG()
        MapTile(3, 4, 3, seed=seed).writePNG()
        MapTile(3, 4, 4, seed=seed).writePNG()
        MapTile(3, 4, 5, seed=seed).writePNG()
        MapTile(3, 4, 6, seed=seed).writePNG()
        MapTile(3, 4, 7, seed=seed).writePNG()
        MapTile(3, 5, 0, seed=seed).writePNG()
        MapTile(3, 5, 1, seed=seed).writePNG()
        MapTile(3, 5, 2, seed=seed).writePNG()
        MapTile(3, 5, 3, seed=seed).writePNG()
        MapTile(3, 5, 4, seed=seed).writePNG()
        MapTile(3, 5, 5, seed=seed).writePNG()
        MapTile(3, 5, 6, seed=seed).writePNG()
        MapTile(3, 5, 7, seed=seed).writePNG()
        MapTile(3, 6, 0, seed=seed).writePNG()
        MapTile(3, 6, 1, seed=seed).writePNG()
        MapTile(3, 6, 2, seed=seed).writePNG()
        MapTile(3, 6, 3, seed=seed).writePNG()
        MapTile(3, 6, 4, seed=seed).writePNG()
        MapTile(3, 6, 5, seed=seed).writePNG()
        MapTile(3, 6, 6, seed=seed).writePNG()
        MapTile(3, 6, 7, seed=seed).writePNG()
        MapTile(3, 7, 0, seed=seed).writePNG()
        MapTile(3, 7, 1, seed=seed).writePNG()
        MapTile(3, 7, 2, seed=seed).writePNG()
        MapTile(3, 7, 3, seed=seed).writePNG()
        MapTile(3, 7, 4, seed=seed).writePNG()
        MapTile(3, 7, 5, seed=seed).writePNG()
        MapTile(3, 7, 6, seed=seed).writePNG()
        MapTile(3, 7, 7, seed=seed).writePNG()
    }
    print(timeMillis)
}