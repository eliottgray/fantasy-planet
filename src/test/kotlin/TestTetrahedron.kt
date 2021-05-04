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

/*

    def test_vertex(self):
        """Point identical to a vertex of the tetrahedron."""
        vertex = self.tetra.a.copy()
        self.assertTrue(self.tetra.contains(vertex))


    def test_negative_case(self):
        """Point far outside the bounds of the tetrahedron."""
        point = Point(x=0.0, y=0.0, z=-1.0, alt=0.0)
        self.assertFalse(self.tetra.contains(point))

    def test_on_edge(self):
        """By setting the fixture tetrahedron to include a right triangle, a point on the edge can be easily tested."""
        point = Point(x=0.0, y=-0.5, z=0.5, alt=0.0)
        self.assertTrue(self.tetra.contains(point))
 */
