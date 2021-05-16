package com.eliottgray.kotlin

import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) = runBlocking() {
    val timeMillis = measureTimeMillis {
        val seed = Random.nextDouble()
        val writer = H3Writer(h3Depth=5, seed=seed)
        writer.collectAndWrite("test_out.csv")
    }
    print(timeMillis)
}