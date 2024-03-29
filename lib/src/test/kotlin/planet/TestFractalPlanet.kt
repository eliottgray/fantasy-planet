import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.planet.FractalPlanet
import com.eliottgray.kotlin.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FractalPlanetTest {

    @Test
    fun test_positive_case(){
        val fractalPlanet = FractalPlanet(Defaults.SEED)
        assertEquals(Defaults.SEED, fractalPlanet.seed)
    }

    @Test
    fun test_custom_params(){
        val seed = 999183.0
        val fractalPlanet = FractalPlanet(seed=seed)
        assertEquals(seed, fractalPlanet.seed)
    }

    @Test
    fun test_low_resolution(){
        val fractalPlanet = FractalPlanet(seed=99987.0)
        val elevation = fractalPlanet.getElevationAt(lat=-10.0, lon=-43.0, resolution=100000).alt
        assertEquals(-2206.5303409091907, elevation)
    }

    @Test
    fun test_high_resolution(){
        val fractalPlanet = FractalPlanet(seed=54399875.0)
        val elevation = fractalPlanet.getElevationAt(lat=45.0, lon=23.0, resolution=50).alt
        assertEquals(160.4482471060341, elevation)
    }

    @Test
    fun test_multiple_points(){
        val fractalPlanet = FractalPlanet(seed=99987.0)
        val points = arrayListOf(
            Point.fromSpherical(lat=-10.0, lon=-43.0, resolution=100000),
            Point.fromSpherical(lat=45.0, lon=23.0, resolution=100000),
            Point.fromSpherical(lat=45.0, lon=23.0, resolution=100000),
            Point.fromSpherical(lat=0.0, lon=0.0, resolution=9876),
            )
        val results = fractalPlanet.getMultipleElevations(points)
        for (point in results){
            when (point.lat) {
                -10.0 -> {
                    assertEquals(-2206.5303409091907, point.alt, 1.0E-7)
                }
                45.0 -> {
                    assertEquals(point.lat, 45.0)
                    assertEquals(-1272.5419541701758, point.alt, 1.0E-7)
                }
                else -> {
                    assertEquals(point.lat, 0.0)
                    assertEquals(2175.729900945921, point.alt, 1.0E-7)
                }
            }
        }
    }
}
