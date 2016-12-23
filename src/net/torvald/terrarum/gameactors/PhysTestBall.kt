package net.torvald.terrarum.gameactors

import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.mapdrawer.MapDrawer.TILE_SIZE
import net.torvald.terrarum.mapgenerator.RoguelikeRandomiser
import org.newdawn.slick.Color
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics

/**
 * Created by minjaesong on 16-03-05.
 */
class PhysTestBall : ActorWithBody() {

    private var color = Color.orange

    init {
        setHitboxDimension(16, 16, 0, 0)
        mass = 10.0
        density = 200.0

        color = RoguelikeRandomiser.composeColourFrom(RoguelikeRandomiser.POTION_PRIMARY_COLSET)
    }

    override fun drawBody(gc: GameContainer, g: Graphics) {
        g.color = color
        g.fillOval(
                hitbox.posX.toFloat(),
                hitbox.posY.toFloat(),
                hitbox.width.toFloat(),
                hitbox.height.toFloat())

        g.fillOval(
                hitbox.posX.toFloat() + Terrarum.ingame.world.width * TILE_SIZE,
                hitbox.posY.toFloat(),
                hitbox.width.toFloat(),
                hitbox.height.toFloat())

        g.fillOval(
                hitbox.posX.toFloat() - Terrarum.ingame.world.width * TILE_SIZE,
                hitbox.posY.toFloat(),
                hitbox.width.toFloat(),
                hitbox.height.toFloat())
    }
}