package com.eliottgray.kotlin

import com.eliottgray.kotlin.profiling.Profiler
import com.eliottgray.kotlin.profiling.TestCase
import kotlinx.coroutines.*

fun main() = runBlocking() {
    // TODO: Move profiling to test package... or to separate app... etc. This is only here because it was expedient.
    val loadOnly = TestCase.fromFile("/profiling/load_globe")
    val zoomTo8 = TestCase.fromFile("/profiling/zoom_in_to_level_8")
    val profiler = Profiler(listOf(loadOnly, zoomTo8))
    val results = profiler.profileAll(3)
    results.forEach{
        println(it)
    }
}
