package com.eliottgray.kotlin

fun main(args: Array<String>) {
    if (args.isEmpty()){
        println("Hello World!")
    } else {
        val joined = args.joinToString(separator = " ")
        println(joined)
    }
}