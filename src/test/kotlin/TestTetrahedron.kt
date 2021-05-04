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
        val default = Tetrahedron.buildDefault(Defaults.DEFAULT_SEED)
        val failures = arrayListOf<String>()
        for (lat in -90..91 step 5) {
            for (lon in -180..181 step 5) {
                val point = Point.fromSpherical(lat=lat.toDouble(), lon=lon.toDouble(), alt=0.0, seed=Defaults.DEFAULT_SEED)
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

    private fun compareTetrahedrons(expected: Tetrahedron, actual: Tetrahedron) {
        testPointEquality(expected.a, actual.a)
        testPointEquality(expected.b, actual.b)
        testPointEquality(expected.c, actual.c)
        testPointEquality(expected.d, actual.d)
    }

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
        this.compareTetrahedrons(expected=expected, actual=actual)
    }

    @Test
    fun test_rotate_around_y_axis_90_degrees(){
        val a = Point(x=1.0, y=0.0, z=0.0, alt=1.0)
        val b = Point(x=-1.0, y=0.0, z=-1.0, alt=0.9)
        val c = Point(x=-1.0, y=1.0, z=1.0, alt=0.8)
        val d = Point(x=-1.0, y=-1.0, z=1.0, alt=0.7)
        val expected = Tetrahedron(a=a, b=b, c=c, d=d)
        val actual = this.tetra.rotateAroundYAxis(90.0)
        this.compareTetrahedrons(expected=expected, actual=actual)
    }
}
