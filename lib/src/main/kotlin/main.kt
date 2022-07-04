package com.eliottgray.kotlin

import com.eliottgray.kotlin.profiling.Profiler
import com.eliottgray.kotlin.profiling.TestCase
import kotlinx.coroutines.*

fun main() = runBlocking() {
    // TODO: Move profiling to test package... or to separate app... etc. This is only here because it was expedient.
    val loadOnly = TestCase.fromFile("/profiling/load_globe")
    val profiler = Profiler(listOf(loadOnly))
    val results = profiler.profileAll(20)
    results.forEach{
        println(it)
    }
}
