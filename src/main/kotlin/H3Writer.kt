package com.eliottgray.kotlin
import com.uber.h3core.H3Core
import com.uber.h3core.LengthUnit
import com.uber.h3core.util.GeoCoord
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File
import kotlin.math.roundToInt

class H3Writer(val h3Depth: Int) {
    private val h3Core: H3Core = H3Core.newInstance()
    private val edgeLength = h3Core.edgeLength(h3Depth, LengthUnit.m)
    private val planet = Planet(resolution = (edgeLength * 0.6).roundToInt())

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
        val file = File(filepath)
        if (file.exists()){
            file.delete()
        }
        // TODO: handle errors related to IO.
        // TODO: File should not exist before writing, since write will append text repeatedly.


        val features = JSONArray()
        for (index in res0) {
            recursiveCollect(index, 0, features)
        }

        val featureCollection = JSONObject()
        featureCollection["type"] = "FeatureCollection"
        featureCollection["features"] = features

        file.writeText(featureCollection.toJSONString())
    }
}