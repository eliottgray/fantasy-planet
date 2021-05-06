package com.eliottgray.kotlin

fun main(args: Array<String>) {
    val writer = H3Writer(h3Depth=4, seed=0.33234034)
    writer.collectAndWrite("test_geojson_out.json")
}