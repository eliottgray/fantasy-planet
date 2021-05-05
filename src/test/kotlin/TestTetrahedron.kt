import org.junit.jupiter.api.Test
import com.eliottgray.kotlin.Point
import com.eliottgray.kotlin.Tetrahedron
import com.eliottgray.kotlin.Defaults
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach


class TetrahedronConstructorTest {

    @Test
    fun test_arbitrary_tetrahedron(){
        val a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)
        val b = Point(x=1.0, y=1.0, z=0.0, alt=1.0)
        val c = Point(x=-1.0, y=1.0, z=0.0, alt=1.0)
        val d = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        val tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        assertEquals(a, tetra.a)
        assertEquals(b, tetra.b)
        assertEquals(c, tetra.c)
        assertEquals(d, tetra.d)
    }

    // TODO: Enable test of default once .contains() method is implemented.
    @Test
    fun test_default(){
        // Default tetrahedron should cover all surface-level points on the globe.
        val default = Tetrahedron.buildDefault(Defaults.SEED)
        val failures = arrayListOf<String>()
        for (lat in -90..91 step 5) {
            for (lon in -180..181 step 5) {
                val point = Point.fromSpherical(lat=lat.toDouble(), lon=lon.toDouble(), alt=0.0, seed=Defaults.SEED)
                if (!default.contains(point)){
                    failures.add("Lat $lat lon $lon")
                }
            }
        }
        val failureSize = failures.size
        assertEquals(0, failureSize, "$failureSize failures to find test point contained in default Tetrahedron.")
    }
}

class ContainsPointTest {

    private lateinit var tetra: Tetrahedron

    @BeforeEach
    fun setUp(){
        val a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)
        val b = Point(x=1.0, y=1.0, z=0.0, alt=1.0)
        val c = Point(x=-1.0, y=1.0, z=0.0, alt=1.0)
        val d = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        this.tetra = Tetrahedron(a=a, b=b, c=c, d=d)
    }

    @Test
    fun test_vertex(){
        val vertex = this.tetra.a.copy()
        assertTrue(this.tetra.contains(vertex))
    }

    @Test
    fun test_positive_case(){
        val point = Point(x=0.0, y=0.0, z=0.5, alt=0.0)
        assertTrue(this.tetra.contains(point))
    }

    @Test
    fun test_negative_case(){
        val point = Point(x=0.0, y=0.0, z=-1.0, alt=0.0)
        assertFalse(this.tetra.contains(point))
    }

    @Test
    fun test_on_edge(){
        val point = Point(x=0.0, y=-0.5, z=0.5, alt=0.0)
        assertTrue(this.tetra.contains(point))
    }
}

class RotateTetrahedronTest {

    private lateinit var tetra: Tetrahedron

    @BeforeEach
    fun setUp(){
        val a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)   // "Top" of pyramid.
        val b = Point(x=1.0, y=0.0, z=-1.0, alt=0.9)
        val c = Point(x=-1.0, y=1.0, z=-1.0, alt=0.8)
        val d = Point(x=-1.0, y=-1.0, z=-1.0, alt=0.7)
        this.tetra = Tetrahedron(a=a, b=b, c=c, d=d)
    }

    @Test
    fun test_rotate_around_x_axis_90_degrees(){
        val a = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        val b = Point(x=1.0, y=1.0, z=0.0, alt=0.9)
        val c = Point(x=-1.0, y=1.0, z=1.0, alt=0.8)
        val d = Point(x=-1.0, y=1.0, z=-1.0, alt=0.7)
        val expected = Tetrahedron(a=a, b=b, c=c, d=d)
        val actual = this.tetra.rotateAroundXAxis(90.0)
        compareTetrahedrons(expected=expected, actual=actual)
    }

    @Test
    fun test_rotate_around_y_axis_90_degrees(){
        val a = Point(x=1.0, y=0.0, z=0.0, alt=1.0)
        val b = Point(x=-1.0, y=0.0, z=-1.0, alt=0.9)
        val c = Point(x=-1.0, y=1.0, z=1.0, alt=0.8)
        val d = Point(x=-1.0, y=-1.0, z=1.0, alt=0.7)
        val expected = Tetrahedron(a=a, b=b, c=c, d=d)
        val actual = this.tetra.rotateAroundYAxis(90.0)
        compareTetrahedrons(expected=expected, actual=actual)
    }
}

