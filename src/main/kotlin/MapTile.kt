package com.eliottgray.kotlin

import java.awt.Transparency
import java.awt.image.*
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO


class MapTile (val zTile: Int, val xTile: Int, val yTile: Int, val seed: Double = Defaults.SEED) {

    private val sortedPoints = generate().sortedWith(compareBy( {-it.lat}, {it.lon}))

    companion object {
        const val MAP_TILE_WIDTH_PIXELS = 256
        const val MAP_TILE_HEIGHT_PIXELS = 256
    }

    fun generate(): ArrayList<Point> {
        val nwCorner = xyzToNWCorner(this.zTile, this.xTile, this.yTile)
        val seCorner = xyzToNWCorner(this.zTile, this.xTile + 1, this.yTile + 1)
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

        // TODO: Determine resolution dynamically depending on the characteristics of the tile (Web Mercator).
        val planet = Planet(seed=seed, resolution = 34000)
        return planet.getMultipleElevations(allPoints)
    }

    fun writePNG() {
        val maxElev: Double = this.sortedPoints.maxByOrNull { it.alt }?.alt ?: 0.0
        val minElev: Double = this.sortedPoints.minByOrNull { it.alt }?.alt ?: 0.0

        val oldRange = maxElev - minElev
        val newRange = 255

        val aByteArray: ByteArray = this.sortedPoints.map{ point ->
            val newValue = (((point.alt - minElev) * newRange) / oldRange) -128
            if (newValue < 0) {
                arrayListOf(0.toByte(), 0.toByte(), (-newValue.toInt()).toByte())
            } else {
                arrayListOf(newValue.toInt().toByte(), 0.toByte(), 0.toByte())
            }
        }.flatten().toByteArray()

        val buffer: DataBuffer = DataBufferByte(aByteArray, aByteArray.size)

        // 3 bytes per pixel: red, green, blue

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

}