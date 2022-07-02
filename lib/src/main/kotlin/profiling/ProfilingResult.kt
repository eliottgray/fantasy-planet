package com.eliottgray.kotlin.profiling

data class ProfilingResult(
    val testName: String,
    val fractalTimeMillis: Double,
    val hexTimeMillis: Double,
    val medianMaxElev: Double,
    val medianMinElev: Double
)