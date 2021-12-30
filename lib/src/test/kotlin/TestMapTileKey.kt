import com.eliottgray.kotlin.MapTileKey
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class TestMapTileKey {

    @Test
    fun test_valid_key() {
        val key = MapTileKey(0, 0, 0, 0.0)
        assertTrue(key.isValid())
    }

    @Test
    fun test_negative_z_coordinate() {
        val key = MapTileKey(-1, 0, 0, 0.0)
        assertFalse(key.isValid())
    }

    @Test
    fun test_negative_x_coordinate() {
        val key = MapTileKey(0, -1, 0, 0.0)
        assertFalse(key.isValid())
    }

    @Test
    fun test_positive_invalid_x_coordinate() {
        val key = MapTileKey(0, 2, 0, 0.0)
        assertFalse(key.isValid())
    }

    @Test
    fun test_negative_y_coordinate() {
        val key = MapTileKey(0, 0, -1, 0.0)
        assertFalse(key.isValid())
    }

    @Test
    fun test_positive_invalid_y_coordinate() {
        val key = MapTileKey(0, 0, 1, 0.0)
        assertFalse(key.isValid())
    }
}