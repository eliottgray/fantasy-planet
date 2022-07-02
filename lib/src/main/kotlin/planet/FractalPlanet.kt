package com.eliottgray.kotlin.planet

import com.eliottgray.kotlin.*
import kotlin.math.ceil

class FractalPlanet constructor(seed: Double = Defaults.SEED): Planet(seed) {

    override fun calculateMapTilePoints(mapTileKey: MapTileKey): MutableList<Point> {
        val allPoints = ArrayList<Point>()

        val tileBounds: MapTileBounds = MapTileBounds.fromGeographicTileXYZ(mapTileKey.z, mapTileKey.x, mapTileKey.y)
        val lonDelta = (tileBounds.east - tileBounds.west) / MapTile.TILE_SIZE
        val latDelta = (tileBounds.north - tileBounds.south) / MapTile.TILE_SIZE
        var currentLat = tileBounds.north
        while (currentLat > tileBounds.south) {

            // It is necessary to determine the appropriate depth to calculate, as the length of a degree of longitude
            // varies by latitude. Do this once for each discrete latitude in the tile.
            val widthOfPixelMeters = MapTile.longitudinalWidthOfPixelMeters(currentLat, lonDelta)

            var currentLon = tileBounds.west
            while (currentLon < tileBounds.east) {
                allPoints.add(
                    Point.fromSpherical(
                        lat = currentLat,
                        lon = currentLon,
                        resolution = ceil(widthOfPixelMeters * 0.6).toInt()
                    )
                )
                currentLon += lonDelta
            }
            currentLat -= latDelta
        }
        assert(allPoints.size == MapTile.TILE_SIZE * MapTile.TILE_SIZE)
        return getMultipleElevations(allPoints)
    }

}
