package com.eliottgray.kotlin

data class Tetrahedron constructor(val a: Point, val b: Point, val c: Point, val d: Point, val longestSide: Double) {

    var normalABC: Vector
    var normalABD: Vector
    var normalACD: Vector
    var normalDBC: Vector

    var aDotB: Double
    var aDotC: Double
    var aDotD: Double
    var dDotA: Double
    init {
        // Normal CrossProduct
        val ab = a.vectorTo(b)
        val ac = a.vectorTo(c)
        val ad = a.vectorTo(d)

        normalABC = ab.crossProduct(ac)
        normalABD = ab.crossProduct(ad)
        normalACD = ac.crossProduct(ad)
        normalDBC = d.vectorTo(b).crossProduct(d.vectorTo(c))

        // DotProduct - 4th Point
        aDotD = normalABC.dotProduct(ad)
        aDotB = normalACD.dotProduct(ab)
        aDotC = normalABD.dotProduct(ac)
        dDotA = normalDBC.dotProduct(d.vectorTo(a))
    }
    val averageAltitude = (this.a.alt + this.b.alt + this.c.alt + this.d.alt) / 4

    companion object {

        fun buildDefault(seed: Double, alt: Double=25_000_000.0): Tetrahedron {
            // Creates a Tetrahedron with default orientation and altitudes.
            val aSeed = mutateSeed(seed, seed)
            val bSeed = mutateSeed(seed, aSeed)
            val cSeed = mutateSeed(seed, bSeed)
            val dSeed = mutateSeed(seed, cSeed)

            val a = Point.fromSpherical(
                lat = 89.0,
                lon = 1.0,
                initialAlt = alt + 1_000_000,
                seed = aSeed,
                altSeed = Defaults.ALTITUDE_METERS
            )
            val b = Point.fromSpherical(
                lat = -29.1,
                lon = -1.1,
                initialAlt = alt + 2_000_000,
                seed = bSeed,
                altSeed = Defaults.ALTITUDE_METERS
            )
            val c = Point.fromSpherical(
                lat = -28.2,
                lon = 119.2,
                initialAlt = alt + 3_000_000,
                seed = cSeed,
                altSeed = Defaults.ALTITUDE_METERS
            )
            val d = Point.fromSpherical(
                lat = -31.3,
                lon = -121.3,
                initialAlt = alt + 4_000_000,
                seed = dSeed,
                altSeed = Defaults.ALTITUDE_METERS
            )

            return withOrderedPoints(a=a, b=b, c=c, d=d)
        }

        fun withOrderedPoints(a: Point, b: Point, c: Point, d: Point): Tetrahedron {
            val ab = a.distance(b)
            val ac = a.distance(c)
            val ad = a.distance(d)
            val bc = b.distance(c)
            val bd = b.distance(d)
            val cd = c.distance(d)

            var longest: Double = ab
            if (ac > longest) longest = ac
            if (ad > longest) longest = ad
            if (bc > longest) longest = bc
            if (bd > longest) longest = bd
            if (cd > longest) longest = cd

            val e1: Point
            val e2: Point
            val n1: Point
            val n2: Point

            // In the case of identical sides, prefer reordering by checking CD first and AB last.
            when (longest) {
                cd -> {
                    e1 = c
                    e2 = d
                    n1 = a
                    n2 = b
                }
                bd -> {
                    e1 = b
                    e2 = d
                    n1 = a
                    n2 = c
                }
                bc -> {
                    e1 = b
                    e2 = c
                    n1 = a
                    n2 = d
                }
                ad -> {
                    e1 = a
                    e2 = d
                    n1 = b
                    n2 = c
                }
                ac -> {
                    e1 = a
                    e2 = c
                    n1 = b
                    n2 = d
                }
                else -> {
                    assert(ab == longest)
                    e1 = a
                    e2 = b
                    n1 = c
                    n2 = d
                }
            }
            return Tetrahedron(e1, e2, n1, n2, longest)
        }

        private fun sameSide(a: Point, b: Point, c: Point, d: Point, tested: Point): Boolean {

            // Normal CrossProduct
            val ab = a.vectorTo(b)
            val ac = a.vectorTo(c)
            val normalABC = ab.crossProduct(ac)

            // DotProduct - 4th Point
            val ad = a.vectorTo(d)
            val aDotD = normalABC.dotProduct(ad)

            // DotProduct - TestPoint
            val aTested = a.vectorTo(tested)
            val aDotTested = normalABC.dotProduct(aTested)

            return ((aDotD < 0) == (aDotTested < 0)) || aDotTested == 0.0

        }

        fun sameSide(testedDotProduct: Double, comparisonDotProduct: Double): Boolean {
            return ((comparisonDotProduct < 0) == (testedDotProduct < 0)) || testedDotProduct == 0.0
        }
    }



    fun contains(point: Point): Boolean {
        // StackOverflow-suggested solution: check if tested point is behind all 3 triangles
        // https://stackoverflow.com/a/25180294
        val at = a.vectorTo(point)

        val testedABC = normalABC.dotProduct(at)
        if (!sameSide(testedDotProduct = testedABC, comparisonDotProduct = aDotD)) {
            return false
        }
        val testedABD = normalABD.dotProduct(at)
        if (!sameSide(testedDotProduct = testedABD, comparisonDotProduct = aDotC)) {
            return false
        }

        val testedACD = normalACD.dotProduct(at)
        if (!sameSide(testedDotProduct = testedACD, comparisonDotProduct = aDotB)) {
            return false
        }

        val testedDBC = normalDBC.dotProduct(d.vectorTo(point))
        if (!sameSide(testedDotProduct = testedDBC, comparisonDotProduct = dDotA)) {
            return false
        }
        return true
    }

    fun rotateAroundXAxis(degrees: Double): Tetrahedron {
        val newA = this.a.rotateAroundXAxis(degrees)
        val newB = this.b.rotateAroundXAxis(degrees)
        val newC = this.c.rotateAroundXAxis(degrees)
        val newD = this.d.rotateAroundXAxis(degrees)
        return Tetrahedron(a = newA, b = newB, c = newC, d = newD, longestSide = this.longestSide)
    }

    fun rotateAroundYAxis(degrees: Double): Tetrahedron {
        val newA = this.a.rotateAroundYAxis(degrees)
        val newB = this.b.rotateAroundYAxis(degrees)
        val newC = this.c.rotateAroundYAxis(degrees)
        val newD = this.d.rotateAroundYAxis(degrees)
        return Tetrahedron(a = newA, b = newB, c = newC, d = newD, longestSide = this.longestSide)
    }

    fun subdivide(): Pair<Tetrahedron, Tetrahedron> {
        val length = this.longestSide
        assert(length == a.distance(b))
        val midpoint = this.a.midpoint(this.b, length)  // A->B is required to be the longest side.
        val tetraOne = withOrderedPoints(a=this.a, b=midpoint, c=this.c, d=this.d)
        val tetraTwo = withOrderedPoints(a=midpoint, b=this.b, c=this.c, d=this.d)
        return Pair(tetraOne, tetraTwo)
    }
}
