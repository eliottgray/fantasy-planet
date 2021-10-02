package com.eliottgray.kotlin

import kotlin.math.max
import kotlin.math.min

data class MapTileElevations(val minElevation: Double, val maxElevation: Double) {
    companion object {
        fun fromTopTiles(topTileOne: MapTile, topTileTwo: MapTile): MapTileElevations {
            return MapTileElevations(
                minElevation = min(topTileOne.minElev, topTileTwo.minElev),
                maxElevation = max(topTileOne.maxElev, topTileTwo.maxElev)
            )
        }
    }
}
