package com.example

class BallManager : ArrayList<BallPath>() {
    fun position(t: Float): Pair<Int, Int> {
        // Find BallPath for given t
        return find { t >= it.t0 && t < it.tf }?.position(t) ?: Pair(0, 0)
    }
}