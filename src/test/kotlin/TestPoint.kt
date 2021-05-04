import com.eliottgray.kotlin.CoordinateError
import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.test.assertNotSame


fun testPointEquality(one: Point, two: Point) {
    // TODO: Use equality override, to test all attributes at once?  Is that even necessary?
    assertEquals(one.x, two.x, 1.0E-7)
    assertEquals(one.y, two.y, 1.0E-7)
    assertEquals(one.z, two.z, 1.0E-7)
    assertEquals(one.alt, two.alt, 1.0E-7)
    assertEquals(one.seed, two.seed)
}


class PointConstructorTest {

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
        testPointEquality(expected, actualPoint)
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
        testPointEquality(expected, actualPoint)
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
        testPointEquality(fixture, copy)
        assertNotSame(fixture, copy)  // Guards against error where copy just points to the same memory address.
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

class MidpointTest{

    private fun generate_seed(one: Point, two: Point): Int {
        return Random((one.seed + two.seed) / 2).nextInt()
    }

    private fun run_test(one: Point, two: Point, expected: Point) {
        val length = one.distance(two)
        val actual = one.midpoint(two, length)
        testPointEquality(expected, actual)
    }

    @Test
    fun test_origin_midpoint(){
        // Two points at opposite ends of the unit space should result in a point at the origin.
        val positive = Point(x=1.0, y=1.0, z=1.0, alt=1.0)
        val negative = Point(x=-1.0, y=-1.0, z=-1.0, alt=-1.0)
        val expected = Point(x=0.0, y=0.0, z=0.0, alt=0.9314346753803272, seed=generate_seed(positive, negative))
        run_test(one=positive, two=negative, expected=expected)
    }

    @Test
    fun test_identical_positive(){
        // Two identical points in the positive space.
        val pos1 = Point(x=1.0, y=1.0, z=1.0, alt=1.0)
        val pos2 = Point(x=1.0, y=1.0, z=1.0, alt=1.0)
        val expected = Point(x=1.0, y=1.0, z=1.0, alt=1.0, seed=generate_seed(pos1, pos2))
        run_test(one=pos1, two=pos2, expected=expected)
    }

    @Test
    fun test_identical_negative(){
        // Two identical points in the negative space.
        val neg1 = Point(x=-0.5, y=-0.5, z=-0.5, alt=-0.5)
        val neg2 = Point(x=-0.5, y=-0.5, z=-0.5, alt=-0.5)
        val expected = Point(x=-0.5, y=-0.5, z=-0.5, alt=-0.5, seed=generate_seed(neg1, neg2))
        run_test(one=neg1, two=neg2, expected=expected)
    }

    @Test
    fun test_complex_case(){
        // Two identical points in the negative space.
        val one = Point(x=0.73, y=1.0001, z=-1.0, alt=0.002)
        val two = Point(x=-1.0, y=0.01, z=-0.2343, alt=0.002)
        val expected = Point(x=-0.135, y=0.50505, z=-0.61715, alt=0.0503669060513222, seed=generate_seed(one, two))
        run_test(one=one, two=two, expected=expected)
    }
}

class ToStringTest{

    @Test
    fun test_all_values(){
        val x = 1.0
        val y = 0.9
        val z = 0.8
        val alt = 0.7
        val lat = 0.6
        val lon = 0.5
        val point = Point(x=x, y=y, z=z, alt=alt, lat=lat, lon=lon)
        val string = point.toString()
        assertTrue(string.contains(x.toString()))
        assertTrue(string.contains(y.toString()))
        assertTrue(string.contains(z.toString()))
        assertTrue(string.contains(alt.toString()))
        assertTrue(string.contains(lat.toString()))
        assertTrue(string.contains(lon.toString()))
    }
}