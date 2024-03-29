package com.eliottgray.kotlin
import kotlin.math.*

data class Point(
    val x: Double,
    val y: Double,
    val z: Double,
    val seed: Double = Defaults.SEED, // TODO: Restrict this property to a Tetrahedral Point subclass.
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val alt: Double = 0.0,
    val resolution: Int = Defaults.RESOLUTION_METERS,  // TODO: Restrict this property to a Map Tile Point subclass.
    val h3Index: Long? = null  // TODO: Restrict this property to a Map Tile Point subclass.
) {

    init {
        if (this.resolution <= 0){
            throw PointError("Illegal resolution encountered: $resolution; must be a positive and non-zero Integer.")
        }
    }

    companion object{
        fun fromSpherical(
            lat: Double,
            lon: Double,
            initialAlt: Double = 0.0,
            seed: Double = Defaults.SEED,
            altSeed: Double = 0.0,
            resolution: Int = Defaults.RESOLUTION_METERS,
            h3Index: Long? = null
        ): Point {
            val (x, y, z) = sphericalToECEF(lat = lat, lon = lon, alt = initialAlt)
            return Point(alt = altSeed, x = x, y = y, z = z, seed = seed, lat = lat, lon = lon, resolution = resolution, h3Index = h3Index)
        }
    }

    fun rotateAroundXAxis(degrees: Double): Point {
        val radians = degrees * (PI/180)
        val sinRad = sin(radians)
        val cosRad = cos(radians)
        val newY = this.y * cosRad - this.z * sinRad
        val newZ = this.z * cosRad + this.y * sinRad
        return Point(x = this.x, y = newY, z = newZ, alt = this.alt, seed = this.seed)
    }

    fun rotateAroundYAxis(degrees: Double): Point {
        val radians = degrees * (PI/180)
        val sinRad = sin(radians)
        val cosRad = cos(radians)
        val newX = this.x * cosRad + this.z * sinRad
        val newZ = -sinRad * this.x + this.z * cosRad
        return Point(x = newX, y = this.y, z = newZ, alt = this.alt, seed = this.seed)
    }

    fun distance(other: Point): Double {
        val xSquared = (this.x - other.x).pow(2)
        val ySquared = (this.y - other.y).pow(2)
        val zSquared = (this.z - other.z).pow(2)
        return sqrt(xSquared + ySquared + zSquared)
    }

    fun midpoint(other: Point, length: Double): Point {
        val newSeed = mutateSeed(this.seed, other.seed)
        val seedTwo = mutateSeed(newSeed, newSeed)

        val cutOne = 0.5 + 0.1 * mutateSeed(seedTwo, seedTwo)
        val cutTwo = 1 - cutOne

        val x: Double
        val y: Double
        val z: Double
        when {
            this.seed < other.seed -> {
                x = cutOne * this.x + cutTwo * other.x
                y = cutOne * this.y + cutTwo * other.y
                z = cutOne * this.z + cutTwo * other.z
            }
            this.seed > other.seed -> {
                x = cutTwo * this.x + cutOne * other.x
                y = cutTwo * this.y + cutOne * other.y
                z = cutTwo * this.z + cutOne * other.z
            }
            else -> {
                // Without a way of ordering, an equal split is required.
                x = (this.x + other.x) / 2
                y = (this.y + other.y) / 2
                z = (this.z + other.z) / 2
            }
        }

        val altWeight = 0.45
        val altPow = 1.0
        val lengthWeight = 0.65
        val lengthPow = 0.47
        val alt = (this.alt + other.alt) / 2 + newSeed * altWeight * (this.alt - other.alt).absoluteValue.pow(altPow) + newSeed * lengthWeight * length.pow(lengthPow)
        return Point(x = x, y = y, z = z, alt = alt, seed = newSeed)
    }
}
