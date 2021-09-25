package com.eliottgray.kotlin

class ColorMap {

    companion object {
        // TODO: Use a config file instead, and pass the colors to use in from the client.
        private val ERROR_COLOR = listOf(255.toByte(), 0.toByte(), 0.toByte())

        // Ranges taken from "Lefevbre2" color map of Planet Generator: http://hjemmesider.diku.dk/~torbenm/Planet/
        private val COLOR_RANGES: HashMap<Int, List<Byte>> = hashMapOf(
            1 to rgbToByteList(0, 53, 83),
            85 to rgbToByteList(5, 70, 107,),
            100 to rgbToByteList(17, 85, 124),
            120 to rgbToByteList(104, 176, 196),
            125 to rgbToByteList(179, 214, 224),
            126 to rgbToByteList( 8, 68, 34),
            155 to rgbToByteList( 50, 101, 50),
            185 to rgbToByteList( 118, 141, 69),
            205 to rgbToByteList( 165, 184, 105),
            230 to rgbToByteList( 205, 207, 162),
            245 to rgbToByteList( 235, 243, 248),
            250 to rgbToByteList( 255, 255, 255)
        )
        
        private fun rgbToByteList(r: Int, g: Int, b: Int): List<Byte> {
            return listOf(r.toByte(), g.toByte(), b.toByte())
        }

        private fun mapRangesToColorValues(): HashMap<Int, List<Byte>> {
            val min = COLOR_RANGES.keys.minByOrNull { it }!!
            val max = COLOR_RANGES.keys.maxByOrNull { it }!!
            assert(min > 0)
            assert(max > min)

            var colorKey = min
            var color = COLOR_RANGES[min]!!
            val hashMap = HashMap<Int, List<Byte>>()

            while (colorKey <= max) {
                val newColor = COLOR_RANGES[colorKey]
                if (newColor != null) {
                    color = newColor
                }
                hashMap[colorKey] = color
                colorKey++
            }
            return hashMap
        }

        private val COLOR_MAP = mapRangesToColorValues()

        private val MIN_ELEVATION = COLOR_RANGES.keys.minByOrNull { it }!!
        private val MAX_ELEVATION = COLOR_RANGES.keys.maxByOrNull { it }!!
        val ELEVATION_RANGE = MAX_ELEVATION - MIN_ELEVATION

        fun getColorForElevation(elevation: Int): List<Byte> {
            val validatedElev = elevation.let {
                when {
                    it < MIN_ELEVATION -> {
                        MIN_ELEVATION
                    }
                    it > MAX_ELEVATION -> {
                        MAX_ELEVATION
                    }
                    else -> {
                        it
                    }
                }
            }
            return COLOR_MAP.getOrDefault(validatedElev, ERROR_COLOR)
        }
    }
}