package com.eliottgray.kotlin.planet

import com.eliottgray.kotlin.*
import com.uber.h3core.H3Core
import com.uber.h3core.LengthUnit
import kotlin.math.roundToInt

class HexPlanet constructor(seed: Double = Defaults.SEED, private val h3Resolution: Int): Planet(seed) {
    companion object {
        private val h3Core = H3Core.newInstance()
    }

    override fun calculateMapTilePoints(mapTileKey: MapTileKey): MutableList<Point> {
        val allPoints = ArrayList<Point>()
        val h3ResMeters = h3Core.edgeLength(h3Resolution, LengthUnit.m).roundToInt()

        val tileBounds: MapTileBounds = MapTileBounds.fromGeographicTileXYZ(mapTileKey.z, mapTileKey.x, mapTileKey.y)
        val lonDelta = (tileBounds.east - tileBounds.west) / MapTile.TILE_SIZE
        val latDelta = (tileBounds.north - tileBounds.south) / MapTile.TILE_SIZE
        var currentLat = tileBounds.north
        while (currentLat > tileBounds.south) {

            var currentLon = tileBounds.west
            while (currentLon < tileBounds.east) {
                allPoints.add(
                    Point.fromSpherical(
                        lat = currentLat,
                        lon = currentLon,
                        resolution = h3ResMeters,
                        h3Index = h3Core.geoToH3(currentLat, currentLon, h3Resolution)
                    )
                )
                currentLon += lonDelta
            }
            currentLat -= latDelta
        }
        assert(allPoints.size == MapTile.TILE_SIZE * MapTile.TILE_SIZE)

        val hexKeyToPointMap = allPoints.map {
            val geoCoord = h3Core.h3ToGeo(it.h3Index!!)
            Point.fromSpherical(lat = geoCoord.lat, lon = geoCoord.lng, resolution = h3ResMeters, h3Index = it.h3Index)
        }.toMutableList().let {
            getMultipleElevations(it)
        }.associateBy {
            HexKey(it.h3Index!!, seed)
        }

        return allPoints.map {
            val hexKey = HexKey(it.h3Index!!, seed)
            val hex = hexKeyToPointMap[hexKey]!!
            it.copy(alt=hex.alt)
        }.toMutableList()
    }

}
