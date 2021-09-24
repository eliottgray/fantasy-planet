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

        fun getTile(mapTileKey: MapTileKey): ByteArray {
            // Unfortunately top tile is needed to ensure consistent coloring of elevation ranges.
            val topKey = mapTileKey.copy(z=0,x=0,y=0)
            val top = mapTiles.get(topKey) { key -> MapTile(key) }.get()!!
            return if (mapTileKey == topKey) {
                // TODO: If key is 0/0/0, pre-populate top-level data, THEN return the desired result.
                top.writeBytes()
            } else {
                val mapTile = mapTiles.get(mapTileKey) { key -> MapTile(key) }.get()!!
                mapTile.writeBytes(topTile = top)
            }
        }
    }
}