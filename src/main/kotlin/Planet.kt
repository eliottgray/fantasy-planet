package com.eliottgray.kotlin

class Planet(val seed: Int = Defaults.SEED, val resolution: Int = Defaults.RESOLUTION){
    val tetra = Tetrahedron.buildDefault(seed)

    init {
        if (this.resolution <= 0){
            throw PlanetError("Illegal resolution encountered: $resolution. Resolution must be a positive, non-zero integer.")
        }
    }

    fun getElevationAt(lat: Double, lon: Double): Double {
        val point = Point.fromSpherical(lat = lat, lon = lon)
        var current = this.tetra
        var subdivisions = 0
        while (current.longestSide > resolution){
            subdivisions += 1
            val (subOne, subTwo) = current.subdivide()
            if (subOne.contains(point)){
                current = subOne
            } else {
                assert(subTwo.contains(point))  // TODO: only enable during testing, or not have this at all.
                current = subTwo
            }
        }
        return current.averageAltitude
    }
}
