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
            val fractalElevations = mutableListOf<SummarizedMapTiles>()
            repeat(repeatCount) {
                val seed = Random.nextDouble()
                val fractalMillis = measureTimeMillis {
                    val summarizedMapTiles = profileAsync(testCase, seed, false)
                    fractalElevations.add(summarizedMapTiles)
                }
                val hexMillis = measureTimeMillis {
                    profileAsync(testCase, seed, true)
                }
                hexTimes.add(hexMillis)
                fractalTimes.add(fractalMillis)
            }
            ProfilingResult(
                testCase.name,
                fractalTimeMillis = fractalTimes.map { it.toDouble() }.median(),
                hexTimeMillis = hexTimes.map { it.toDouble() }.median(),
                medianMaxElev = fractalElevations.map { it.averageMaximumAlt }.median(),
                medianMinElev = fractalElevations.map { it.averageMinimumAlt }.median()
            )
        }
    }

    private suspend fun profileAsync(testCase: TestCase, seed: Double, useHex: Boolean): SummarizedMapTiles {
        return coroutineScope {
            println("Profiling ${testCase.name} - seed $seed - hex $useHex")
            val planet = if(useHex) {HexPlanet(seed, h3Resolution = 5)} else {FractalPlanet(seed)}

            val deferredResults: ArrayList<Deferred<MapTile>> = ArrayList()

            for (mapTileKey: MapTileKey in testCase.mapTileKeyList) {
                val result = async {
                    // Have to copy the seed because the pre-generated MapTileKey contains an unknown seed.
                    val customTestTile = mapTileKey.copy(seed = seed)
                    planet.getMapTile(customTestTile)
                }
                deferredResults.add(result)
            }

            val maximumAlts = mutableListOf<Double>()
            val minimumAlts = mutableListOf<Double>()

            for (deferred in deferredResults) {
                val mapTile = deferred.await()
                maximumAlts.add(mapTile.maxElev)
                minimumAlts.add(mapTile.minElev)
            }

            SummarizedMapTiles(averageMaximumAlt = maximumAlts.median(), averageMinimumAlt = minimumAlts.median())
        }
    }

    private fun List<Double>.median(): Double {
        return if (this.size % 2 != 0) {
            val index = this.size / 2  // Division operator is a floor; 3 / 2 = 1
            this[index]
        } else {
            val a = this.size / 2
            val b = a - 1
            val one = this[a]
            val two = this[b]
            (one + two) / 2
        }
    }
}