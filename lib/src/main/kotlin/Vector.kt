package com.eliottgray.kotlin

data class Vector (val x: Double, val y: Double, val z: Double){

    companion object {
        fun fromPoints(start: Point, end: Point): Vector {
            return Vector(
                x = end.x - start.x,
                y = end.y - start.y,
                z = end.z - start.z
            )
        }
    }

    fun crossProduct(vector: Vector): Vector {
        return Vector(
            x = this.y * vector.z - this.z * vector.y,
            y = this.z * vector.x - this.x * vector.z,
            z = this.x * vector.y - this.y * vector.x
        )
    }

    fun dotProduct(vector: Vector): Double {
        return this.x * vector.x + this.y * vector.y + this.z * vector.z
    }
}