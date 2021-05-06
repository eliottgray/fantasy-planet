package com.eliottgray.kotlin
import kotlin.math.*


class Point(var alt: Double = Defaults.ALTITUDE_METERS, val x: Double, val y: Double, val z: Double, val seed: Double = Defaults.SEED, val lat: Double = 0.0, val lon: Double = 0.0) {

    companion object{
        fun fromSpherical(lat: Double, lon: Double, alt: Double = 0.0, seed: Double = Defaults.SEED): Point {
            if (lat > 90 || lat < -90){
                throw CoordinateError("Invalid latitude encountered: $lat")
            }
            if (lon > 180 || lon < -180){
                throw CoordinateError("Invalid longitude encountered: $lon")
            }
            val radLat = lat * (PI/180) // TODO: does compilation optimize away multiple divisions of PI?
            val radLon = lon * (PI/180)
            val cosLat = cos(radLat)
            val sinLat = sin(radLat)
            val cosLon = cos(radLon)
            val sinLon = sin(radLon)
            val majorSquared = WGS84.SEMI_MAJOR_AXIS.pow(2)
            val minorSquared = WGS84.SEMI_MINOR_AXIS.pow(2)
            val primeVerticalRadiusOfCurvature = majorSquared / sqrt(majorSquared * cosLat.pow(2) + minorSquared * sinLat.pow(2))

            val x = (primeVerticalRadiusOfCurvature + alt) * cosLat * cosLon
            val y = (primeVerticalRadiusOfCurvature + alt) * cosLat * sinLon
            val z = ((minorSquared / majorSquared) * primeVerticalRadiusOfCurvature + alt) * sinLat

            return Point(alt=alt, x=x, y=y, z=z, seed=seed, lat=lat, lon=lon)
        }

    }

    fun rotateAroundXAxis(degrees: Double): Point {
        val radians = degrees * (PI/180)
        val sinRad = sin(radians)
        val cosRad = cos(radians)
        val newY = this.y * cosRad - this.z * sinRad
        val newZ = this.z * cosRad + this.y * sinRad
        return Point(x=this.x, y=newY, z=newZ, alt=this.alt, seed=this.seed)
    }

    fun rotateAroundYAxis(degrees: Double): Point {
        val radians = degrees * (PI/180)
        val sinRad = sin(radians)
        val cosRad = cos(radians)
        val newX = this.x * cosRad + this.z * sinRad
        val newZ = -sinRad * this.x + this.z * cosRad
        return Point(x=newX, y=this.y, z=newZ, alt=this.alt, seed=this.seed)
    }

    fun copy(): Point {
        return Point(lat=this.lat, lon=this.lon, alt=this.alt, seed=this.seed, x=this.x, y=this.y, z=this.z)
    }

    fun distance(other: Point): Double {
        val xSquared = (this.x - other.x).pow(2)
        val ySquared = (this.y - other.y).pow(2)
        val zSquared = (this.z - other.z).pow(2)
        return sqrt(xSquared + ySquared + zSquared)
    }

    fun midpoint(other: Point, length: Double): Point {
        val newSeed = Utils.mutateSeed(this.seed, other.seed)
        val altWeight = 0.45
        val altPow = 1.0
        val lengthWeight = 0.035
        val lengthPow = 0.47
        val x = (this.x + other.x) / 2
        val y = (this.y + other.y) / 2
        val z = (this.z + other.z) / 2
        val alt = (this.alt + other.alt) / 2 + newSeed * altWeight * (this.alt - other.alt).absoluteValue.pow(altPow) + newSeed * lengthWeight * length.pow(lengthPow)
        return Point(x=x, y=y, z=z, alt=alt, seed=newSeed)
    }

    override fun toString(): String {
        return "Point(x=$x, y=$y, z=$z, alt=$alt, seed=$seed, lat=$lat, lon=$lon)"
    }
}
