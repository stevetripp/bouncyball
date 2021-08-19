package com.example

import kotlin.math.pow

class BallPath(val x0: Int, val t0: Float, val xf: Int, val tf: Float) {

    private val h = 200
    private val t = tf - t0
    private val v = (xf - x0) / t

    fun xPos(t: Float): Int {
        return when {
            t < t0 -> x0
            t > tf -> xf
            else -> (x0 + (v * (t - t0))).toInt()
        }
    }

    fun yPos(t: Float): Int {
        val x = (xPos(t) - x0).toFloat()
        val m = (xf - x0) / 2F

        return ((-1 * h * x.pow(2)) / m.pow(2) + (2 * h * x) / m).toInt()
    }

    fun position(t: Float): Pair<Int, Int> = Pair(xPos(t), yPos(t))

    override fun toString(): String = "[x0: $x0  t0: $t0  xf: $xf  tf: $tf  v: $v]"
}