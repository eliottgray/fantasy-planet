package com.eliottgray.kotlin

import java.awt.Transparency
import java.awt.image.*
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.*


class MapTile (val zTile: Int, val xTile: Int, val yTile: Int, val seed: Double = Defaults.SEED) {

    private val sortedPoints = generate().sortedWith(compareBy( {-it.lat}, {it.lon}))
    private val maxElev: Double = this.sortedPoints.maxByOrNull { it.alt }?.alt ?: 0.0
    private val minElev: Double = this.sortedPoints.minByOrNull { it.alt }?.alt ?: 0.0

    companion object {
        const val MAP_TILE_WIDTH_PIXELS = 256
        const val MAP_TILE_HEIGHT_PIXELS = 256

        private fun xyzToNWCorner(z: Int, x: Int, y: Int): Pair<Double, Double> {
            // Taken from OSM docs on xyz/latLon interchange: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
            val n = 2.0.pow(z)
            val nwLon = x / n * 360.0 - 180.0
            val nwLatRad =  atan(sinh(PI * (1 - 2 * y / n)))
            val nwLat = nwLatRad * (180/ PI) // TODO: use built-in rad->degree conversion rather than PI over 180?
            return Pair(nwLon, nwLat)  // TODO: Create a data class to avoid ordering ambiguity.
        }
    }

    fun generate(): ArrayList<Point> {
        val nwCorner = nwCorner()
        val seCorner = seCorner()
        val lonDelta = (seCorner.first - nwCorner.first) / MAP_TILE_WIDTH_PIXELS
        val latDelta = (nwCorner.second - seCorner.second) / MAP_TILE_HEIGHT_PIXELS

        val allPoints = ArrayList<Point>()
        var currentLat = nwCorner.second
        for (xPixel in 1..MAP_TILE_WIDTH_PIXELS){
            var currentLon = nwCorner.first
            for (yPixel in 1..MAP_TILE_HEIGHT_PIXELS){
                val point = Point.fromSpherical(lat=currentLat, lon=currentLon)
                allPoints.add(point)
                currentLon += lonDelta
            }
            currentLat -= latDelta
        }

        assert(allPoints.size == MAP_TILE_HEIGHT_PIXELS * MAP_TILE_WIDTH_PIXELS)

        // TODO: bounding box of tile becomes class properties, reference them instead of Pair.first, Pair.second.
        // TODO: Use N or S side of tile, depending on which is closer to the poles.
        val first = Point.fromSpherical(lon = nwCorner.first, lat = seCorner.second)
        val second = Point.fromSpherical(lon = seCorner.first, lat = seCorner.second)
        val widthOfPixelMeters = first.distance(second)

        val planet = Planet(seed=seed, resolution = ceil(widthOfPixelMeters * 0.6).toInt())
        return planet.getMultipleElevations(allPoints)
    }

    suspend fun writePNG(topTile: MapTile = this) {
        val oldRange = topTile.maxElev - topTile.minElev
        val newRange = 255

        val aByteArray: ByteArray = ByteArray(3 * this.sortedPoints.size)
        for (i in 0 until this.sortedPoints.size){
            val point = this.sortedPoints[0]
            val newValue = (((point.alt - topTile.minElev) * newRange) / oldRange) -128
            val byteIndex = 3 * i
            if (newValue < 0) {
                aByteArray[byteIndex + 0] = 0.toByte()
                aByteArray[byteIndex + 1] = 0.toByte()
                aByteArray[byteIndex + 2] = (-newValue.toInt()).toByte()
            } else {
                aByteArray[byteIndex + 0] = newValue.toInt().toByte()
                aByteArray[byteIndex + 1] = 0.toByte()
                aByteArray[byteIndex + 2] = 0.toByte()
            }
        }

        val buffer: DataBuffer = DataBufferByte(aByteArray, aByteArray.size)

        // 3 bytes per pixel: red, green, blue
        val raster = Raster.createInterleavedRaster(
            buffer,
            MAP_TILE_WIDTH_PIXELS,
            MAP_TILE_HEIGHT_PIXELS,
            3 * MAP_TILE_WIDTH_PIXELS,
            3,
            intArrayOf(0, 1, 2),
            null as java.awt.Point?
        )
        val cm: ColorModel = ComponentColorModel(
            ColorModel.getRGBdefault().colorSpace,
            false,
            true,
            Transparency.OPAQUE,
            DataBuffer.TYPE_BYTE
        )
        val image = BufferedImage(cm, raster, true, null)

        val path = "web/tiles/$zTile/$xTile/$yTile.png"
        val dir = File("web/tiles/$zTile/$xTile/")
        if (!dir.exists()){
            dir.mkdirs()
        }
        try {
            ImageIO.write(image, "png", File(path))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun nwCorner(): Pair<Double, Double> {
        return xyzToNWCorner(zTile, xTile, yTile)
    }

    fun seCorner(): Pair<Double, Double> {
        return xyzToNWCorner(zTile, xTile + 1, yTile + 1)
    }
}