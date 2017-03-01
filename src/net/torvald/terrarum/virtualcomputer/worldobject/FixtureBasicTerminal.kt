package net.torvald.terrarum.virtualcomputer.worldobject

import net.torvald.terrarum.gameactors.AVKey
import net.torvald.terrarum.gameactors.FixtureBase
import net.torvald.terrarum.virtualcomputer.computer.TerrarumComputer
import net.torvald.terrarum.virtualcomputer.terminal.SimpleTextTerminal
import net.torvald.terrarum.virtualcomputer.terminal.Terminal
import net.torvald.terrarum.virtualcomputer.worldobject.ui.UITextTerminal
import org.newdawn.slick.Color
import java.util.*

/**
 * Created by minjaesong on 16-09-08.
 */
class FixtureBasicTerminal(phosphor: Color) : FixtureBase() {

    val computer = TerrarumComputer(8)
    val vt: Terminal = SimpleTextTerminal(phosphor, 80, 25, computer)
    val ui = UITextTerminal(vt)

    init {
        computer.attachTerminal(vt)

        collisionFlag = COLLISION_PLATFORM

        actorValue[AVKey.UUID] = UUID.randomUUID().toString()
    }

}