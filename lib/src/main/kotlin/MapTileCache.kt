package com.eliottgray.kotlin

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import kotlin.math.max
import kotlin.math.min


class MapTileCache {

    companion object {
        private val mapTiles: AsyncCache<MapTileKey, MapTile> = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(60))
            .buildAsync()

        private val mapSeedElevations: AsyncCache<Double, MapTileElevations> = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(60))
            .buildAsync()

        fun getTile(mapTileKey: MapTileKey): MapTile {
            return if (mapTileKey.z == 0) {
                mapTiles.get(mapTileKey) { key -> MapTile(key) }.get()!!
            } else {
                val topElevations = mapSeedElevations.get(mapTileKey.seed) { tileSeed -> buildMapTileElevations(tileSeed)}.get()!!
                mapTiles.get(mapTileKey) { key -> MapTile(key, topElevations) }.get()!!
            }
        }

        private fun buildMapTileElevations(seed: Double): MapTileElevations {
            // Unfortunately, the top tiles are needed to ensure consistent coloring of elevation ranges.
            val topTileOne = mapTiles.get(MapTileKey(0, 0, 0, seed)) { key -> MapTile(key) }.get()!!
            val topTileTwo = mapTiles.get(MapTileKey(0, 1, 0, seed)) { key -> MapTile(key) }.get()!!
            val minElev = min(topTileOne.minElev, topTileTwo.minElev)
            val maxElev = max(topTileOne.maxElev, topTileTwo.maxElev)
            return MapTileElevations(minElevation = minElev, maxElevation = maxElev)
        }
    }
}