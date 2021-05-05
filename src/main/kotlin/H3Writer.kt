package com.eliottgray.kotlin
import com.uber.h3core.*
import com.uber.h3core.util.GeoCoord
import java.io.File
import kotlin.math.roundToInt

class H3Writer(val h3Depth: Int) {
    private val h3Core: H3Core = H3Core.newInstance()
    private val edgeLength = h3Core.edgeLength(h3Depth, LengthUnit.m)
    private val planet = Planet(resolution = (edgeLength * 0.6).roundToInt())

    private fun makeGeoJSON(h3Node: Long): String {
        val geo: GeoCoord = h3Core.h3ToGeo(h3Node)
        val elevation = planet.getElevationAt(lat=geo.lat, lon=geo.lng)
        val properties = "{\"h3\": ${h3Node}, \"alt\": $elevation}"
        val geometry = "{\"type\": \"Point\", \"coordinates\": [${geo.lng}, ${geo.lat}]}"
        return "{\"type\": \"Feature\", \"properties\": $properties, \"geometry\": $geometry}\n"
    }

    private fun recursiveWrite(current: Long, depth: Int, outputfile: File){
        if (depth >= h3Depth){
            val geoJSON = makeGeoJSON(current)
            outputfile.appendText(geoJSON)
        } else {
            val newDepth = depth + 1
            val children = h3Core.h3ToChildren(current, newDepth)
            for (child in children){
                recursiveWrite(child, newDepth, outputfile)
            }
        }
    }

    fun write(filepath: String){
        val res0 = h3Core.res0Indexes
        val file = File(filepath)
        if (file.exists()){
            file.delete()
        }
        // TODO: handle errors related to IO.
        // TODO: File should not exist before writing, since write will append text repeatedly.

        for (index in res0) {
            recursiveWrite(index, 0, file)
        }
    }
}