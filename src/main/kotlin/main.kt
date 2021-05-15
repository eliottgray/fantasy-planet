package com.eliottgray.kotlin

import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) = runBlocking() {
    val timeMillis = measureTimeMillis {
        val writer = H3Writer(h3Depth=4, seed=0.33234034)
        writer.collectAndWrite("test_out.csv")
    }
    print(timeMillis)
}