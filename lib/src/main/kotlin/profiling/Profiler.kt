package com.eliottgray.kotlin.profiling

import com.eliottgray.kotlin.MapTile
import com.eliottgray.kotlin.MapTileKey
import com.eliottgray.kotlin.planet.FractalPlanet
import com.eliottgray.kotlin.planet.HexPlanet
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class Profiler(private val testCases: List<TestCase>) {

    suspend fun profileAll(repeatCount: Int = 5): List<ProfilingResult> {
        return testCases.map { testCase ->
            val hexTimes = mutableListOf<Long>()
            val fractalTimes = mutableListOf<Long>()
            repeat(repeatCount) {
                val seed = Random.nextDouble()
                val fractalMillis = measureTimeMillis {
                    profileAsync(testCase, seed, false)
                }
                val hexMillis = measureTimeMillis {
                    profileAsync(testCase, seed, true)
                }
                hexTimes.add(hexMillis)
                fractalTimes.add(fractalMillis)
            }
            // TODO: Use median, instead of mean? Median will avoid outliers impacting the results.
            ProfilingResult(
                testCase.name,
                fractalTimeMillis = fractalTimes.average(),
                hexTimeMillis = hexTimes.average()
            )
        }
    }

    private suspend fun profileAsync(testCase: TestCase, seed: Double, useHex: Boolean) = coroutineScope {
        println("Profiling ${testCase.name} - seed $seed - hex $useHex")
        val planet = if(useHex) {HexPlanet.get(seed, h3Resolution = 5)} else {FractalPlanet.get(seed)}

        val deferredResults: ArrayList<Deferred<MapTile>> = ArrayList()

        for (mapTileKey: MapTileKey in testCase.mapTileKeyList) {
            val result = async {
                planet.getMapTile(mapTileKey)
            }
            deferredResults.add(result)
        }

        for (deferred in deferredResults) {
            deferred.await()
        }
    }
}