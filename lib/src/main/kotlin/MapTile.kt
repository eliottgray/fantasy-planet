package com.eliottgray.kotlin

import org.slf4j.LoggerFactory
import java.awt.Transparency
import java.awt.image.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.*


class MapTile (
    private val zTile: Int,
    private val xTile: Int,
    private val yTile: Int,
    unsortedPoints: MutableList<Point>
) {
    constructor(mapTileKey: MapTileKey, unsortedPoints: MutableList<Point>) : this(mapTileKey.z, mapTileKey.x, mapTileKey.y,
        unsortedPoints)

    val pngByteArray: ByteArray
    val maxElev: Double
    val minElev: Double

    init {
        val sortedPoints = unsortedPoints.sortedWith(compareBy( {-it.lat}, {it.lon}))
        // TODO: Avoid calculating max and min elevation, since they are not currently used beyond profiling tile stats.
        this.maxElev = sortedPoints.maxOfOrNull { it.alt }!!
        this.minElev = sortedPoints.minOfOrNull { it.alt }!!
        pngByteArray = writePNGBytes(sortedPoints)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MapTile::class.java)
        const val TILE_SIZE = 256

        private fun haversineDistanceMeters(
            coordinateOne: MapTileCoordinate,
            coordinateTwo: MapTileCoordinate,
        ): Double {
            var lat1 = coordinateOne.latitude
            var lat2 = coordinateTwo.latitude
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(coordinateTwo.longitude - coordinateOne.longitude)

            // convert to radians
            lat1 = Math.toRadians(lat1)
            lat2 = Math.toRadians(lat2)

            // apply formulae
            val a = sin(dLat / 2).pow(2.0) +
                    sin(dLon / 2).pow(2.0) *
                    cos(lat1) *
                    cos(lat2)
            val rad = 6371000.0
            val c = 2 * asin(sqrt(a))
            return rad * c
        }

        fun longitudinalWidthOfPixelMeters(pixelLat: Double, pixelLongitudeWidth: Double): Double {
            return haversineDistanceMeters(
                MapTileCoordinate(pixelLat, 0.0),
                MapTileCoordinate(pixelLat, pixelLongitudeWidth)
            )
        }
    }

    private fun toBufferedImage(sortedPoints: List<Point>): BufferedImage {
        val oldRange = Defaults.MAXIMUM_ALTITUDE_METERS - Defaults.MINIMUM_ALTITUDE_METERS
        // TODO: ColorMap should be a param for the map tile.
        val newRange = ColorMap.ELEVATION_RANGE

        val aByteArray: ByteArray = sortedPoints.map{ point ->
            val newValue = (((point.alt - Defaults.MINIMUM_ALTITUDE_METERS) * newRange) / oldRange)
            ColorMap.getColorForElevation(newValue.toInt())
        }.flatten().toByteArray()

        val buffer: DataBuffer = DataBufferByte(aByteArray, aByteArray.size)

        // 3 bytes per pixel: red, green, blue
        val raster = Raster.createInterleavedRaster(
            buffer,
            TILE_SIZE,
            TILE_SIZE,
            3 * TILE_SIZE,
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
        return BufferedImage(cm, raster, true, null)
    }

    private fun writePNGBytes(sortedPoints: List<Point>): ByteArray {
        val image = toBufferedImage(sortedPoints)
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }

    fun writePNG(): File {

        val path = "web/tiles/$zTile/$xTile/$yTile.png"
        val dir = File("web/tiles/$zTile/$xTile/")
        if (!dir.exists()){
            dir.mkdirs()
        }
        val mapFile = File(path)
        try {
            logger.info("Writing $path")
            mapFile.writeBytes(pngByteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mapFile
    }
}