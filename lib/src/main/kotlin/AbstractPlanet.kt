package com.eliottgray.kotlin

abstract class AbstractPlanet {

    abstract fun getMapTile(mapTileKey: MapTileKey): MapTile
}