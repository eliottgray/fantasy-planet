import com.eliottgray.kotlin.MapTileBounds
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class TestMapTileBounds {

    @Test
    fun test_level_zero_right_side() {
        val expected = MapTileBounds(north=90.0, south=-90.0, east=180.0, west=0.0)
        val actual = MapTileBounds.fromGeographicTileXYZ(z=0, x=1, y=0)
        assertEquals(expected, actual)
    }

    @Test
    fun test_level_zero_left_side() {
        val expected = MapTileBounds(north=90.0, south=-90.0, east=0.0, west=-180.0)
        val actual = MapTileBounds.fromGeographicTileXYZ(z=0, x=0, y=0)
        assertEquals(expected, actual)
    }

    @Test
    fun test_level_two_middle() {
        val expected = MapTileBounds(north=45.0, south=0.0, east=-90.0, west=-135.0)
        val actual = MapTileBounds.fromGeographicTileXYZ(z=2, x=1, y=1)
        assertEquals(expected, actual)
    }
}