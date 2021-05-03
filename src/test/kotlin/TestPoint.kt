import com.eliottgray.kotlin.CoordinateError
import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.assertNotSame


class ConstructorTest {

    @Test
    fun test_positive_case(){
        val point = Point(lat =30.0, lon =45.0, alt =20.3, x =1.0, y =0.7, z =-1.1)
        assertEquals(30.0, point.lat)
        assertEquals(45.0, point.lon)
        assertEquals(20.3, point.alt)
        assertEquals(1.0, point.x)
        assertEquals(0.7, point.y)
        assertEquals(-1.1, point.z)
        assertEquals(Defaults.DEFAULT_SEED, point.seed)
    }
}

class FromSphericalTest {

    private fun run_test(lat: Double = 0.0, lon: Double = 0.0, alt: Double = 0.0, x: Double = 0.0, y: Double = 0.0, z: Double = 0.0){
        val actualPoint = Point.fromSpherical(lat=lat, lon=lon, alt=alt)

        // Cartesian coordinates may be approximate, as they are the result of a transformation.
        assertEquals(x, actualPoint.x, 1.0E-7)
        assertEquals(y, actualPoint.y, 1.0E-7)
        assertEquals(z, actualPoint.z, 1.0E-7)

        // Spherical coordinates must have been stored without being transformed.
        assertEquals(lat, actualPoint.lat)
        assertEquals(lon, actualPoint.lon)
        assertEquals(alt, actualPoint.alt)
    }

    @Test
    fun test_x_axis(){
        // The Origin (0.0, 0.0) and it's Antipode represent the bounds of the X axis
        this.run_test(lat=0.0, lon=0.0, x=6378137.0)
        this.run_test(lat=0.0, lon=180.0, x=-6378137.0)
        this.run_test(lat=0.0, lon=-180.0, x=-6378137.0)
    }

    @Test
    fun test_y_axis(){
        // 90 and negative 90 represent the bounds of the Y axis.
        this.run_test(lat=90.0, lon=0.0, z=6356752.314245179)
        this.run_test(lat=-90.0, lon=0.0, z=-6356752.314245179)    }

    @Test
    fun test_z_axis(){
        // The poles represent the bounds of the Z axis.
        this.run_test(lat=90.0, lon=0.0, z=6356752.314245179)
        this.run_test(lat=-90.0, lon=0.0, z=-6356752.314245179)
    }

    @Test
    fun test_all_axes(){
        this.run_test(lat=45.0, lon=45.0, x=3194419.1450605732, y=3194419.1450605732, z=4487348.408865919)
    }

    @Test
    fun test_invalid_latitude(){
        assertThrows(CoordinateError::class.java) { this.run_test(lat=91.0) }
        assertThrows(CoordinateError::class.java) { this.run_test(lat=90.0001) }
    }

    @Test
    fun test_invalid_longitude(){
        assertThrows(CoordinateError::class.java) { this.run_test(lat=181.0) }
        assertThrows(CoordinateError::class.java) { this.run_test(lat=180.0001) }
    }
}

class RotateAroundXAxisTest{

    private lateinit var fixture: Point

    @BeforeEach
    fun setUp(){
        this.fixture = Point(x=0.5, y=0.6, z=0.7, alt=1.0)
    }

    private fun run_test(degrees: Double, expected: Point){
        val actualPoint = this.fixture.rotateAroundXAxis(degrees)
        // TODO: Use equality override, to test all attributes at once?  Is that even necessary?
        assertEquals(expected.x, actualPoint.x, 1.0E-7)
        assertEquals(expected.y, actualPoint.y, 1.0E-7)
        assertEquals(expected.z, actualPoint.z, 1.0E-7)
        assertEquals(expected.seed, actualPoint.seed)
    }

