package net.torvald.terrarum.gameactors

import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameactors.ActorWithPhysics.Companion.SI_TO_GAME_ACC
import net.torvald.terrarum.worlddrawer.FeaturesDrawer.TILE_SIZE
import net.torvald.terrarum.blockproperties.Block
import net.torvald.terrarum.blockproperties.BlockCodex
import org.dyn4j.geometry.Vector2
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics
import org.newdawn.slick.Image

/**
 * Actors with static sprites and very simple physics
 *
 * Created by minjaesong on 2017-01-20.
 */
open class ParticleBase(renderOrder: Actor.RenderOrder, maxLifeTime: Int? = null) : Runnable {

    /** Will NOT actually delete from the CircularArray */
    @Volatile var flagDespawn = false

    override fun run() = update(Terrarum.appgc, Terrarum.delta)

    var isNoSubjectToGrav = false
    var dragCoefficient = 3.0

    private val lifetimeMax = maxLifeTime ?: 5000
    private var lifetimeCounter = 0

    open val velocity = Vector2(0.0, 0.0)
    open val hitbox = Hitbox(0.0, 0.0, 0.0, 0.0)

    open lateinit var body: Image // you might want to use SpriteAnimation
    open var glow: Image? = null

    init {

    }

    fun update(gc: GameContainer, delta: Int) {
        if (!flagDespawn) {
            lifetimeCounter += delta
            if (velocity.isZero || lifetimeCounter >= lifetimeMax ||
                // simple stuck check
                BlockCodex[Terrarum.ingame!!.world.getTileFromTerrain(
                        hitbox.canonicalX.div(TILE_SIZE).floorInt(),
                        hitbox.canonicalY.div(TILE_SIZE).floorInt()
                ) ?: Block.STONE].isSolid) {
                flagDespawn = true
            }

            // gravity, winds, etc. (external forces)
            if (!isNoSubjectToGrav) {
                velocity += Terrarum.ingame!!.world.gravitation / dragCoefficient * SI_TO_GAME_ACC
            }


            // combine external forces
            hitbox.translate(velocity)
        }
    }

    fun drawBody(g: Graphics) {
        if (!flagDespawn) {
            g.drawImage(body, hitbox.centeredX.toFloat(), hitbox.centeredY.toFloat())
        }
    }

    fun drawGlow(g: Graphics) {
        if (!flagDespawn && glow != null) {
            g.drawImage(glow, hitbox.centeredX.toFloat(), hitbox.centeredY.toFloat())
        }
    }
}