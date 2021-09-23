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

        private fun xyzToNWCorner(z: Int, x: Int, y: Int): MapTileCoordinate {
            // Taken from OSM docs on xyz/latLon interchange: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
            val n = 2.0.pow(z)
            val nwLon = x / n * 360.0 - 180.0
            val nwLat =  Math.toDegrees(atan(sinh(PI * (1 - 2 * y / n))))
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

        fun haversineDistanceMeters(
            coordOne: MapTileCoordinate,
            coordTwo: MapTileCoordinate,
        ): Double {
            var lat1 = coordOne.latitude
            var lat2 = coordTwo.latitude
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(coordTwo.longitude - coordOne.longitude)

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
    }

    fun generate(): ArrayList<Point> {
        println("Generating $zTile $xTile $yTile")
        val xPixelStart = (xTile * TILE_SIZE) + 1
        val xPixelEnd = xPixelStart + TILE_SIZE

        val yPixelStart = (yTile * TILE_SIZE) + 1
        val yPixelEnd = yPixelStart + TILE_SIZE

        val allPoints = ArrayList<Point>()
        for (yPixel in yPixelStart until yPixelEnd){

            // It is necessary to determine the appropriate depth to calculate, as the length of a degree of longitude
            // varies by latitude. Do this once for each discrete latitude in the tile.
            val widthOfPixelMeters = longitudinalPixelLengthInMeters(yPixel.toDouble())

            for (xPixel in xPixelStart until xPixelEnd){
                val tileCoordinate = pixelsToLatLon(px=xPixel.toDouble(), py=yPixel.toDouble(), zoom=zTile)
                val point = Point.fromSpherical(lat=tileCoordinate.latitude, lon=tileCoordinate.longitude,  resolution = ceil(widthOfPixelMeters * 0.6).toInt())
                allPoints.add(point)
            }
        }

        assert(allPoints.size == TILE_SIZE * TILE_SIZE)

        // TODO: Map tile should not generate the planet.
        val planet = Planet(seed=seed)
        return planet.getMultipleElevations(allPoints)
    }

    private fun longitudinalPixelLengthInMeters(py: Double): Double {
        val startCoordinate = pixelsToLatLon(px=0.0, py=py, zoom=zTile)
        val neighborCoordinate = pixelsToLatLon(px=1.0, py=py, zoom=zTile)
        return haversineDistanceMeters(startCoordinate, neighborCoordinate)
    }

    fun writePNG(topTile: MapTile = this): File {
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
        val mapFile = File(path)
        try {
            println("Writing $path")
            ImageIO.write(image, "png", mapFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mapFile
    }

    fun nwCorner(): MapTileCoordinate {
        return xyzToNWCorner(zTile, xTile, yTile)
    }

    fun seCorner(): MapTileCoordinate {
        return xyzToNWCorner(zTile, xTile + 1, yTile + 1)
    }
}