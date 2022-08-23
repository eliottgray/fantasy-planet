import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.eliottgray.kotlin.MapTileKey
import com.eliottgray.kotlin.planet.FractalPlanet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class HandlerTest {
    private val handler = Handler()
    private val context = TestContext()

    @Test
    fun testMapTile() {
        val seed = 12345
        val x = 0
        val y = 0
        val z = 0
        val mapTileKey = MapTileKey(z, x, y, seed.toDouble())
        val pathParameters = mapOf(
            "x" to x.toString(),
            "y" to y.toString(),
            "z" to z.toString(),
            "seed" to seed.toString()
        )
        val event = APIGatewayV2HTTPEvent.builder().withPathParameters(pathParameters).build()
        val result = handler.handleRequest(event, context)
        val actualBytesBase64 = result.body
        val actualBytes = Base64.getDecoder().decode(actualBytesBase64)
        val expectedBytes = FractalPlanet(seed.toDouble()).getMapTile(mapTileKey).pngByteArray
        assertEquals(expectedBytes.size, actualBytes.size, "Length of result failed to match expected result.")
        for (i in actualBytes.indices) {
            val expectedByte = expectedBytes[i]
            val actualByte = actualBytes[i]
            assertEquals(expectedByte, actualByte, "Contents of result failed to match expected result.")
        }
    }
}