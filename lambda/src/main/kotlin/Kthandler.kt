import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.eliottgray.kotlin.MapTileKey
import com.eliottgray.kotlin.planet.FractalPlanet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlin.system.measureTimeMillis

// Handler value: example.Handler
class Kthandler :
    RequestHandler<Map<String?, String?>, ByteArray> {
    var gson: Gson = GsonBuilder().setPrettyPrinting().create()

    override fun handleRequest(event: Map<String?, String?>, context: Context): ByteArray {
        val logger = context.logger

        var pngByteArray: ByteArray?
        val time = measureTimeMillis {
            val x = event["x"]!!.toInt()
            val y = event["y"]!!.toInt()
            val z = event["z"]!!.toInt()
            val seed = event["seed"]!!.toDouble()

            val mapTileKey = MapTileKey(z, x, y, seed)
            val planet = FractalPlanet.get(seed)
            val tile = planet.getMapTile(mapTileKey)
            pngByteArray = tile.pngByteArray
        }

        logger.log("ELAPSED TIME: $time")

//        val response = "200 OK"
        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()))
        logger.log("CONTEXT: " + gson.toJson(context))
        // process event
        logger.log("EVENT: " + gson.toJson(event))
        logger.log("EVENT TYPE: " + event.javaClass.toString())
        return pngByteArray!!
    }


}