package net.torvald.terrarum.ui

import net.torvald.terrarum.gameactors.sqr

/**
 * Created by SKYHi14 on 2017-03-14.
 */
object UIUtils {
    fun moveQuick(start: Double, end: Double, timer: Double, duration: Double) =
            (start - end) * ((timer / duration) - 1).sqr() + end
}