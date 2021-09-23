import com.eliottgray.kotlin.MapTile
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertEquals


class MapTileTest {

    val tolerance = 0.000001
    @Test
    fun test_level_zero_corners() {
        val mapTile = MapTile(0, 0, 0)

        val nw = mapTile.nwCorner()
        val nwLon = nw.longitude
        val nwLat = nw.latitude

        assertEquals(nwLon, -180.0)
        assertEquals(nwLat, 85.051128, tolerance)

        val se = mapTile.seCorner()
        val seLon = se.longitude
        val seLat = se.latitude

        assertEquals(seLon, 180.0)
        assertEquals(seLat, -85.051128, tolerance)
    }

    @Test
    fun test_level_one_corners() {
        val mapTile = MapTile(0, 0, 0)

        val nw = mapTile.nwCorner()
        val nwLon = nw.longitude
        val nwLat = nw.latitude

        assertEquals(nwLon, -180.0)
        assertEquals(nwLat, 85.051128, tolerance)

        val se = mapTile.seCorner()
        val seLon = se.longitude
        val seLat = se.latitude

        assertEquals(seLon, 180.0)
        assertEquals(seLat, -85.051128, tolerance)
    }

    @Test
    fun test_specific_tile_and_seed(){
        val seed = 762391.0
        val mapTile = MapTile(2, 1, 1, seed)
        val actualFile = mapTile.writePNG(mapTile)
        val expectedFile = File("src/test/resources/canaryTile.png")

        val actualBytes = imageToBytes(ImageIO.read(actualFile))
        val expectedBytes = imageToBytes(ImageIO.read(expectedFile))
        assertEquals(expectedBytes.size, actualBytes.size, "Length of test image ${actualFile.path} failed to match expected image ${expectedFile.path}.")
        for (i in actualBytes.indices) {
            val expectedByte = expectedBytes[i]
            val actualByte = actualBytes[i]
            assertEquals(expectedByte, actualByte, "Test image ${actualFile.path} failed to match expected image ${expectedFile.path} at byte index $i.")
        }
    }
    private fun imageToBytes(bi: BufferedImage): ByteArray {
        return (bi.data.dataBuffer as DataBufferByte).data
    }
}

