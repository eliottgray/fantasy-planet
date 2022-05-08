import com.eliottgray.kotlin.MapTile
import com.eliottgray.kotlin.MapTileKey
import com.eliottgray.kotlin.Planet
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertEquals


class MapTileTest {

    @Test
    fun test_specific_tile_and_seed(){
        val seed = 762391.0
        val mapTileKey = MapTileKey(2, 1, 1, seed)
        val mapTile = Planet.get(seed).getMapTile(mapTileKey)
        val actualFile = mapTile.writePNG()
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

    @Test
    fun test_longitudinal_width_of_pixel(){
        val expected = 11119.492664455875
        val actual = MapTile.longitudinalWidthOfPixelMeters(0.0, 0.1)
        assertEquals(expected, actual)
    }

    private fun imageToBytes(bi: BufferedImage): ByteArray {
        return (bi.data.dataBuffer as DataBufferByte).data
    }
}

