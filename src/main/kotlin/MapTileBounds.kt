package com.eliottgray.kotlin

data class MapTileBounds(val north: Double, val south: Double, val east: Double, val west: Double) {

    companion object {
        fun fromGeographicTileXYZ(z: Int, x: Int, y: Int): MapTileBounds {
            val xTiles = 2 shl z
            val yTiles = 1 shl z

            val xTileWidth = 360.0 / xTiles
            val west = x * xTileWidth -180.0
            val east = (x + 1) * xTileWidth - 180.0

            val yTileHeight = 180.0 / yTiles
            val north = 90.0 - y * yTileHeight
            val south = 90.0 - (y + 1) * yTileHeight
            return MapTileBounds(north=north, south=south, east=east, west=west)
        }
    }
}