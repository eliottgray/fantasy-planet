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
        const val TILE_SIZE = 256

        // 156543.03392804062 for tileSize 256 Pixels
        private const val INITIAL_RESOLUTION = 2 * PI * 6378137 / TILE_SIZE

        // 20037508.342789244
        private const val ORIGIN_SHIFT = 2 * PI * 6378137 / 2.0

        private const val DEGREES_TO_RAD = 180 / PI

        private fun xyzToNWCorner(z: Int, x: Int, y: Int): MapTileCoordinate {
            // Taken from OSM docs on xyz/latLon interchange: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
            val n = 2.0.pow(z)
            val nwLon = x / n * 360.0 - 180.0
            val nwLatRad =  atan(sinh(PI * (1 - 2 * y / n)))
            val nwLat = nwLatRad * DEGREES_TO_RAD
            return MapTileCoordinate(latitude = nwLat, longitude = nwLon)
        }

        fun pixelsToLatLon(px: Double, py: Double, zoom: Int): MapTileCoordinate {
            val res: Double = resolution(zoom)
            val mx = px * res - ORIGIN_SHIFT
            val my = -py * res + ORIGIN_SHIFT

            val lon: Double = mx / ORIGIN_SHIFT * 180.0
            var lat: Double = my / ORIGIN_SHIFT * 180.0
            lat = 180 / Math.PI * (2 * atan(exp(lat * Math.PI / 180.0)) - Math.PI / 2.0)
            return MapTileCoordinate(latitude = lat, longitude = lon)
        }

        private fun resolution(zoomLevel: Int): Double {
            return INITIAL_RESOLUTION / matrixSize(zoomLevel)
        }

        private fun matrixSize(zoomLevel: Int): Int {
            return 1 shl zoomLevel
        }
    }

    fun generate(): ArrayList<Point> {
        val xPixelStart = (xTile * TILE_SIZE) + 1
        val xPixelEnd = xPixelStart + TILE_SIZE

        val yPixelStart = (yTile * TILE_SIZE) + 1
        val yPixelEnd = yPixelStart + TILE_SIZE

        val allPoints = ArrayList<Point>()
        for (xPixel in xPixelStart until xPixelEnd){
            for (yPixel in yPixelStart until yPixelEnd){
                val tileCoordinate = pixelsToLatLon(px=xPixel.toDouble(), py=yPixel.toDouble(), zoom=zTile)
                val point = Point.fromSpherical(lat=tileCoordinate.latitude, lon=tileCoordinate.longitude)
                allPoints.add(point)
            }
        }

        assert(allPoints.size == TILE_SIZE * TILE_SIZE)

        val middleXPixel = xPixelStart + (TILE_SIZE / 2)
        val middleYPixel = yPixelStart + (TILE_SIZE / 2)
        val widthOfPixelMeters = longitudinalPixelLengthInMeters(middleXPixel.toDouble(), middleYPixel.toDouble())

        // TODO: Each Pixel should have its own depth, rather than relying on the tile center for all.
        val planet = Planet(seed=seed, resolution = ceil(widthOfPixelMeters * 0.6).toInt())
        return planet.getMultipleElevations(allPoints)
    }

    private fun longitudinalPixelLengthInMeters(px: Double, py: Double): Double {
        val middleCoordinate = pixelsToLatLon(px=px, py=py, zoom=zTile)
        val neighborCoordinate = pixelsToLatLon(px=1+px, py=py, zoom=zTile)

        // TODO: Just use Haversine formula instead of using ECEF distance.  Overkill!
        val first = Point.fromSpherical(lon = middleCoordinate.longitude, lat = middleCoordinate.latitude)
        val second = Point.fromSpherical(lon = neighborCoordinate.longitude, lat = neighborCoordinate.latitude)
        return first.distance(second)
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

    fun nwCorner(): MapTileCoordinate {
        return xyzToNWCorner(zTile, xTile, yTile)
    }

    fun seCorner(): MapTileCoordinate {
        return xyzToNWCorner(zTile, xTile + 1, yTile + 1)
    }
}