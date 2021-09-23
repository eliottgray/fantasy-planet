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

        private fun xyzToNWCorner(z: Int, x: Int, y: Int): MapTileCorner {
            // Taken from OSM docs on xyz/latLon interchange: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
            val n = 2.0.pow(z)
            val nwLon = x / n * 360.0 - 180.0
            val nwLatRad =  atan(sinh(PI * (1 - 2 * y / n)))
            val nwLat = nwLatRad * (180/ PI) // TODO: use built-in rad->degree conversion rather than PI over 180?
            return MapTileCorner(latitude = nwLat, longitude = nwLon)
        }
    }

    fun generate(): ArrayList<Point> {
        val nwCorner = nwCorner()
        val seCorner = seCorner()
        val lonDelta = (seCorner.longitude - nwCorner.longitude) / MAP_TILE_WIDTH_PIXELS
        val latDelta = (nwCorner.latitude - seCorner.latitude) / MAP_TILE_HEIGHT_PIXELS

        val allPoints = ArrayList<Point>()
        var currentLat = nwCorner.latitude
        for (xPixel in 1..MAP_TILE_WIDTH_PIXELS){
            var currentLon = nwCorner.longitude
            for (yPixel in 1..MAP_TILE_HEIGHT_PIXELS){
                val point = Point.fromSpherical(lat=currentLat, lon=currentLon)
                allPoints.add(point)
                currentLon += lonDelta
            }
            currentLat -= latDelta
        }

        assert(allPoints.size == MAP_TILE_HEIGHT_PIXELS * MAP_TILE_WIDTH_PIXELS)

        // TODO: Use N or S side of tile, depending on which is closer to the poles.
        val first = Point.fromSpherical(lon = 0.0, lat = seCorner.latitude)
        val second = Point.fromSpherical(lon = lonDelta, lat = seCorner.latitude)
        val widthOfPixelMeters = first.distance(second)

        val planet = Planet(seed=seed, resolution = ceil(widthOfPixelMeters * 0.6).toInt())
        return planet.getMultipleElevations(allPoints)
    }

    suspend fun writePNG(topTile: MapTile = this) {
        val oldRange = topTile.maxElev - topTile.minElev
        val newRange = 255

        val aByteArray: ByteArray = this.sortedPoints.map{ point ->
            val newValue = (((point.alt - topTile.minElev) * newRange) / oldRange) -128
            if (newValue < 0) {
                arrayListOf(0.toByte(), 0.toByte(), (-newValue.toInt()).toByte())
            } else {
                arrayListOf(newValue.toInt().toByte(), 0.toByte(), 0.toByte())
            }
        }.flatten().toByteArray()

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

    fun nwCorner(): MapTileCorner {
        return xyzToNWCorner(zTile, xTile, yTile)
    }

    fun seCorner(): MapTileCorner {
        return xyzToNWCorner(zTile, xTile + 1, yTile + 1)
    }
}