package com.eliottgray.kotlin

import java.awt.Transparency
import java.awt.image.*
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO


class MapTile (val zTile: Int, val xTile: Int, val yTile: Int, val seed: Double = Defaults.SEED) {

    private val sortedPoints = generate().sortedWith(compareBy({it.lon}, {-it.lat}))

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
        var currentLon = nwCorner.first + (lonDelta / 2.0)  // Lets derive tile centers, to avoid the poles/IDL.
        while (currentLon < seCorner.first) {
            var currentLat = seCorner.second + (latDelta / 2.0) // Lets derive tile centers, to avoid the poles/IDL
            while (currentLat < nwCorner.second) {
                val point = Point.fromSpherical(lat=currentLat, lon=currentLon)
                allPoints.add(point)
                currentLat += latDelta
            }
            currentLon += lonDelta
        }

        // TODO: Determine resolution dynamically depending on the characteristics of the tile (Web Mercator).
        val planet = Planet(seed=seed, resolution = 34000)
        return planet.getMultipleElevations(allPoints)
    }

    fun writePNG(path: String) {
        val maxElev: Double = this.sortedPoints.maxByOrNull { it.alt }?.alt ?: 0.0
        val minElev: Double = this.sortedPoints.minByOrNull { it.alt }?.alt ?: 0.0

        val oldRange = maxElev - minElev
        val newRange = 255
        /*
        OldRange = (OldMax - OldMin)
        NewRange = (NewMax - NewMin)
        NewValue = (((OldValue - OldMin) * NewRange) / OldRange) + NewMin
         */

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

        try {
            ImageIO.write(image, "png", File(path))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}