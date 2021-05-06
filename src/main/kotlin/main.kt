package com.eliottgray.kotlin

import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val timeMillis = measureTimeMillis {
        val writer = H3Writer(h3Depth=3, seed=0.33234034)
        writer.collectAndWrite("test_geojson_out.jsonl")
    }
    print(timeMillis)
}