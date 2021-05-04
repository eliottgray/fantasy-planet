package com.eliottgray.kotlin
import kotlin.random.Random

const val DEFAULT_ALTITUDE = -.02   // Just below 'sea level' of 0 altitude.

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
            a.alt = DEFAULT_ALTITUDE
            b.alt = DEFAULT_ALTITUDE
            c.alt = DEFAULT_ALTITUDE
            d.alt = DEFAULT_ALTITUDE
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
}

/*

    def __init__(self, a: Point, b: Point, c: Point, d: Point):
        self.a = a
        self.b = b
        self.c = c
        self.d = d
        # TODO: Profile space/time difference of pre-generating a tuple for each point, or just defining them here.
        self._longest_side_len = None

    @staticmethod
    def build_default(seed, alt=25000000) -> 'Tetrahedron':
        """
        Creates a Tetrahedron with default orientation and altitudes.
        :return: Default Tetrahedron.
        """
        local_random = random.Random(seed)

        a = Point.from_spherical(lat=90, lon=0, alt=alt, seed=local_random.random())
        b = Point.from_spherical(lat=-30, lon=0, alt=alt, seed=local_random.random())
        c = Point.from_spherical(lat=-30, lon=120, alt=alt, seed=local_random.random())
        d = Point.from_spherical(lat=-30, lon=-120, alt=alt, seed=local_random.random())
        a.alt = DEFAULT_ALTITUDE
        b.alt = DEFAULT_ALTITUDE
        c.alt = DEFAULT_ALTITUDE
        d.alt = DEFAULT_ALTITUDE
        default = Tetrahedron(a=a, b=b, c=c, d=d)
        return default

 */