package com.eliottgray.kotlin.planet

import com.eliottgray.kotlin.*
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class FractalPlanet constructor(seed: Double = Defaults.SEED): Planet(seed) {
    private var elevations: MapTileElevations

    init {
        // We need to know what the points are for the top tiles BEFORE they go in the cache, because their min/max
        // elevations are used to determine map colors/etc. for all other tiles, to ensure consistency.
        // For now we also don't want to make the full tile objects, because doing so incurs additional work USING
        // the global elevation data we don't have yet.
        val topTileOneKey = MapTileKey(0, 0, 0, seed)
        val topTileTwoKey = MapTileKey(0, 1, 0, seed)
        // TODO: calculate top tile points asynchronously, to speed up building the planet.
        val topTileOnePoints = calculateMapTilePoints(topTileOneKey)
        val topTileTwoPoints = calculateMapTilePoints(topTileTwoKey)
        val minElevation = min(
            topTileOnePoints.minByOrNull { it.alt }?.alt ?: 0.0,
            topTileTwoPoints.minByOrNull { it.alt }?.alt ?: 0.0
        )
        val maxElevation = max(
            topTileOnePoints.maxByOrNull { it.alt }?.alt ?: 0.0,
            topTileTwoPoints.maxByOrNull { it.alt }?.alt ?: 0.0
        )
        elevations = MapTileElevations(minElevation = minElevation, maxElevation = maxElevation)
        val topTileOne = MapTile(topTileOneKey, topTileOnePoints, elevations)
        val topTileTwo = MapTile(topTileTwoKey, topTileTwoPoints, elevations)
        mapTileCache.put(topTileOneKey, CompletableFuture.completedFuture(topTileOne))
        mapTileCache.put(topTileTwoKey, CompletableFuture.completedFuture(topTileTwo))
    }
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
        return mapTileCache.get(mapTileKey) { key -> buildMapTile(key, elevations) }.get()!!
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
