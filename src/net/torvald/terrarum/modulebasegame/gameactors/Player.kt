package net.torvald.terrarum.modulebasegame.gameactors

import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.modulebasegame.gameworld.time_t


/**
 * Game player (YOU!)
 *
 * Created by minjaesong on 2015-12-31.
 */

class Player(world: GameWorld, born: time_t) : ActorHumanoid(world, born) {

    companion object {
        @Transient const val PLAYER_REF_ID: Int = 0x91A7E2
    }

    /**
     * Creates new Player instance with empty elements (sprites, actorvalue, etc.).

     * **Use PlayerFactory to build player!**

     * @throws SlickException
     */
    init {
        referenceID = PLAYER_REF_ID // forcibly set ID
        density = BASE_DENSITY
        collisionType = COLLISION_KINEMATIC
    }

    override fun update(delta: Float) {
        super.update(delta)
    }

}