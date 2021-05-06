package com.eliottgray.kotlin
import com.uber.h3core.H3Core
import com.uber.h3core.LengthUnit
import com.uber.h3core.util.GeoCoord
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File
import kotlin.math.roundToInt

class H3Writer(val h3Depth: Int, val seed: Double = Defaults.SEED) {
    private val h3Core: H3Core = H3Core.newInstance()
    private val edgeLength = h3Core.edgeLength(h3Depth, LengthUnit.m)
    private val planet = Planet(seed = seed, resolution = (edgeLength * 0.6).roundToInt())

    private fun toGeoJSONFeature(h3Node: Long): JSONObject {
        val geo: GeoCoord = h3Core.h3ToGeo(h3Node)
        val elevation = planet.getElevationAt(lat=geo.lat, lon=geo.lng)
        val properties = JSONObject()
        properties["h3"] = h3Node
        properties["alt"] = elevation

        val coordinates = JSONArray()
        coordinates.add(geo.lng)
        coordinates.add(geo.lat)

        val geometry = JSONObject()
        geometry["type"] = "Point"
        geometry["coordinates"] = coordinates

        val feature = JSONObject()
        feature["type"] = "Feature"
        feature["properties"] = properties
        feature["geometry"] = geometry

        return feature
    }

    private fun pointToGeoJSONFeature(point: Point): JSONObject {
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
    private fun recursiveCollect(current: Long, depth: Int, features: JSONArray) {
        if (depth >= h3Depth){
            features.add(toGeoJSONFeature(current))
        } else {
            val newDepth = depth + 1
            val children = h3Core.h3ToChildren(current, newDepth)
            for (child in children){
                recursiveCollect(child, newDepth, features)
            }
        }
    }

    fun collectAndWrite(filepath: String){
        val res0 = h3Core.res0Indexes
        val children = ArrayList<Long>()
        for (res0Node in res0) {
            children.addAll(h3Core.h3ToChildren(res0Node, h3Depth))
        }
        val allPoints = ArrayList(children.map {
            val geo = h3Core.h3ToGeo(it)
            Point.fromSpherical(lat = geo.lat, lon = geo.lng)
        })

        // TODO: handle errors related to IO.


        val finishedPoints = planet.getH3Elevations(allPoints)

        val file = File(filepath)
        if (file.exists()){
            file.delete()
        }

        val bufferedWriter = file.bufferedWriter()
        for (point in finishedPoints){
            val feature = pointToGeoJSONFeature(point)
            bufferedWriter.write(feature.toJSONString())
            bufferedWriter.write("\n")
        }
        bufferedWriter.close()
    }
}
