
import com.eliottgray.kotlin.module
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(ContentType("text", "html", listOf(HeaderValueParam("charset", "UTF-8"))), response.contentType())
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testTile() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/tiles/762391.0/2/1/1.png").apply {
                // TODO: Compare saved expected file to returned bytes.
                assertEquals(ContentType.Image.PNG, response.contentType())
                assertTrue((response.byteContent?.size ?: 0) > 0)
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}