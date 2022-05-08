package com.eliottgray.kotlin

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class Planet constructor(val seed: Double = Defaults.SEED){
    private val squishedSeed = squishSeed(seed)
    private val tetra = Tetrahedron.buildDefault(squishedSeed)
    private var elevations: MapTileElevations

    init {
        // We need to know what the points are for the top tiles BEFORE they go in the cache, because their min/max
        // elevations are used to determine map colors/etc. for all other tiles, to ensure consistency.
        // For now we also don't want to make the full tile objects, because doing so incurs additional work USING
        // the global elevation data we don't have yet.
        val topTileOneKey = MapTileKey(0, 0, 0, seed)
        val topTileTwoKey = MapTileKey(0, 1, 0, seed)
        // TODO: calculate top tile points asynchronously, to speed up building the planet.
        val topTileOnePoints = calculateMapTilePoints(topTileOneKey)
        val topTileTwoPoints = calculateMapTilePoints(topTileTwoKey)
        val minElevation = min(
            topTileOnePoints.minByOrNull { it.alt }?.alt ?: 0.0,
            topTileTwoPoints.minByOrNull { it.alt }?.alt ?: 0.0
        )
        val maxElevation = max(
            topTileOnePoints.maxByOrNull { it.alt }?.alt ?: 0.0,
            topTileTwoPoints.maxByOrNull { it.alt }?.alt ?: 0.0
        )
        elevations = MapTileElevations(minElevation = minElevation, maxElevation = maxElevation)
        val topTileOne = MapTile(topTileOneKey, topTileOnePoints, elevations)
        val topTileTwo = MapTile(topTileTwoKey, topTileTwoPoints, elevations)
        mapTileCache.put(topTileOneKey, CompletableFuture.completedFuture(topTileOne))
        mapTileCache.put(topTileTwoKey, CompletableFuture.completedFuture(topTileTwo))
    }
    companion object {

        private val mapTileCache: AsyncCache<MapTileKey, MapTile> = Caffeine.newBuilder()
            .maximumSize(10000)
            .buildAsync()

        private val planetCache: AsyncCache<Double, Planet> = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofMinutes(60))
            .buildAsync()

        fun get(seed: Double): Planet {
            return planetCache.get(seed) { it -> Planet(it) }.get()!!
        }

        private fun MutableList<Point>.partitionInPlaceBy(compareFunc: (Point) -> Boolean): Int {
            // Sorts all records that return TRUE by the comparator to the front of the list, and
            //   returns the index of the first record that returns FALSE.
            var pointerOne = 0
            var pointerTwo = this.size - 1
            while (true) {
                while (pointerOne < this.size && compareFunc(this[pointerOne])) {
                    pointerOne++
                }
                while (pointerTwo >= 0 && !compareFunc(this[pointerTwo])) {
                    pointerTwo--
                }
                if (pointerOne >= pointerTwo) {
                    break
                }
                // Using .also{} to swap! https://stackoverflow.com/questions/45377802/swap-function-in-kotlin
                this[pointerOne] = this[pointerTwo].also { this[pointerTwo] = this[pointerOne] }
            }
            return pointerOne
        }
    }
    fun getMapTile(mapTileKey: MapTileKey): MapTile {
        return mapTileCache.get(mapTileKey) { key -> buildMapTile(key, elevations) }.get()!!
    }

    fun getElevationAt(lat: Double, lon: Double, resolution: Int): Point {
        val point = Point.fromSpherical(lat = lat, lon = lon, resolution = resolution)
        var current = this.tetra
        var subdivisions = 0
        while (current.longestSide > resolution){
            subdivisions += 1
            val (subOne, subTwo) = current.subdivide()
            current = if (subOne.contains(point)){
                subOne
            } else {
                assert(subTwo.contains(point))
                subTwo
            }
        }
        return point.copy(alt=current.averageAltitude)
    }

    fun getMultipleElevations(points: MutableList<Point>, current: Tetrahedron = this.tetra): MutableList<Point> {
        if (points.isEmpty()){
            return points
        }

        // Since some points may not require any further recursion, we can filter them out and set elevation.
        val doneIndex: Int = points.partitionInPlaceBy { point ->  current.longestSide <= point.resolution }
        for (i in 0 until doneIndex) {
            points[i] = points[i].copy(alt=current.averageAltitude)
        }

        // If we've identified all points as done, no need to subdivide and recurse.
        if (doneIndex == points.size) {
            return points
        }

        // All remaining points must be sorted into the tetrahedron they are contained within.
        val (leftTetra, rightTetra) = current.subdivide()

        val pendingPoints = points.subList(doneIndex, points.size)
        val containmentIndex = pendingPoints.partitionInPlaceBy { point ->
            leftTetra.contains(point)
        }

        val leftPoints = pendingPoints.subList(0, containmentIndex)
        val rightPoints = pendingPoints.subList(containmentIndex, pendingPoints.size)

        getMultipleElevations(leftPoints, leftTetra)
        getMultipleElevations(rightPoints, rightTetra)
        return points
    }

    private fun buildMapTile(mapTileKey: MapTileKey, elevations: MapTileElevations): MapTile {
        val pointsWithElevations = calculateMapTilePoints(mapTileKey)
        return MapTile(mapTileKey, pointsWithElevations, elevations)
    }
    private fun calculateMapTilePoints(mapTileKey: MapTileKey): MutableList<Point> {
        val allPoints = ArrayList<Point>()

        val tileBounds: MapTileBounds = MapTileBounds.fromGeographicTileXYZ(mapTileKey.z, mapTileKey.x, mapTileKey.y)
        val lonDelta = (tileBounds.east - tileBounds.west) / MapTile.TILE_SIZE
        val latDelta = (tileBounds.north - tileBounds.south) / MapTile.TILE_SIZE
        var currentLat = tileBounds.north
        while (currentLat > tileBounds.south) {

            // It is necessary to determine the appropriate depth to calculate, as the length of a degree of longitude
            // varies by latitude. Do this once for each discrete latitude in the tile.
            val widthOfPixelMeters = MapTile.longitudinalWidthOfPixelMeters(currentLat, lonDelta)

            var currentLon = tileBounds.west
            while (currentLon < tileBounds.east) {
                allPoints.add(
                    Point.fromSpherical(
                        lat = currentLat,
                        lon = currentLon,
                        resolution = ceil(widthOfPixelMeters * 0.6).toInt()
                    )
                )
                currentLon += lonDelta
            }
            currentLat -= latDelta
        }
        assert(allPoints.size == MapTile.TILE_SIZE * MapTile.TILE_SIZE)
        return getMultipleElevations(allPoints)
    }

}
