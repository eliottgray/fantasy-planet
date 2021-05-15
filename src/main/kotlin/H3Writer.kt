package com.eliottgray.kotlin
import com.uber.h3core.H3Core
import com.uber.h3core.LengthUnit
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class H3Writer(val h3Depth: Int, val seed: Double = Defaults.SEED) {
    private val h3Core: H3Core = H3Core.newInstance()
    private val edgeLength = h3Core.edgeLength(h3Depth, LengthUnit.m)
    private val planet = Planet(seed = seed, resolution = (edgeLength * 0.6).roundToInt())

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

    suspend fun collectAndWrite(filepath: String) = coroutineScope {
        val channel = Channel<Point>()

        val res0 = h3Core.res0Indexes
        var count = 0

        for (chunkedNodes in res0.chunked( 15)) {
            val allPoints = ArrayList<Point>()
            for (res0Node in chunkedNodes) {
                val children = h3Core.h3ToChildren(res0Node, h3Depth)
                for (child in children) {
                    val geo = h3Core.h3ToGeo(child)
                    val point = Point.fromSpherical(lat = geo.lat, lon = geo.lng)
                    allPoints.add(point)
                }
            }
            count += allPoints.size
            val localPlanet = Planet(seed = seed, resolution = (edgeLength * 0.6).roundToInt())
            launch {
                localPlanet.getMultipleElevationsRecursiveAsync(allPoints, channel)
            }
        }

        // TODO: handle errors related to IO.

        val finishedPoints = ArrayList<Point>()
        var newPoint: Point
        while (count > 0) {
            newPoint = channel.receive()
            finishedPoints.add(newPoint)
            count--
        }

        val file = File(filepath)
        if (file.exists()){
            file.delete()
        }

        val bufferedWriter = file.bufferedWriter()

        val features = JSONArray()
        for (point in finishedPoints){
            val feature = toGeoJSONFeature(point)
            features.add(feature)
        }
        val featureCollection = JSONObject()
        featureCollection["type"] = "FeatureCollection"
        featureCollection["features"] = features

        bufferedWriter.write(featureCollection.toJSONString())
        bufferedWriter.close()
    }
}
