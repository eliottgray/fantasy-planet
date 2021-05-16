package com.eliottgray.kotlin

import kotlinx.coroutines.channels.Channel

class Planet(val seed: Double = Defaults.SEED, val resolution: Int = Defaults.RESOLUTION_METERS){
    private val squishedSeed = squishSeed(seed)
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
                assert(subTwo.contains(point))
                current = subTwo
            }
        }
        return current.averageAltitude
    }

    fun getMultipleElevations(points: ArrayList<Point>, current: Tetrahedron = this.tetra): ArrayList<Point> {
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
                    assert(rightTetra.contains(point))
                    rightNodes.add(point)
                }
            }
            val results = getMultipleElevations(leftNodes, leftTetra)
            results.addAll(getMultipleElevations(rightNodes, rightTetra))
            return results
        } else {
            val elevation = current.averageAltitude
            return ArrayList(points.map {
                it.copy(alt=elevation)
            })
        }
    }

    suspend fun getMultipleElevationsRecursiveAsync(points: ArrayList<Point>, channel: Channel<Point>, current: Tetrahedron = this.tetra) {
        if (points.isEmpty()){
            return
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
                    assert(rightTetra.contains(point))
                    rightNodes.add(point)
                }
            }
            getMultipleElevationsRecursiveAsync(leftNodes, channel, leftTetra)
            getMultipleElevationsRecursiveAsync(rightNodes, channel, rightTetra)
            return
        } else {
            val elevation = current.averageAltitude
            for (point in points) {
                // While it is often better to avoid side effects, this way we reuse the current object.
                channel.send(point.copy(alt=elevation))
            }
        }
    }
}
