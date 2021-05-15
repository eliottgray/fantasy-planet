package com.eliottgray.kotlin

import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val timeMillis = measureTimeMillis {
        val seed = Random.nextDouble()
        val writer = H3Writer(h3Depth=3, seed=seed)
        writer.collectAndWrite("test_out.csv")
    }
    print(timeMillis)
}