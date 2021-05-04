package com.eliottgray.kotlin
import kotlin.random.Random

class Tetrahedron(val a: Point, val b: Point, val c: Point, val d: Point) {

    companion object {

        fun buildDefault(seed: Int, alt: Double=25000000.0): Tetrahedron{
            // Creates a Tetrahedron with default orientation and altitudes
            val random = Random(seed)

            val a = Point.fromSpherical(lat=90.0, lon=0.0, alt=alt, seed=random.nextInt())
            val b = Point.fromSpherical(lat=-30.0, lon=0.0, alt=alt, seed=random.nextInt())
            val c = Point.fromSpherical(lat=-30.0, lon=120.0, alt=alt, seed=random.nextInt())
            val d = Point.fromSpherical(lat=-30.0, lon=-120.0, alt=alt, seed=random.nextInt())

            // TODO ideally don't have to override altitude after fromSpherical, but alt is used for lat/lon -> XYZ.
            a.alt = Defaults.ALTITUDE
            b.alt = Defaults.ALTITUDE
            c.alt = Defaults.ALTITUDE
            d.alt = Defaults.ALTITUDE
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
}
