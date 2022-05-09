package com.eliottgray.kotlin

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
import kotlin.properties.Delegates

class HexPlanet constructor(seed: Double = Defaults.SEED): AbstractPlanet(seed) {
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

        private val planetCache: AsyncCache<Double, HexPlanet> = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofMinutes(60))
            .buildAsync()

        // TODO: Configure this cache, maxsize, expiration rules, etc.
        private val hexCache: Cache<HexKey, Hex> = Caffeine.newBuilder().build()

        private val h3Core = H3Core.newInstance()
        private const val h3Res = 5  // TODO: Parameterize
        private val h3ResMeters = h3Core.edgeLength(h3Res, LengthUnit.m).roundToInt()

        fun get(seed: Double): HexPlanet {
            return planetCache.get(seed) { it -> HexPlanet(it) }.get()!!
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

            var currentLon = tileBounds.west
            while (currentLon < tileBounds.east) {
                allPoints.add(
                    Point.fromSpherical(
                        lat = currentLat,
                        lon = currentLon,
                        resolution = h3ResMeters
                    )
                )
                currentLon += lonDelta
            }
            currentLat -= latDelta
        }
        assert(allPoints.size == MapTile.TILE_SIZE * MapTile.TILE_SIZE)

        val hexesToCalculate = allPoints.map {
            val h3Index = h3Core.geoToH3(it.lat, it.lon, h3Res)
            HexKey(h3Index, seed)
        }.filter{
            hexCache.getIfPresent(it) == null
        }.map {
            val geoCoord = h3Core.h3ToGeo(it.h3Index)
            val point = Point.fromSpherical(lat = geoCoord.lat, lon = geoCoord.lng, resolution = h3ResMeters)
            val hex = Hex(it.h3Index, point)
            hex
        }

        // For all missing hexes, we can calculate their elevations and then store.
        if (hexesToCalculate.isNotEmpty()) {
            hexesToCalculate
                .map { it.point }
                .toMutableList()
                .let { getMultipleElevations(it) }
                .forEach {
                    // TODO: Avoid needing to calculate the hexId multiple times. Store in the point? New subclass?
                    val h3Index = h3Core.geoToH3(it.lat, it.lon, h3Res)
                    val hexKey = HexKey(h3Index, seed)
                    val hex = Hex(h3Index, it)
                    hexCache.put(hexKey, hex)
                }
        }

        return allPoints.map {
            val h3Index = h3Core.geoToH3(it.lat, it.lon, h3Res)
            val hex = hexCache.getIfPresent(HexKey(h3Index, seed))!!  // TODO: Retrieve from just-calculated values
            it.copy(alt=hex.point.alt)
        }.toMutableList()
    }

}
