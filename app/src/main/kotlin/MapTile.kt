package com.eliottgray.kotlin

import java.awt.Transparency
import java.awt.image.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.*


class MapTile (
    val zTile: Int,
    val xTile: Int,
    val yTile: Int,
    planet: Planet = Planet(Defaults.SEED),
    maxElev: Double? = null,
    minElev: Double? = null
) {
    constructor(mapTileKey: MapTileKey, topTile: MapTile? = null) : this(mapTileKey.z, mapTileKey.x, mapTileKey.y, Planet(mapTileKey.seed), topTile?.maxElev, topTile?.minElev)

    val pngByteArray: ByteArray
    private val maxElev: Double
    private val minElev: Double

    init {
        val sortedPoints = generate(planet).sortedWith(compareBy( {-it.lat}, {it.lon}))
        this.maxElev = maxElev ?: sortedPoints.maxByOrNull { it.alt }?.alt ?: 0.0
        this.minElev = minElev ?: sortedPoints.minByOrNull { it.alt }?.alt ?: 0.0
        pngByteArray = writePNGBytes(sortedPoints)
    }

    companion object {
        const val TILE_SIZE = 256

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

        fun longitudinalWidthOfPixelMeters(pixelLat: Double, pixelLongitudeWidth: Double): Double {
            return haversineDistanceMeters(
                MapTileCoordinate(pixelLat, 0.0),
                MapTileCoordinate(pixelLat, pixelLongitudeWidth)
            )
        }
    }

    private fun generate(planet: Planet): MutableList<Point> {
        println("Generating $zTile $xTile $yTile for seed ${planet.seed}")
        val allPoints = ArrayList<Point>()

        val tileBounds: MapTileBounds = MapTileBounds.fromGeographicTileXYZ(zTile, xTile, yTile)
        val lonDelta = (tileBounds.east - tileBounds.west) / TILE_SIZE
        val latDelta = (tileBounds.north - tileBounds.south) / TILE_SIZE
        var currentLat = tileBounds.north
        while (currentLat > tileBounds.south) {

            // It is necessary to determine the appropriate depth to calculate, as the length of a degree of longitude
            // varies by latitude. Do this once for each discrete latitude in the tile.
            val widthOfPixelMeters = longitudinalWidthOfPixelMeters(currentLat, lonDelta)

            var currentLon = tileBounds.west
            while (currentLon < tileBounds.east) {
                allPoints.add(
                    Point.fromSpherical(
                        lat=currentLat,
                        lon=currentLon,
                        resolution = ceil(widthOfPixelMeters * 0.6).toInt()
                    )
                )
                currentLon += lonDelta
            }
            currentLat -= latDelta
        }
        assert(allPoints.size == TILE_SIZE * TILE_SIZE)
        return planet.getMultipleElevations(allPoints)
    }

    fun toBufferedImage(sortedPoints: List<Point>): BufferedImage {
        val oldRange = maxElev - minElev
        // TODO: ColorMap should be a param for the map tile.
        val newRange = ColorMap.ELEVATION_RANGE

        val aByteArray: ByteArray = sortedPoints.map{ point ->
            val newValue = (((point.alt - minElev) * newRange) / oldRange)
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
            println("Writing $path")
            mapFile.writeBytes(pngByteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mapFile
    }
}