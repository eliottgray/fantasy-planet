import com.eliottgray.kotlin.MapTile
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class MapTileTest {

    val tolerance = 0.000001
    @Test
    fun test_level_zero_corners() {
        val mapTile = MapTile(0, 0, 0)

        val nw = mapTile.nwCorner()
        val nwLon = nw.first
        val nwLat = nw.second

        assertEquals(nwLon, -180.0)
        assertEquals(nwLat, 85.051128, tolerance)

        val se = mapTile.seCorner()
        val seLon = se.first
        val seLat = se.second

        assertEquals(seLon, 180.0)
        assertEquals(seLat, -85.051128, tolerance)
    }

    @Test
    fun test_level_one_corners() {
        val mapTile = MapTile(0, 0, 0)

        val nw = mapTile.nwCorner()
        val nwLon = nw.first
        val nwLat = nw.second

        assertEquals(nwLon, -180.0)
        assertEquals(nwLat, 85.051128, tolerance)

        val se = mapTile.seCorner()
        val seLon = se.first
        val seLat = se.second

        assertEquals(seLon, 180.0)
        assertEquals(seLat, -85.051128, tolerance)
    }
}
