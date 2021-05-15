package com.eliottgray.kotlin

data class Tetrahedron(var a: Point, var b: Point, var c: Point, var d: Point) {

    val longestSide: Double by lazy {
        val ab = this.a.distance(this.b)
        val ac = this.a.distance(this.c)
        val ad = this.a.distance(this.d)
        val bc = this.b.distance(this.c)
        val bd = this.b.distance(this.d)
        val cd = this.c.distance(this.d)

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
                e1 = this.c
                e2 = this.d
                n1 = this.a
                n2 = this.b
            }
            bd -> {
                e1 = this.b
                e2 = this.d
                n1 = this.a
                n2 = this.c
            }
            bc -> {
                e1 = this.b
                e2 = this.c
                n1 = this.a
                n2 = this.d
            }
            ad -> {
                e1 = this.a
                e2 = this.d
                n1 = this.b
                n2 = this.c
            }
            ac -> {
                e1 = this.a
                e2 = this.c
                n1 = this.b
                n2 = this.d
            }
            else -> {
                assert(ab == longest)
                e1 = this.a
                e2 = this.b
                n1 = this.c
                n2 = this.d
            }
        }

        // TODO: It would be good to avoid needing side effects. Instead of reassignment, consider
        //   returning a new Tetrahedron instead.
        this.a = e1
        this.b = e2
        this.c = n1
        this.d = n2
        longest
    }

    val averageAltitude by lazy {
        // Average altitude of constituent points.
        (this.a.alt + this.b.alt + this.c.alt + this.d.alt) / 4
    }

    companion object {

        fun buildDefault(seed: Double, alt: Double=25000000.0): Tetrahedron{
            // Creates a Tetrahedron with default orientation and altitudes
            val aSeed = mutateSeed(seed, seed)
            val bSeed = mutateSeed(seed, aSeed)
            val cSeed = mutateSeed(seed, bSeed)
            val dSeed = mutateSeed(seed, cSeed)

            val mod = 1.5
            val a = Point(x=-9771629.941634936*mod, y=-9644067.201634936*mod, z=-9580285.831634935*mod, seed=aSeed, alt=Defaults.ALTITUDE_METERS)
            val b = Point(x=-9835411.311634935*mod, y=12195322.001634935*mod, z=12131540.631634936*mod, seed=bSeed, alt=Defaults.ALTITUDE_METERS)
            val c = Point(x=12386666.111634936*mod, y=-9516504.461634936*mod, z=12003977.891634936*mod, seed=cSeed, alt=Defaults.ALTITUDE_METERS)
            val d = Point(x=12578010.221634936*mod, y=12450447.481634935*mod, z=-9452723.091634935*mod, seed=dSeed, alt=Defaults.ALTITUDE_METERS)
//            val a = Point.fromSpherical(lat=89.0, lon=1.0, initialAlt=alt+1000000, seed=aSeed, altSeed=Defaults.ALTITUDE_METERS)
//            val b = Point.fromSpherical(lat=-29.0, lon=-1.5, initialAlt=alt+2000000, seed=bSeed, altSeed=Defaults.ALTITUDE_METERS)
//            val c = Point.fromSpherical(lat=-28.0, lon=119.0, initialAlt=alt+3000000, seed=cSeed, altSeed=Defaults.ALTITUDE_METERS)
//            val d = Point.fromSpherical(lat=-31.0, lon=-121.8, initialAlt=alt+4000000, seed=dSeed, altSeed=Defaults.ALTITUDE_METERS)
            val origin = Point(x=0.0, y=0.0, z=0.0)
            println(origin.distance(a))
            println(origin.distance(b))
            println(origin.distance(c))
            println(origin.distance(d))

            return Tetrahedron(a=a, b=b, c=c, d=d)
        }

        private fun sameSide(one: Point, two: Point, three: Point, four: Point, tested: Point): Boolean {

            // Normal CrossProduct
            val ax = two.x - one.x
            val ay = two.y - one.y
            val az = two.z - one.z
            val bx = three.x - one.x
            val by = three.y - one.y
            val bz = three.z - one.z
            val normalX = ay * bz - az * by
            val normalY = az * bx - ax * bz
            val normalZ = ax * by - ay * bx

            // DotProduct - 4th Point
            val cx = four.x - one.x
            val cy = four.y - one.y
            val cz = four.z - one.z
            val dotV4 = (normalX * cx) + (normalY * cy) + (normalZ * cz)

            // DotProduct - TestPoint
            val tx = tested.x - one.x
            val ty = tested.y - one.y
            val tz = tested.z - one.z
            val dotTested = (normalX * tx) + (normalY * ty) + (normalZ * tz)

            return ((dotV4 < 0) == (dotTested < 0)) || dotTested == 0.0

        }
    }

    fun contains(point: Point): Boolean {
        // StackOverflow-suggested solution: check if tested point is behind all 3 triangles
        // https://stackoverflow.com/a/25180294

        return sameSide(this.a, this.b, this.c, this.d, point) &&
                sameSide(this.b, this.c, this.d, this.a, point) &&
                sameSide(this.c, this.d, this.a, this.b, point) &&
                sameSide(this.d, this.a, this.b, this.c, point)
    }

    fun rotateAroundXAxis(degrees: Double): Tetrahedron {
        val newA = this.a.rotateAroundXAxis(degrees)
        val newB = this.b.rotateAroundXAxis(degrees)
        val newC = this.c.rotateAroundXAxis(degrees)
        val newD = this.d.rotateAroundXAxis(degrees)
        return Tetrahedron(a = newA, b = newB, c = newC, d = newD)
    }

    fun rotateAroundYAxis(degrees: Double): Tetrahedron {
        val newA = this.a.rotateAroundYAxis(degrees)
        val newB = this.b.rotateAroundYAxis(degrees)
        val newC = this.c.rotateAroundYAxis(degrees)
        val newD = this.d.rotateAroundYAxis(degrees)
        return Tetrahedron(a = newA, b = newB, c = newC, d = newD)
    }

    fun subdivide(): Pair<Tetrahedron, Tetrahedron> {
        val length = this.longestSide
        // Since calculating the longest side orients the longest edge as A->B, we can just split between A->B.
        // TODO: Instead of relying on the side effect of calculating longest side, just retrieve what the longest edge is and divide there.
        val midpoint = this.a.midpoint(this.b, length)
        val tetraOne = Tetrahedron(a=this.a, b=midpoint, c=this.c, d=this.d)
        val tetraTwo = Tetrahedron(a=midpoint, b=this.b, c=this.c, d=this.d)
        return Pair(tetraOne, tetraTwo)
    }
}
