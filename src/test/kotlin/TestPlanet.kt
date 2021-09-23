import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.Planet
import com.eliottgray.kotlin.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlanetConstructorTest {

    @Test
    fun test_positive_case(){
        val planet = Planet()
        assertEquals(Defaults.SEED, planet.seed)
    }

    @Test
    fun test_custom_params(){
        val seed = 999183.0
        val planet = Planet(seed=seed)
        assertEquals(seed, planet.seed)
    }

}

class GetElevationAtCoordinateTest {

    @Test
    fun test_low_resolution(){
        val planet = Planet(seed=99987.0)
        val elevation = planet.getElevationAt(lat=-10.0, lon=-43.0, resolution=100000).alt
        assertEquals(-2206.5303409091907, elevation)
    }

    @Test
    fun test_high_resolution(){
        val planet = Planet(seed=54399875.0)
        val elevation = planet.getElevationAt(lat=45.0, lon=23.0, resolution=50).alt
        assertEquals(160.4482471060341, elevation)
    }
}

class GetMultipleElevationsTest {

    lateinit var planet: Planet

    @BeforeEach
    fun setUp(){
        planet = Planet(seed=99987.0)
    }

    @Test
    fun test_multiple_points(){
        val points = arrayListOf(
            Point.fromSpherical(lat=-10.0, lon=-43.0, resolution=100000),
            Point.fromSpherical(lat=45.0, lon=23.0, resolution=100000)
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
