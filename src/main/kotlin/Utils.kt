package com.eliottgray.kotlin

import kotlin.math.*

fun mutateSeed(a: Double, b: Double): Double {
    // Returns a Double of range -1.0 to 1.0, inclusive.
    val r = (a + PI) * (b + PI)
    val strippedWholeNumber = r - floor(r)
    return 2.0 * strippedWholeNumber - 1.0  // Number should be from -1..1, not 0..1.
}

fun squishSeed(seed: Double): Double{
    // Because seed mutation has issues with very large numbers (Floor/toInt does not seem to properly work with exponential notation)
    // it is necessary to be able to move the decimal to the left until the number is small enough.
    var result = seed
    while (result > 100000){
        result *= 0.1
    }
    return result
}


fun sphericalToECEF(lat: Double, lon: Double, alt: Double): Triple<Double, Double, Double> {
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
    return Triple(x, y, z)
}