package com.Torvald.Terrarum.ConsoleCommand

/**
 * Created by minjaesong on 16-01-15.
 */
interface ConsoleCommand {

    @Throws(Exception::class)
    fun execute(args: Array<String>)

    fun printUsage()

}