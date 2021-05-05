package com.eliottgray.kotlin

fun main(args: Array<String>) {
    val writer = H3Writer(h3Depth=3)
    writer.write("test_geojson_out.json")
}