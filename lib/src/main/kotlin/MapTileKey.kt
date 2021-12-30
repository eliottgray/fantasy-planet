package com.eliottgray.kotlin

import kotlin.math.pow

data class MapTileKey (val z: Int, val x: Int, val y: Int, val seed: Double = Defaults.SEED) {
    fun isValid(): Boolean {
        return zIsValid() && xIsValid() && yIzValid()
    }

    private fun zIsValid(): Boolean {
        return z >= 0
    }

    private fun xIsValid(): Boolean {
        return x in 0 until (2.0.pow(z) * 2).toInt()
    }

    private fun yIzValid(): Boolean {
        return y in 0 until 2.0.pow(z).toInt()
    }
}