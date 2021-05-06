package com.eliottgray.kotlin

class Planet(val seed: Double = Defaults.SEED, val resolution: Int = Defaults.RESOLUTION_METERS){
    private val squishedSeed = Utils.squishSeed(seed)
    private val tetra = Tetrahedron.buildDefault(squishedSeed)

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

    private fun recursiveGetH3Elevations(points: ArrayList<Point>, current: Tetrahedron): ArrayList<Point> {
        if (points.isEmpty()){
            return points
        }
        if (current.longestSide > resolution) {
            val (leftTetra, rightTetra) = current.subdivide()
            // TODO: Avoid needing to create an arrayList for each tetrahedron created. Expensive!
            val leftNodes = ArrayList<Point>()
            val rightNodes = ArrayList<Point>()
            for (point in points) {
                if (leftTetra.contains(point)){
                    leftNodes.add(point)
                } else {
                    assert(rightTetra.contains(point))  // TODO: only enable during testing, or not have this at all.
                    rightNodes.add(point)
                }
            }
            val results = recursiveGetH3Elevations(leftNodes, leftTetra)
            results.addAll(recursiveGetH3Elevations(rightNodes, rightTetra))
            return results
        } else {
            val elevation = current.averageAltitude
            for (point in points) {
                // While it is often better to avoid side effects, this way we reuse the current object.
                point.alt = elevation
            }
            return points
        }
    }

    fun getH3Elevations(h3Nodes: ArrayList<Point>): ArrayList<Point> {
        return recursiveGetH3Elevations(h3Nodes, this.tetra)
    }

}
