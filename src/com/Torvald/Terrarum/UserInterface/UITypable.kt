package com.Torvald.Terrarum.UserInterface

/**
 * Created by minjaesong on 16-03-14.
 */
interface UITypable {
    fun keyPressed(key: Int, c: Char)

    fun keyReleased(key: Int, c: Char)
}