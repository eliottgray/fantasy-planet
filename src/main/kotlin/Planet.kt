package com.eliottgray.kotlin

class Planet(val seed: Double = Defaults.SEED){
    private val squishedSeed = squishSeed(seed)
    private val tetra = Tetrahedron.buildDefault(squishedSeed)

    fun getElevationAt(lat: Double, lon: Double, resolution: Int): Point {
        val point = Point.fromSpherical(lat = lat, lon = lon, resolution = resolution)
        var current = this.tetra
        var subdivisions = 0
        while (current.longestSide > resolution){
            subdivisions += 1
            val (subOne, subTwo) = current.subdivide()
            if (subOne.contains(point)){
                current = subOne
            } else {
                assert(subTwo.contains(point))
                current = subTwo
            }
        }
        return point.copy(alt=current.averageAltitude)
    }

    fun getMultipleElevations(points: ArrayList<Point>, current: Tetrahedron = this.tetra): ArrayList<Point> {
        if (points.isEmpty()){
            return points
        }

        val (leftTetra, rightTetra) = current.subdivide()
        // TODO: Avoid needing to create multiple arrayList for each tetrahedron created. Expensive!
        val results = ArrayList<Point>()
        val leftNodes = ArrayList<Point>()
        val rightNodes = ArrayList<Point>()
        for (point in points) {
            when {
                current.longestSide <= point.resolution -> {
                    results.add(point.copy(alt=current.averageAltitude))
                }
                leftTetra.contains(point) -> {
                    leftNodes.add(point)
                }
                else -> {
                    rightNodes.add(point)
                }
            }
        }
        results.addAll(getMultipleElevations(leftNodes, leftTetra))
        results.addAll(getMultipleElevations(rightNodes, rightTetra))
        return results
    }
}