    @Test
    fun test_rotate_90_degrees(){
        val degrees = 90.0
        val expected = Point(x=0.5, y=-0.7, z=0.6, alt=1.0)
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_180_degrees(){
        val degrees = 180.0
        val expected = Point(x=0.5, y=-0.6, z=-0.7, alt=1.0)
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_270_degrees(){
        val degrees = 270.0
        val expected = Point(x=0.5, y=0.7, z=-0.6, alt=1.0)
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_360_degrees(){
        val degrees = 360.0
        val expected = this.fixture.copy()
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_negative_90_degrees(){
        val degrees = -90.0
        val expected = Point(x=0.5, y=0.7, z=-0.6, alt=1.0)
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_45_degrees(){
        val degrees = 45.0
        val expected = Point(x=0.5, y=-0.070710678, z=0.919238815, alt=1.0)
        run_test(degrees, expected)
    }
}

class RotateAroundYAxisTest{

    private lateinit var fixture: Point

    @BeforeEach
    fun setUp(){
        this.fixture = Point(x=0.5, y=0.6, z=0.7, alt=1.0)
    }

    private fun run_test(degrees: Double, expected: Point){
        val actualPoint = this.fixture.rotateAroundYAxis(degrees)
        // TODO: Use equality override, to test all attributes at once?  Is that even necessary?
        assertEquals(expected.x, actualPoint.x, 1.0E-7)
        assertEquals(expected.y, actualPoint.y, 1.0E-7)
        assertEquals(expected.z, actualPoint.z, 1.0E-7)
        assertEquals(expected.seed, actualPoint.seed)
    }

    @Test
    fun test_rotate_90_degrees(){
        val degrees = 90.0
        val expected = Point(x=0.7, y=0.6, z=-0.5, alt=1.0)
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_180_degrees(){
        val degrees = 180.0
        val expected = Point(x=-0.5, y=0.6, z=-0.7, alt=1.0)
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_270_degrees(){
        val degrees = 270.0
        val expected = Point(x=-0.7, y=0.6, z=0.5, alt=1.0)
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_360_degrees(){
        val degrees = 360.0
        val expected = this.fixture.copy()
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_negative_90_degrees(){
        val degrees = -90.0
        val expected = Point(x=-0.7, y=0.6, z=0.5, alt=1.0)
        run_test(degrees, expected)
    }

    @Test
    fun test_rotate_45_degrees(){
        val degrees = 45.0
        val expected = Point(x=0.848528137, y=0.6, z=0.141421356, alt=1.0)
        run_test(degrees, expected)
    }
}

class CopyTest {

    @Test
    fun test_copy() {
        val fixture = Point(x = 0.5, y = 0.6, z = 0.7, alt = 1.0)
        val copy = fixture.copy()

        // TODO: Use equality override, to test all attributes at once?  Is that even necessary?
        assertEquals(fixture.x, copy.x, 1.0E-7)
        assertEquals(fixture.y, copy.y, 1.0E-7)
        assertEquals(fixture.z, copy.z, 1.0E-7)
        assertEquals(fixture.alt, copy.alt, 1.0E-7)
        assertEquals(fixture.seed, copy.seed)

        // Guards against error to actually have the fixture and copy being different entities.
        assertNotSame(fixture, copy)
    }
}

class DistanceTest {

    private lateinit var fixture: Point

    @BeforeEach
    fun setUp() {
        this.fixture = Point(x = 0.0, y = 0.0, z = 0.0, alt = 0.0)
    }

    private fun euclidean_distance(a: Point, b: Point): Double {
        val xSquared = (a.x - b.x).pow(2)
        val ySquared = (a.y - b.y).pow(2)
        val zSquared = (a.z - b.z).pow(2)
        return sqrt(xSquared + ySquared + zSquared)
    }

    private fun run_test(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) {
        val other = Point(x = x, y = y, z = z, alt = 0.0)
        val expected = euclidean_distance(this.fixture, other)
        val actual = this.fixture.distance(other)
        assertEquals(expected, actual, 1.0E-7)
    }

    @Test
    fun identical_points() {
        this.run_test()
    }

    @Test
    fun one_axis() {
        this.run_test(x = 1.0)
    }

    @Test
    fun two_axis() {
        this.run_test(x = 1.0, y = 4.0)
    }

    @Test
    fun three_axis() {
        this.run_test(x = 1.0, y = 2.0, z = -3.0)
    }

}