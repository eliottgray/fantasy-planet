
import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*

class ApplicationTest {

    private val testEnv = createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load("application.conf"))
    }

    @Test
    fun testRoot() {
        withApplication(testEnv) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(ContentType("text", "html", listOf(HeaderValueParam("charset", "UTF-8"))), response.contentType())
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testTile() {
        withApplication(testEnv) {
            handleRequest(HttpMethod.Get, "/tiles/762391.0/2/1/1.png").apply {
                // TODO: Compare saved expected file to returned bytes.
                assertEquals(ContentType.Image.PNG, response.contentType())
                assertTrue((response.byteContent?.size ?: 0) > 0)
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}