package net.torvald.terrarum.modulebasegame.worldgenerator

import com.jme3.math.FastMath

/**
 * Quadratic polynomial
 * -(16/9) * (start-end)/height^2 * (x - 0.25 * height)^2 + start
 * 16/9: terrain is formed from 1/4 of height.
 * 1 - (1/4) = 3/4, reverse it and square it.
 * That makes 16/9.

 * Shape:

 * cavity                                 _
 * small
 * _
 * _
 * __
 * ____
 * cavity                 ________
 * large ________________

 * @param func_argX
 * *
 * @param start
 * *
 * @param end
 * *
 * @return
 * Created by minjaesong on 2016-03-31.
 */
object NoiseFilterMinusQuadratic : NoiseFilter {
    override fun getGrad(func_argX: Int, start: Double, end: Double): Double {
        val graph_gradient = -FastMath.pow(FastMath.sqr((1 - WorldGenerator.TERRAIN_AVERAGE_HEIGHT).toFloat()), -1f) * // 1/4 -> 3/4 -> 9/16 -> 16/9
                             (start - end) / FastMath.sqr(WorldGenerator.HEIGHT.toFloat()) *
                             FastMath.sqr((func_argX - WorldGenerator.TERRAIN_AVERAGE_HEIGHT).toFloat()) + start

        if (func_argX < WorldGenerator.TERRAIN_AVERAGE_HEIGHT) {
            return start
        } else if (func_argX >= WorldGenerator.HEIGHT) {
            return end
        } else {
            return graph_gradient
        }
    }
}