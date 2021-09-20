package com.eliottgray.kotlin


class MapTile (val zTile: Int, val xTile: Int, val yTile: Int, val seed: Double = Defaults.SEED) {

    init {
        val points = generate()
    }

    companion object {
        const val MAP_TILE_WIDTH_PIXELS = 256
        const val MAP_TILE_HEIGHT_PIXELS = 256
    }

    fun generate(): ArrayList<Point> {
        val nwCorner = xyzToNWCorner(this.zTile, this.xTile, this.yTile)
        val seCorner = xyzToNWCorner(this.zTile, this.xTile + 1, this.yTile + 1)
        val lonDelta = (seCorner.first - nwCorner.first) / MAP_TILE_WIDTH_PIXELS
        val latDelta = (nwCorner.second - seCorner.second) / MAP_TILE_HEIGHT_PIXELS

        val allPoints = ArrayList<Point>()
        var currentLon = nwCorner.first + (lonDelta / 2.0)  // Lets derive tile centers, to avoid the poles/IDL.
        while (currentLon < seCorner.first) {
            var currentLat = seCorner.second + (latDelta / 2.0) // Lets derive tile centers, to avoid the poles/IDL
            while (currentLat < nwCorner.second) {
                val point = Point.fromSpherical(lat=currentLat, lon=currentLon)
                allPoints.add(point)
                currentLat += latDelta
            }
            currentLon += lonDelta
        }

        // TODO: Determine resolution dynamically depending on the characteristics of the tile (Web Mercator).
        val planet = Planet(seed=seed, resolution = 34000)
        return planet.getMultipleElevations(allPoints)
    }

    fun writePNG(path: String) {
        // TODO write generated lat/lon records to PNG file at the given path.
    }

}