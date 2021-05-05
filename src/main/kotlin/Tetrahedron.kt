package com.eliottgray.kotlin
import kotlin.random.Random

class Tetrahedron(var a: Point, var b: Point, var c: Point, var d: Point) {

    val longestSide: Double by lazy {
        val ab = this.a.distance(this.b)
        val ac = this.a.distance(this.c)
        var longest: Double?
        var e1: Point?
        var e2: Point?
        var n1: Point?
        var n2: Point?
        if (ab >= ac){
            longest = ab
            e1 = this.a
            e2 = this.b
            n1 = this.c
            n2 = this.d
        } else{
            longest = ac
            e1 = this.a
            e2 = this.c
            n1 = this.b
            n2 = this.d
        }
        val ad = this.a.distance(this.d)
        if (ad > longest){
            longest = ad
            e1 = this.a
            e2 = this.d
            n1 = this.b
            n2 = this.c
        }
        val bc = this.b.distance(this.c)
        if (bc > longest) {
            longest = bc
            e1 = this.b
            e2 = this.c
            n1 = this.a
            n2 = this.d
        }

        val bd = this.b.distance(this.d)
        if (bd > longest){
            longest = bd
            e1 = this.b
            e2 = this.c
            n1 = this.a
            n2 = this.d
        }
        val cd = this.c.distance(this.d)
        if (cd > longest){
            longest = cd
            e1 = this.c
            e2 = this.d
            n1 = this.a
            n2 = this.b
        }
        // TODO: It would be good to avoid needing side effects. Instead of reassignment, consider saving new reference.
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

        fun buildDefault(seed: Int, alt: Double=25000000.0): Tetrahedron{
            // Creates a Tetrahedron with default orientation and altitudes
            val random = Random(seed)

            val a = Point.fromSpherical(lat=90.0, lon=0.0, alt=alt, seed=random.nextInt())
            val b = Point.fromSpherical(lat=-30.0, lon=0.0, alt=alt, seed=random.nextInt())
            val c = Point.fromSpherical(lat=-30.0, lon=120.0, alt=alt, seed=random.nextInt())
            val d = Point.fromSpherical(lat=-30.0, lon=-120.0, alt=alt, seed=random.nextInt())

            // TODO ideally don't have to override altitude after fromSpherical, but alt is used for lat/lon -> XYZ.
            a.alt = Defaults.ALTITUDE_METERS
            b.alt = Defaults.ALTITUDE_METERS
            c.alt = Defaults.ALTITUDE_METERS
            d.alt = Defaults.ALTITUDE_METERS
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

    override fun toString(): String {
        val aStr = this.a.toString()
        val bStr = this.b.toString()
        val cStr = this.c.toString()
        val dStr = this.d.toString()
        return "Tetrahedron(a=$aStr, b=$bStr, c=$cStr, d=$dStr)"
    }
}
