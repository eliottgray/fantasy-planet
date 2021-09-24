package com.eliottgray.kotlin
import com.uber.h3core.H3Core
import com.uber.h3core.LengthUnit
import kotlinx.coroutines.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File
import kotlin.math.roundToInt

class H3Writer(val h3Depth: Int, val seed: Double = Defaults.SEED) {
    private val h3Core: H3Core = H3Core.newInstance()
    private val edgeLength = h3Core.edgeLength(h3Depth, LengthUnit.m)
    private val planet = Planet(seed = seed)

    private fun toGeoJSONFeature(point: Point): JSONObject {
        val properties = JSONObject()
        properties["alt"] = point.alt

        val coordinates = JSONArray()
        coordinates.add(point.lon)
        coordinates.add(point.lat)

        val geometry = JSONObject()
        geometry["type"] = "Point"
        geometry["coordinates"] = coordinates

        val feature = JSONObject()
        feature["type"] = "Feature"
        feature["properties"] = properties
        feature["geometry"] = geometry

        return feature
    }

    private fun toCSVRow(point: Point): String{
        return "${point.lat},${point.lon},${point.alt}\n"
    }

    suspend fun collectAndWrite(filepath: String) = coroutineScope {
        val deferredResults: ArrayList<Deferred<MutableList<Point>>> = ArrayList()

        for (chunkedRes0Indexes in h3Core.res0Indexes.chunked( 10)) {
            val allPoints = ArrayList<Point>()
            for (res0Node in chunkedRes0Indexes) {
                val children = h3Core.h3ToChildren(res0Node, h3Depth)
                for (child in children) {
                    val geoCoordinates = h3Core.h3ToGeo(child)
                    val point = Point.fromSpherical(lat = geoCoordinates.lat, lon = geoCoordinates.lng, resolution = (edgeLength * 0.6).roundToInt())
                    allPoints.add(point)
                }
            }
            val result = async (Dispatchers.Default) {
                planet.getMultipleElevations(allPoints)
            }
            deferredResults.add(result)
        }

        launch (Dispatchers.IO){
            // TODO: handle errors related to IO.

            val file = File(filepath)
            if (file.exists()){
                file.delete()
            }

            // Header row
            val bufferedWriter = file.bufferedWriter()
            val headerRow = "lat,lon,alt\n"
            bufferedWriter.write(headerRow)

            // Data rows
            for (newPoint in deferredResults.awaitAll().flatten()) {
                val csvRow = toCSVRow(newPoint)
                bufferedWriter.write(csvRow)
            }
            bufferedWriter.close()
        }

    }
}
