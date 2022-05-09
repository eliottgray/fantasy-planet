package com.eliottgray.kotlin

abstract class AbstractPlanet(val seed: Double) {
    private val squishedSeed = squishSeed(seed)
    protected val tetra: Tetrahedron = Tetrahedron.buildDefault(squishedSeed)
    abstract fun getMapTile(mapTileKey: MapTileKey): MapTile
    private fun MutableList<Point>.partitionInPlaceBy(compareFunc: (Point) -> Boolean): Int {
        // Sorts all records that return TRUE by the comparator to the front of the list, and
        //   returns the index of the first record that returns FALSE.
        var pointerOne = 0
        var pointerTwo = this.size - 1
        while (true) {
            while (pointerOne < this.size && compareFunc(this[pointerOne])) {
                pointerOne++
            }
            while (pointerTwo >= 0 && !compareFunc(this[pointerTwo])) {
                pointerTwo--
            }
            if (pointerOne >= pointerTwo) {
                break
            }
            // Using .also{} to swap! https://stackoverflow.com/questions/45377802/swap-function-in-kotlin
            this[pointerOne] = this[pointerTwo].also { this[pointerTwo] = this[pointerOne] }
        }
        return pointerOne
    }

    fun getElevationAt(lat: Double, lon: Double, resolution: Int): Point {
        val point = Point.fromSpherical(lat = lat, lon = lon, resolution = resolution)
        var current = this.tetra
        var subdivisions = 0
        while (current.longestSide > resolution){
            subdivisions += 1
            val (subOne, subTwo) = current.subdivide()
            current = if (subOne.contains(point)){
                subOne
            } else {
                assert(subTwo.contains(point))
                subTwo
            }
        }
        return point.copy(alt=current.averageAltitude)
    }

    fun getMultipleElevations(points: MutableList<Point>, current: Tetrahedron = this.tetra): MutableList<Point> {
        if (points.isEmpty()){
            return points
        }

        // Since some points may not require any further recursion, we can filter them out and set elevation.
        val doneIndex: Int = points.partitionInPlaceBy { point ->  current.longestSide <= point.resolution }
        for (i in 0 until doneIndex) {
            points[i] = points[i].copy(alt=current.averageAltitude)
        }

        // If we've identified all points as done, no need to subdivide and recurse.
        if (doneIndex == points.size) {
            return points
        }

        // All remaining points must be sorted into the tetrahedron they are contained within.
        val (leftTetra, rightTetra) = current.subdivide()

        val pendingPoints = points.subList(doneIndex, points.size)
        val containmentIndex = pendingPoints.partitionInPlaceBy { point ->
            leftTetra.contains(point)
        }

        val leftPoints = pendingPoints.subList(0, containmentIndex)
        val rightPoints = pendingPoints.subList(containmentIndex, pendingPoints.size)

        getMultipleElevations(leftPoints, leftTetra)
        getMultipleElevations(rightPoints, rightTetra)
        return points
    }

    protected fun buildMapTile(mapTileKey: MapTileKey, elevations: MapTileElevations): MapTile {
        val pointsWithElevations = calculateMapTilePoints(mapTileKey)
        return MapTile(mapTileKey, pointsWithElevations, elevations)
    }
    abstract fun calculateMapTilePoints(mapTileKey: MapTileKey): MutableList<Point>
}