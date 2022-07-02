package com.eliottgray.kotlin.planet

import com.eliottgray.kotlin.*
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.math.ceil

class FractalPlanet constructor(seed: Double = Defaults.SEED): Planet(seed) {
    companion object {

        private val mapTileCache: AsyncCache<MapTileKey, MapTile> = Caffeine.newBuilder()
            .maximumSize(10000)
            .buildAsync()

        private val fractalPlanetCache: AsyncCache<Double, FractalPlanet> = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofMinutes(60))
            .buildAsync()

        fun get(seed: Double): FractalPlanet {
            return fractalPlanetCache.get(seed) { it -> FractalPlanet(it) }.get()!!
        }
    }
    override fun getMapTile(mapTileKey: MapTileKey): MapTile {
        return mapTileCache.get(mapTileKey) { key -> buildMapTile(key) }.get()!!
    }
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
