package net.torvald.terrarum.gameactors

import net.torvald.terrarum.gameactors.ActorHumanoid
import net.torvald.terrarum.gameactors.ai.AILuaAPI
import net.torvald.terrarum.gameactors.ai.ActorAI
import net.torvald.terrarum.gameitem.EquipPosition
import net.torvald.terrarum.gameitem.InventoryItem
import org.luaj.vm2.*
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.*
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib
import org.luaj.vm2.lib.jse.JsePlatform
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Input
import java.io.InputStreamReader
import java.io.Reader

/**
 * @param ai AI class. Use LuaAIWrapper for Lua script
 *
 * Created by minjaesong on 16-01-31.
 */
open class HumanoidNPC(
        override val ai: ActorAI, // it's there for written-in-Kotlin, "hard-wired" AIs
        born: GameDate
) : ActorHumanoid(born), AIControlled, CanBeAnItem {

    companion object {
        val DEFAULT_COLLISION_TYPE = ActorWithSprite.COLLISION_DYNAMIC
    }

    init {
        collisionType = DEFAULT_COLLISION_TYPE
    }

    // we're having InventoryItem data so that this class could be somewhat universal
    override var itemData: InventoryItem = object : InventoryItem() {
        override var id = referenceID

        override var baseMass: Double
            get() = actorValue.getAsDouble(AVKey.BASEMASS)!!
            set(value) { actorValue[AVKey.BASEMASS] = value }
        override var baseToolSize: Double? = 0.0
        override var scale: Double
            get() = actorValue.getAsDouble(AVKey.SCALE)!!
            set(value) {
                actorValue[AVKey.SCALE] = value
            }

        override fun secondaryUse(gc: GameContainer, delta: Int) {
            // TODO place this Actor to the world
        }
    }

    override fun getItemWeight(): Double {
        return mass
    }

    override fun stopUpdateAndDraw() {
        isUpdate = false
        isVisible = false
    }

    override fun resumeUpdateAndDraw() {
        isUpdate = true
        isVisible = true
    }

    override fun update(gc: GameContainer, delta: Int) {
        super.update(gc, delta)
        ai.update(delta)
    }

    override fun moveLeft(amount: Float) { // hit the buttons on the controller box
        axisX = -amount
    }

    override fun moveRight(amount: Float) { // hit the buttons on the controller box
        axisX = amount
    }

    override fun moveUp(amount: Float) { // hit the buttons on the controller box
        axisY = -amount
    }

    override fun moveDown(amount: Float) { // hit the buttons on the controller box
        axisY = amount
    }

    override fun moveJump(amount: Float) { // hit the buttons on the controller box
        isJumpDown = true
    }

    /** fly toward arbitrary angle  WARNING: the map is looped! */
    override fun moveTo(bearing: Double) {
        // if your NPC should fly, override this
        throw UnsupportedOperationException("Humans cannot fly :p")
    }

    /** fly toward arbitrary coord  WARNING: the map is looped! */
    override fun moveTo(toX: Double, toY: Double) {
        // if your NPC should fly, override this
        throw UnsupportedOperationException("Humans cannot fly :p")
    }
}