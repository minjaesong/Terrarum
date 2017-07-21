package net.torvald.terrarum.virtualcomputer.worldobject

import net.torvald.terrarum.gameactors.FixtureBase
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.virtualcomputer.computer.TerrarumComputer
import net.torvald.terrarum.virtualcomputer.terminal.Terminal
import java.io.PrintStream
import java.security.SecureRandom
import java.util.*

/**
 * Created by minjaesong on 16-09-08.
 */
open class FixtureComputerBase(world: GameWorld) : FixtureBase(world) {

    /** Connected terminal */
    var terminal: FixtureBasicTerminal? = null

    var computerInside: TerrarumComputer? = null

    init {
        // UUID of the "brain"
        actorValue["computerid"] = "none"


        collisionFlag = COLLISION_PLATFORM
    }

    ////////////////////////////////////
    // get the computer actually work //
    ////////////////////////////////////

    fun attachTerminal(uuid: String) {
        val fetchedTerminal = getTerminalByUUID(uuid)
        computerInside = TerrarumComputer(8)
        computerInside!!.attachTerminal(fetchedTerminal!!)
        actorValue["computerid"] = computerInside!!.UUID
    }

    fun detatchTerminal() {
        terminal = null
    }

    private fun getTerminalByUUID(uuid: String): Terminal? {
        TODO("get terminal by UUID. Return null if not found")
    }



    ////////////////
    // game codes //
    ////////////////

    override fun update(delta: Float) {
        super.update(delta)
        if (terminal != null) terminal!!.update(delta)
    }

    fun keyPressed(key: Int, c: Char) {
        /*if (terminal != null) {
            terminal!!.vt.keyPressed(key, c)
            computerInside!!.keyPressed(key, c)
        }*/
    }
}