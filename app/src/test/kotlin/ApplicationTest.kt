
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.File

class ApplicationTest {

    private val appConfig = ConfigFactory.load("application.conf")

    private val testAppEnv = createTestEnvironment {
        config = HoconApplicationConfig(appConfig)
    }

    @AfterEach
    @BeforeEach
    fun cleanup() {
        // Because the demo implementation writes files to disk, it is necessary to clean them up.
        val dir = File("web/tiles/")
        if (dir.exists()){
            dir.deleteRecursively()
        }
    }

    @Test
    fun testRoot() {
        withApplication(testAppEnv) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(ContentType("text", "html", listOf(HeaderValueParam("charset", "UTF-8"))), response.contentType())
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testTile() {
        withApplication(testAppEnv) {
            handleRequest(HttpMethod.Get, "/tiles/762391.0/2/1/1.png").apply {
                // TODO: Compare saved expected file to returned bytes.
                assertEquals(ContentType.Image.PNG, response.contentType())
                assertTrue((response.byteContent?.size ?: 0) > 0)
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testTileIndexOutOfBounds() {
        withApplication(testAppEnv) {
            val invalidYCoordinate = 9999
            handleRequest(HttpMethod.Get, "/tiles/762391.0/0/0/$invalidYCoordinate.png").apply {
                assertEquals(ContentType.Text.Plain.withParameter("charset", "UTF-8"), response.contentType())
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testTileSeedNotNumeric() {
        withApplication(testAppEnv) {
            val notNumeric = "foo"
            handleRequest(HttpMethod.Get, "/tiles/$notNumeric/0/0/0.png").apply {
                assertEquals(ContentType.Text.Plain.withParameter("charset", "UTF-8"), response.contentType())
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testTileZNotNumeric() {
        withApplication(testAppEnv) {
            val notNumeric = "foo"
            handleRequest(HttpMethod.Get, "/tiles/762391.0/$notNumeric/0/0.png").apply {
                assertEquals(ContentType.Text.Plain.withParameter("charset", "UTF-8"), response.contentType())
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testTileXNotNumeric() {
        withApplication(testAppEnv) {
            val notNumeric = "foo"
            handleRequest(HttpMethod.Get, "/tiles/762391.0/0/$notNumeric/0.png").apply {
                assertEquals(ContentType.Text.Plain.withParameter("charset", "UTF-8"), response.contentType())
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testTileYNotNumeric() {
        withApplication(testAppEnv) {
            val notNumeric = "foo"
            handleRequest(HttpMethod.Get, "/tiles/762391.0/0/0/$notNumeric.png").apply {
                assertEquals(ContentType.Text.Plain.withParameter("charset", "UTF-8"), response.contentType())
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    private val testDemoEnv = createTestEnvironment {
        config = HoconApplicationConfig(appConfig
            .withValue("ktor.demo.enabled", ConfigValueFactory.fromAnyRef(true))
            .withValue("ktor.demo.depth", ConfigValueFactory.fromAnyRef(0))
        )
    }

    @Test
    fun testDemoRoot() {
        withApplication(testDemoEnv) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(ContentType("text", "html", listOf(HeaderValueParam("charset", "UTF-8"))), response.contentType())
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testDemoTile() {
        withApplication(testDemoEnv) {
            handleRequest(HttpMethod.Get, "/tiles/762391.0/0/1/0.png").apply {
                // TODO: Compare saved expected file to returned bytes.
                assertEquals(ContentType.Image.PNG, response.contentType())
                assertTrue((response.byteContent?.size ?: 0) > 0)
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testDemoTileNotFound() {
        withApplication(testDemoEnv) {
            val invalidDepth = 1
            handleRequest(HttpMethod.Get, "/tiles/762391.0/$invalidDepth/1/0.png").apply {
                assertEquals(ContentType.Text.Plain.withParameter("charset", "UTF-8"), response.contentType())
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}