import com.eliottgray.kotlin.CoordinateError
import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test


class TestPointConstructor {

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

class TestPointFromSphericalCoordinates {

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
