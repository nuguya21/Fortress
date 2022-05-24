package com.github.nuguya21.fortress.util

import org.bukkit.Location
import org.bukkit.util.Vector

/**
 * m(g) - mess
 * N - newton
 */
class Movement
    (
    origin: Location,
    private val m: Double,
    private val vector: Vector,
    private val N: Double
) {

    companion object {
        const val G = 9.80665
        private const val frame: Int = 80
    }

    private var now: Location = origin.clone()
    private var previous: Location = now.clone()
    private var g: Double = G

    init {
        vector.apply {
            divide(Vector(length(), length(), length()))
            multiply((N / m) / frame)
        }
    }

    fun nextTick(): Location {
        val nowVelocity = getVelocity().clone()
        var newNow = now.clone().add(nowVelocity)
        previous = now.clone()
        for (i in 1..(frame * 10)) {
            val checkLocation = previous.clone().add(nowVelocity.clone().multiply(i / (frame * 10)))
            if (checkLocation.block.isSolid) {
                newNow = previous.clone().add(nowVelocity.clone().multiply((i - 1) / (frame * 10)))
                g = G
                break
            }
        }
        now = newNow
        return now
    }

    fun deltaVector(): Vector {
        return now.clone().subtract(previous).toVector()
    }

    private fun getVelocity(): Vector {
        return vector.subtract(Vector(0.0, (g / frame), 0.0)).also { g += (G / frame) }
    }
}