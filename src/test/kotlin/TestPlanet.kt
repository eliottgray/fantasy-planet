import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.Planet
import com.eliottgray.kotlin.PlanetError
import com.eliottgray.kotlin.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlanetConstructorTest {

    @Test
    fun test_positive_case(){
        val planet = Planet()
        assertEquals(Defaults.SEED, planet.seed)
        assertEquals(Defaults.RESOLUTION_METERS, planet.resolution)
    }

    @Test
    fun test_custom_params(){
        val seed = 999183.0
        val resolution = 99
        val planet = Planet(seed=seed, resolution = resolution)
        assertEquals(seed, planet.seed)
        assertEquals(resolution, planet.resolution)
    }

    @Test
    fun test_negative_resolution(){
        val invalidResolution = -1
        assertThrows(PlanetError::class.java) { Planet(resolution = invalidResolution) }
    }

    @Test
    fun test_zero_resolution(){
        val invalidResolution = 0
        assertThrows(PlanetError::class.java) { Planet(resolution = invalidResolution) }
    }
}

class GetElevationAtCoordinateTest {

    @Test
    fun test_low_resolution(){
        val planet = Planet(seed=99987.0, resolution=100000)
        val elevation = planet.getElevationAt(lat=-10.0, lon=-43.0)
        assertEquals(-2206.5303409091907, elevation)
    }

    @Test
    fun test_high_resolution(){
        val planet = Planet(seed=54399875.0, resolution=50)
        val elevation = planet.getElevationAt(lat=45.0, lon=23.0)
        assertEquals(160.4482471060341, elevation)
    }
}

class GetMultipleElevationsTest {

    lateinit var planet: Planet

    @BeforeEach
    fun setUp(){
        planet = Planet(seed=99987.0, resolution=100000)
    }

    @Test
    fun test_multiple_points(){
        val points = arrayListOf(
            Point.fromSpherical(lat=-10.0, lon=-43.0),
            Point.fromSpherical(lat=45.0, lon=23.0)
        )
        val results = planet.getMultipleElevations(points)

        for (point in results){
            if (point.lat == -10.0) {
                assertEquals(-2206.5303409091907, point.alt, 1.0E-7)
            } else {
                assertEquals(point.lat, 45.0)
                assertEquals(-1272.5419541701758, point.alt)
            }
        }
    }
}
