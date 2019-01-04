package net.torvald.terrarum.modulebasegame.gameactors

import net.torvald.terrarum.ModMgr
import net.torvald.terrarum.gameactors.AVKey
import net.torvald.terrarum.gameactors.ActorWBMovable
import net.torvald.terrarum.gameactors.ai.NullAI
import net.torvald.terrarum.worlddrawer.FeaturesDrawer
import net.torvald.terrarumsansbitmap.gdx.TextureRegionPack

/**
 * Created by minjaesong on 2016-03-25.
 */
object PlayerBuilderCynthia {

    operator fun invoke(): ActorWBMovable {
        //val p: IngamePlayer = IngamePlayer(GameDate(100, 143)) // random value thrown
        val p: HumanoidNPC = HumanoidNPC(
                NullAI(),
                -589141658L) // random value thrown
        InjectCreatureRaw(p.actorValue, "basegame", "CreatureHuman.json")


        p.actorValue[AVKey.__PLAYER_QUICKSLOTSEL] = 0
        p.actorValue[AVKey.__ACTION_TIMER] = 0.0
        p.actorValue[AVKey.ACTION_INTERVAL] = ActorHumanoid.BASE_ACTION_INTERVAL
        p.actorValue[AVKey.NAME] = "Cynthia"


        p.makeNewSprite(TextureRegionPack(ModMgr.getGdxFile("basegame", "sprites/test_player_2.tga"), 26, 42))
        p.sprite!!.setRowsAndFrames(1, 1)

        p.setHitboxDimension(15, p.actorValue.getAsInt(AVKey.BASEHEIGHT) ?: ActorHumanoid.BASE_HEIGHT, 9, 0)

        p.setPosition(4096.0 * FeaturesDrawer.TILE_SIZE, 300.0 * FeaturesDrawer.TILE_SIZE)




        p.referenceID = 321321321



        return p
    }


}