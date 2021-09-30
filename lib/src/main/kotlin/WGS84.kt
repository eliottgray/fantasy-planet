package com.eliottgray.kotlin

/*
Constants which define the World Geodetic System 1984 model.
See: https://en.wikipedia.org/wiki/World_Geodetic_System
*/


object WGS84 {
    const val SEMI_MAJOR_AXIS = 6378137.0
    const val FLATTENING = 1.0/298.257223563
    const val SEMI_MINOR_AXIS = SEMI_MAJOR_AXIS * (1.0 - FLATTENING)
}
