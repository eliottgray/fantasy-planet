package com.eliottgray.kotlin

import kotlin.math.PI
import kotlin.math.floor

object Utils {
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
}