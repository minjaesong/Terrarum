package net.torvald.terrarum

import net.torvald.terrarum.gamecontroller.Key
import net.torvald.terrarum.virtualcomputer.computer.BaseTerrarumComputer
import net.torvald.terrarum.virtualcomputer.terminal.ColouredTextTerminal
import net.torvald.terrarum.virtualcomputer.terminal.SimpleTextTerminal
import net.torvald.terrarum.virtualcomputer.terminal.Teletype
import net.torvald.terrarum.virtualcomputer.terminal.TeletypeTerminal
import org.newdawn.slick.Color
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics
import org.newdawn.slick.Image
import org.newdawn.slick.state.BasicGameState
import org.newdawn.slick.state.StateBasedGame

/**
 * ComputerCraft/OpenComputers like-alike, just for fun!
 *
 * Created by minjaesong on 16-09-07.
 */
class StateVTTest : BasicGameState() {

    val vt = SimpleTextTerminal(SimpleTextTerminal.AMBER, 80, 25)
    val computerInside = BaseTerrarumComputer(vt)

    val vtUI = Image(vt.displayW, vt.displayH)


    init {
    }

    override fun init(container: GameContainer, game: StateBasedGame) {
        vt.openInput()
    }

    override fun update(container: GameContainer, game: StateBasedGame, delta: Int) {
        Terrarum.appgc.setTitle("VT — F: ${container.fps}" +
                                " — M: ${Terrarum.memInUse}M / ${Terrarum.totalVMMem}M")
        vt.update(container, delta)
        computerInside.update(container, delta)
    }

    override fun getID() = Terrarum.STATE_ID_TEST_TTY

    private val paperColour = Color(0xfffce6)

    override fun render(container: GameContainer, game: StateBasedGame, g: Graphics) {
        vt.render(container, vtUI.graphics)

        g.drawImage(vtUI,
                Terrarum.WIDTH.minus(vtUI.width).div(2f),
                Terrarum.HEIGHT.minus(vtUI.height).div(2f))

        vtUI.graphics.flush()
    }

    override fun keyPressed(key: Int, c: Char) {
        super.keyPressed(key, c)
        vt.keyPressed(key, c)

        if (key == Key.RETURN) {
            val input = vt.closeInput()

            computerInside.runCommand(input, "=prompt")

            vt.openInput()

            computerInside.runCommand("io.write(_COMPUTER.prompt)", "=prompt")
        }
    }
}