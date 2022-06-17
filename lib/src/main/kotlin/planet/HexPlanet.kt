package com.eliottgray.kotlin.planet

import com.eliottgray.kotlin.*
import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.uber.h3core.H3Core
import com.uber.h3core.LengthUnit
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HexPlanet constructor(seed: Double = Defaults.SEED, private val h3Resolution: Int): Planet(seed) {
    private var elevations: MapTileElevations

    init {
        // We need to know what the points are for the top tiles BEFORE they go in the cache, because their min/max
        // elevations are used to determine map colors/etc. for all other tiles, to ensure consistency.
        // For now we also don't want to make the full tile objects, because doing so incurs additional work USING
        // the global elevation data we don't have yet.
        val topTileOneKey = MapTileKey(0, 0, 0, seed)
        val topTileOnePoints = calculateMapTilePoints(topTileOneKey)
        val minElevation = topTileOnePoints.minByOrNull { it.alt }?.alt ?: 0.0
        val maxElevation = topTileOnePoints.maxByOrNull { it.alt }?.alt ?: 0.0
        elevations = MapTileElevations(minElevation = minElevation, maxElevation = maxElevation)
        val topTileOne = MapTile(topTileOneKey, topTileOnePoints, elevations)
        mapTileCache.put(topTileOneKey, CompletableFuture.completedFuture(topTileOne))
    }
    companion object {

        private val mapTileCache: AsyncCache<MapTileKey, MapTile> = Caffeine.newBuilder()
            .maximumSize(10000)
            .buildAsync()

        private val planetCache: AsyncCache<Double, HexPlanet> = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofMinutes(60))
            .buildAsync()

        // TODO: Configure this cache, maxsize, expiration rules, etc.
        private val hexCache: Cache<HexKey, Point> = Caffeine.newBuilder().build()
        private val h3Core = H3Core.newInstance()

        fun get(seed: Double, h3Resolution: Int): HexPlanet {
            return planetCache.get(seed) { it -> HexPlanet(it, h3Resolution) }.get()!!
        }
    }
    override fun getMapTile(mapTileKey: MapTileKey): MapTile {
        return mapTileCache.get(mapTileKey) { key -> buildMapTile(key, elevations) }.get()!!
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

        val hexesToCalculate = allPoints.filter{
            hexCache.getIfPresent(HexKey(h3Index = it.h3Index!!, seed)) == null
        }.map {
            val geoCoord = h3Core.h3ToGeo(it.h3Index!!)
            Point.fromSpherical(lat = geoCoord.lat, lon = geoCoord.lng, resolution = h3ResMeters, h3Index = it.h3Index)
        }

        // For all missing hexes, we can calculate their elevations and then store.
        if (hexesToCalculate.isNotEmpty()) {
            hexesToCalculate
                .toMutableList()
                .let { getMultipleElevations(it) }
                .forEach {
                    val hexKey = HexKey(it.h3Index!!, seed)
                    hexCache.put(hexKey, it)
                }
        }

        return allPoints.map {
            val hex = hexCache.getIfPresent(HexKey(it.h3Index!!, seed))!!  // TODO: Retrieve from just-calculated values
            it.copy(alt=hex.alt)
        }.toMutableList()
    }

}
