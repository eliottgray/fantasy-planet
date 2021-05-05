import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.Planet
import com.eliottgray.kotlin.PlanetError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PlanetConstructorTest {

    @Test
    fun test_positive_case(){
        val planet = Planet()
        assertEquals(Defaults.SEED, planet.seed)
        assertEquals(Defaults.RESOLUTION, planet.resolution)
    }

    @Test
    fun test_custom_params(){
        val seed = 999183
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

class GetElevationAtCoordinateTest{

    @Test
    fun test_low_resolution(){
        val planet = Planet(seed=99987, resolution=100000)
        val elevation = planet.getElevationAt(lat=-10.0, lon=-43.0)
        assertEquals(322.5023400433702, elevation)
    }

    @Test
    fun test_high_resolution(){
        val planet = Planet(seed=54399875, resolution=50)
        val elevation = planet.getElevationAt(lat=45.0, lon=23.0)
        assertEquals(371.7954327115484, elevation)
    }
}