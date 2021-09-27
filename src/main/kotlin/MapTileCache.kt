package com.eliottgray.kotlin

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration


class MapTileCache {

    companion object {
        private val mapTiles: AsyncCache<MapTileKey, MapTile> = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(60))
            .buildAsync()

        fun getTile(mapTileKey: MapTileKey): MapTile {
            // Unfortunately top tile is needed to ensure consistent coloring of elevation ranges.
            // TODO: With changes to use Geographic Tiling, Tile 0,0,0 is only one of two top tiles!  Use Both!
            val topKey = mapTileKey.copy(z=0,x=0,y=0)
            val topTile = mapTiles.get(topKey) { key -> MapTile(key) }.get()!!
            return if (mapTileKey == topKey) {
                topTile
            } else {
                return mapTiles.get(mapTileKey) { key -> MapTile(key, topTile) }.get()!!
            }
        }
    }
}