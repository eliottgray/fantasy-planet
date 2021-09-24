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

    fun getMultipleElevations(points: MutableList<Point>, current: Tetrahedron = this.tetra): MutableList<Point> {
        if (points.isEmpty()){
            return points
        }

        val (leftTetra, rightTetra) = current.subdivide()

        val resolutionComparator = fun(point: Point): Boolean {
            return current.longestSide <= point.resolution
        }

        val doneIndex: Int = points.partitionInPlaceBy(resolutionComparator)

        // All the points sorted to the front of the list can now have their altitude set.
        val donePoints = points.subList(0, doneIndex)
        for (i in 0 until doneIndex) {
            donePoints[i] = donePoints[i].copy(alt=current.averageAltitude)
        }

        val containmentComparator = fun(point: Point): Boolean {
            return leftTetra.contains(point)
        }
        // All remaining points must be sorted into the tetrahedron they are contained within.
        val pendingPoints = points.subList(doneIndex, points.size)
        val containmentIndex = pendingPoints.partitionInPlaceBy(containmentComparator)

        val leftPoints = pendingPoints.subList(0, containmentIndex)
        val rightPoints = pendingPoints.subList(containmentIndex, pendingPoints.size)

        getMultipleElevations(leftPoints, leftTetra)
        getMultipleElevations(rightPoints, rightTetra)
        return points
    }

}

fun MutableList<Point>.partitionInPlaceBy(compareFunc: (Point) -> Boolean): Int {

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