class LongestSideTest{

    private lateinit var tetra: Tetrahedron
    private val expected = 2.8284271247461903

    @BeforeEach
    fun setUp(){
        // Side a-d is longer than the others.
        val a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        val b = Point(x=1.0, y=1.0, z=-1.0, alt=2.0)
        val c = Point(x=1.0, y=-1.0, z=-1.0, alt=3.0)
        val d = Point(x=-1.0, y=0.0, z=-1.0, alt=4.0)
        this.tetra = Tetrahedron(a=a, b=b, c=c, d=d)
    }

    private fun run_test(tetra: Tetrahedron){
        val actual = tetra.longestSide
        assertEquals(expected, actual, 1.0E-7)
        assertEquals(expected, tetra.a.distance(tetra.b), 1.0E-7)  // Longest side must always be A-B.
    }

    @Test
    fun test_simple_case(){
        run_test(this.tetra)
    }

    @Test
    fun test_rotated_case(){
        val rotated = this.tetra.rotateAroundXAxis(29.0).rotateAroundYAxis(123.0)
        run_test(rotated)
    }

    @Test
    fun test_scrambled_points_case(){
        val scrambled = Tetrahedron(a=this.tetra.b, b=this.tetra.d, c=this.tetra.a, d=this.tetra.c)
        run_test(scrambled)
    }
}

class SubdivideTest{

    private fun run_test(tetra: Tetrahedron, expectedOne: Tetrahedron, expectedTwo: Tetrahedron) {
        val (subOne, subTwo) = tetra.subdivide()
        compareTetrahedrons(expectedOne, subOne)
        compareTetrahedrons(expectedTwo, subTwo)
    }

    @Test
    fun test_simple_case(){
        // Side a-d longer than the others.
        val a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        val b = Point(x=1.0, y=1.0, z=-1.0, alt=2.0)
        val c = Point(x=1.0, y=-1.0, z=-1.0, alt=3.0)
        val d = Point(x=-1.0, y=0.0, z=-1.0, alt=4.0)
        val tetra = Tetrahedron(a=a, b=b, c=c, d=d)

        val length = a.distance(d)
        val midpoint = a.midpoint(d, length)
        val subdividedA = Tetrahedron(a=a, b=midpoint, c=b, d=c)
        val subdividedB = Tetrahedron(a=midpoint, b=d, c=b, d=c)
        run_test(tetra, subdividedA, subdividedB)
    }
    // TODO: Validate that, in the case of equal sides, edges are preferred in a guaranteed order.
}


class AverageAltitudeTest {

    private fun constructTetra(alt1: Double, alt2: Double, alt3: Double, alt4: Double): Tetrahedron {
        val a = Point(x=0.0, y=0.0, z=1.0, alt=alt1)
        val b = Point(x=1.0, y=0.0, z=-1.0, alt=alt2)
        val c = Point(x=-1.0, y=1.0, z=-1.0, alt=alt3)
        val d = Point(x=-1.0, y=-1.0, z=-1.0, alt=alt4)
        return Tetrahedron(a=a, b=b, c=c, d=d)
    }

    private fun run_test(tetra: Tetrahedron, expected: Double){
        val actual = tetra.averageAltitude
        assertEquals(expected, actual, 1.0E-7)
    }

    @Test
    fun test_identical_altitudes(){
        val alt = 1.0
        val tetra = constructTetra(alt, alt, alt, alt)
        run_test(tetra, alt)
    }

    @Test
    fun test_different_altitudes(){
        val expected = 1.0
        val tetra = this.constructTetra(1.2, 1.1, 0.9, 0.8)
        run_test(tetra, expected)
    }

    @Test
    fun test_tiny_difference(){
        val one = 1.000001
        val two = 1.000003
        val expected = 1.000002
        val tetra = this.constructTetra(one, one, two, two)
        run_test(tetra, expected)
    }
}
