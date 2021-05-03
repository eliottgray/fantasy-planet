package com.eliottgray.kotlin
import kotlin.math.*

class CoordinateError(message:String): Exception(message)


class Point(val alt: Double, val x: Double, val y: Double, val z: Double, val seed: Double = Defaults.DEFAULT_SEED, val lat: Double? = null, val lon: Double? = null) {

    companion object{
        fun fromSpherical(lat: Double, lon: Double, alt: Double = 0.0, seed: Double = Defaults.DEFAULT_SEED): Point {
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
}



/*
    @staticmethod
    def from_spherical(lat: float, lon: float, alt: float = 0.0, seed: float = DEFAULT_SEED) -> 'Point':
        if not 90 >= lat >= -90:
            raise CoordinateError("Latitude {} encountered, which is not a valid coordinate.".format(str(lat)))
        if not 180 >= lon >= -180:
            raise CoordinateError("Longitude {} encountered, which is not a valid coordinate.".format(str(lat)))

        major_squared = wgs84.SEMI_MAJOR_AXIS ** 2
        minor_squared = wgs84.SEMI_MINOR_AXIS ** 2
        prime_vertical_radius_of_curvature = major_squared / np.sqrt(major_squared * (cosLat ** 2) + minor_squared * (sinLat ** 2))

        x = (prime_vertical_radius_of_curvature + alt) * cosLat * cosLon
        y = (prime_vertical_radius_of_curvature + alt) * cosLat * sinLon
        z = ((minor_squared / major_squared) * prime_vertical_radius_of_curvature + alt) * sinLat
        point = Point(lat=lat, lon=lon, alt=alt, x=x, y=y, z=z, seed=seed)
        return point

 */
/*
    def __init__(self, alt: float, x: float, y: float, z: float, seed: float = DEFAULT_SEED, lat: float = None, lon: float = None):
        self.lat = lat
        self.lon = lon
        self.alt = alt
        self.seed = seed
        self.x = x
        self.y = y
        self.z = z
        self.xyz = (x, y, z)
        if alt is None:
            raise ValueError("Altitude value is None.")
        if seed is None:
            raise ValueError("Seed value is None.")
        if x is None:
            raise ValueError("X value is None.")
        if y is None:
            raise ValueError("Y value is None.")
        if z is None:
            raise ValueError("Z value is None.")
 */