package net.torvald.terrarum.virtualcomputer.luaapi

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaFunction
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameworld.WorldTime
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ZeroArgFunction
import java.util.*

/**
 * Implementation of lua's os.date, to return world info of the game world.
 *
 * Created by minjaesong on 16-09-28.
 */
class WorldInformationProvider(globals: Globals) {

    init {
        globals["os"]["time"] = LuaValue.NIL // history is LONG! Our 32-bit Lua's epoch is destined to break down...
        globals["os"]["date"] = OsDateImpl()
    }

    companion object {
        fun getWorldTimeInLuaFormat() : LuaTable {
            val t = LuaTable()
            if (Terrarum.gameStarted) {
                val time = Terrarum.ingame.world.time

                // int Terrarum World Time format
                t["hour"] = time.hours
                t["min"] = time.minutes
                t["wday"] = time.dayOfWeek
                t["year"] = time.years
                t["yday"] = time.yearlyDays
                t["month"] = time.months
                t["sec"] = time.seconds
                t["day"] = time.days
            }
            else {
                t["hour"] = 0
                t["min"] = 0
                t["wday"] = 1
                t["year"] = 0
                t["yday"] = 1
                t["month"] = 1
                t["sec"] = 0
                t["day"] = 1
            }

            return t
        }

        val defaultDateFormat = "%a %d %B %Y %X"

        /** evaluate single C date format */
        fun String.evalAsDate(): String {
            if (Terrarum.gameStarted) {
                val time = Terrarum.ingame.world.time
                return when (this) {
                    "%a" -> time.getDayNameShort()
                    "%A" -> time.getDayNameFull()
                    "%b" -> time.getMonthNameShort()
                    "%B" -> time.getMonthNameFull()
                    "%c" -> "%x".evalAsDate() + " " + "%X".evalAsDate()
                    "%d" -> time.days.toString()
                    "%H" -> time.hours.toString()
                    "%I" -> throw IllegalArgumentException("%I: AM/PM concept does not exists.")
                    "%M" -> time.minutes.toString()
                    "%m" -> time.months.toString()
                    "%p" -> throw IllegalArgumentException("%p: AM/PM concept does not exists.")
                    "%S" -> time.seconds.toString()
                    "%w" -> time.dayOfWeek.toString()
                    "%x" -> "${String.format("%02d", time.years)}-${String.format("%02d", time.months)}-${String.format("%02d", time.days)}"
                    "%X" -> "${String.format("%02d", time.hours)}:${String.format("%02d", time.minutes)}:${String.format("%02d", time.seconds)}"
                    "%Y" -> time.years.toString()
                    "%y" -> time.years.mod(100).toString()
                    "%%" -> "%"
                    else -> throw IllegalArgumentException("Unknown format string: $this")
                }
            }
            else {
                return when (this) {
                    "%a" -> "---"
                    "%A" -> "------"
                    "%b" -> "----"
                    "%B" -> "--------"
                    "%c" -> "%x".evalAsDate() + " " + "%X".evalAsDate()
                    "%d" -> "0"
                    "%H" -> "0"
                    "%I" -> throw IllegalArgumentException("%I: AM/PM concept does not exists.")
                    "%M" -> "0"
                    "%m" -> "0"
                    "%p" -> throw IllegalArgumentException("%p: AM/PM concept does not exists.")
                    "%S" -> "0"
                    "%w" -> "0"
                    "%x" -> "00-00-00"
                    "%X" -> "00:00:00"
                    "%Y" -> "0"
                    "%y" -> "00"
                    "%%" -> "%"
                    else -> throw IllegalArgumentException("Unknown format string: $this")
                }
            }
        }

        val acceptedDateFormats = arrayOf("%a", "%A", "%b", "%B", "%c", "%d", "%H", "%I", "%M", "%m", "%p", "%S", "%w", "%x", "%X", "%Y", "%y", "%%" )
    }

    /**
     * Changes: cannot get a representation of arbitrary time.
     */
    class OsDateImpl() : LuaFunction() {
        // no args
        override fun call(): LuaValue {
            return call(defaultDateFormat)
        }

        override fun call(format: LuaValue): LuaValue {
            var arg = format.checkjstring()
            acceptedDateFormats.forEach {
                if (arg.contains(it))
                    arg = arg.replace(it, it.evalAsDate(), ignoreCase = false)
            }
            return LuaValue.valueOf(arg)
        }


    }

}