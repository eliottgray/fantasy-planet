package com.eliottgray.kotlin.profiling

import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.MapTileKey

data class TestCase(val name: String, val mapTileKeyList: List<MapTileKey>, val seed: Double) {


    companion object {

        fun fromFile(resourcePath: String, seed: Double = Defaults.SEED): TestCase {
            val resource = javaClass.getResource(resourcePath)
            val mapTileKeyList = resource?.readText()?.split("\n")?.map {
                val split = it.trim().split(',')
                val z = split[0].toInt()
                val x = split[1].toInt()
                val y = split[2].toInt()
                MapTileKey(z, x, y, seed)
            }
            val name = resource?.path?.split("/")?.last()
            return TestCase(name!!, mapTileKeyList!!, seed)
        }
    }

}