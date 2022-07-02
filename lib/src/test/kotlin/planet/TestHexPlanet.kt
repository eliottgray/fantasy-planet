import com.eliottgray.kotlin.Defaults
import com.eliottgray.kotlin.planet.HexPlanet
import com.eliottgray.kotlin.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HexPlanetTest {
    val h3Resolution = 5

    @Test
    fun test_positive_case(){
        val hexPlanet = HexPlanet(Defaults.SEED, h3Resolution)
        assertEquals(Defaults.SEED, hexPlanet.seed)
    }

    @Test
    fun test_custom_params(){
        val seed = 999183.0
        val hexPlanet = HexPlanet(seed=seed, h3Resolution)
        assertEquals(seed, hexPlanet.seed)
    }

    @Test
    fun test_low_resolution(){
        val hexPlanet = HexPlanet(seed=99987.0, h3Resolution)
        val elevation = hexPlanet.getElevationAt(lat=-10.0, lon=-43.0, resolution=100000).alt
        assertEquals(-2206.5303409091907, elevation)
    }

    @Test
    fun test_high_resolution(){
        val hexPlanet = HexPlanet(seed=54399875.0, h3Resolution)
        val elevation = hexPlanet.getElevationAt(lat=45.0, lon=23.0, resolution=50).alt
        assertEquals(160.4482471060341, elevation)
    }

    @Test
    fun test_multiple_points(){
        val hexPlanet = HexPlanet(seed=99987.0, h3Resolution)
        val points = arrayListOf(
            Point.fromSpherical(lat=-10.0, lon=-43.0, resolution=100000),
            Point.fromSpherical(lat=45.0, lon=23.0, resolution=100000),
            Point.fromSpherical(lat=45.0, lon=23.0, resolution=100000),
            Point.fromSpherical(lat=0.0, lon=0.0, resolution=9876),
            )
        val results = hexPlanet.getMultipleElevations(points)
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
