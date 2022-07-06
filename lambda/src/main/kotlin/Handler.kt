import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.eliottgray.kotlin.MapTileKey
import com.eliottgray.kotlin.planet.FractalPlanet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.*
import kotlin.system.measureTimeMillis

// Handler value: example.Handler
class Handler :
    RequestHandler<Map<String?, Any?>, APIGatewayV2HTTPResponse> {
    var gson: Gson = GsonBuilder().create()

    override fun handleRequest(event: Map<String?, Any?>, context: Context): APIGatewayV2HTTPResponse {
        val logger = context.logger
        val pathParameters = gson.toJsonTree(event).asJsonObject["pathParameters"].asJsonObject

        var pngByteArray: ByteArray?
        val time = measureTimeMillis {
            val x = pathParameters["x"].asInt
            val y = pathParameters["y"].asInt
            val z = pathParameters["z"].asInt
            val seed = pathParameters["seed"].asDouble
            val mapTileKey = MapTileKey(z, x, y, seed)
            val planet = FractalPlanet(seed)
            val tile = planet.getMapTile(mapTileKey)
            pngByteArray = tile.pngByteArray
        }

        logger.log("ELAPSED TIME: $time")

        // log execution details
//        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()))
//        logger.log("CONTEXT: " + gson.toJson(context))
//        logger.log("EVENT: " + gson.toJson(event))
//        logger.log("EVENT TYPE: " + event.javaClass.toString())
        // TODO: Handle invalid cases with an appropriate response, e.g. invalid tile parameters -> code 400
        return buildResponse(pngByteArray!!)
    }

    private fun buildResponse(byteArray: ByteArray): APIGatewayV2HTTPResponse {
        val body = Base64.getEncoder().encodeToString(byteArray)
        val headers = mapOf("content-type" to "image/png")
        return APIGatewayV2HTTPResponse
            .builder()
            .withHeaders(headers)
            .withStatusCode(200)
            .withBody(body)
            .withIsBase64Encoded(true)
            .build()
    }
}