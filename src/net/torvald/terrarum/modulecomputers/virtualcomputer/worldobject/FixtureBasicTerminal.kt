package net.torvald.terrarum.modulecomputers.virtualcomputer.worldobject

import com.badlogic.gdx.graphics.Color
import net.torvald.terrarum.modulebasegame.gameactors.BlockBox
import net.torvald.terrarum.modulebasegame.gameactors.FixtureBase

/**
 * Created by minjaesong on 2016-09-08.
 */
class FixtureBasicTerminal(phosphor: Color) : FixtureBase(BlockBox(BlockBox.ALLOW_MOVE_DOWN, 1, 1)) {

    /*val computer = TerrarumComputer(8)
    val vt: Terminal = SimpleTextTerminal(phosphor, 80, 25, computer)
    val ui = UITextTerminal(vt)

    init {
        computer.attachTerminal(vt)

        collisionFlag = COLLISION_PLATFORM

        actorValue[AVKey.UUID] = UUID.randomUUID().toString()
    }*/

}