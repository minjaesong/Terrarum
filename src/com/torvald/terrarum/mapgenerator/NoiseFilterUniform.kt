package com.torvald.terrarum.mapgenerator

/**
 * Created by minjaesong on 16-03-31.
 */
object NoiseFilterUniform : NoiseFilter {
    override fun getGrad(func_argX: Int, start: Float, end: Float): Float {
        return 1f
    }
}