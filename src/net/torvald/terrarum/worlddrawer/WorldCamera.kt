package net.torvald.terrarum.worlddrawer

import com.jme3.math.FastMath
import net.torvald.terrarum.AppLoader
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.ceilInt
import net.torvald.terrarum.floorInt
import net.torvald.terrarum.gameactors.ActorWithBody
import net.torvald.terrarum.gameworld.GameWorld
import org.dyn4j.geometry.Vector2

/**
 * Created by minjaesong on 2016-12-30.
 */
object WorldCamera {
    private val TILE_SIZE = CreateTileAtlas.TILE_SIZE

    //val zoom: Float
    //    get() = Terrarum.ingame?.screenZoom ?: 1f

    var x: Int = 0 // left position
        private set
    var y: Int = 0 // top position
        private set

    var width: Int = 0
        private set
    var height: Int = 0
        private set

    private var zoom = 1f
    private var zoomSamplePoint = 0f

    // zoomed coords. Currently only being used by the lightmaprenderer.
    // What about others? We just waste 3/4 of the framebuffer
    val zoomedX: Int
        get() = x + (width * zoomSamplePoint).toInt()
    val zoomedY: Int
        get() = y + (height * zoomSamplePoint).toInt()

    val zoomedWidth: Int
        get() = (width / zoom).ceilInt()
    val zoomedHeight: Int
        get() = (height / zoom).ceilInt()

    var xEnd: Int = 0 // right position
        private set
    var yEnd: Int = 0 // bottom position
        private set

    inline val gdxCamX: Float // centre position
        get() = xCentre.toFloat()
    inline val gdxCamY: Float// centre position
        get() = yCentre.toFloat()

    inline val xCentre: Int
        get() = x + width.ushr(1)
    inline val yCentre: Int
        get() = y + height.ushr(1)

    private val nullVec = Vector2(0.0, 0.0)

    fun update(world: GameWorld, player: ActorWithBody?) {
        if (player == null) return

        width = AppLoader.screenW//FastMath.ceil(AppLoader.screenW / zoom) // div, not mul
        height = AppLoader.screenH//FastMath.ceil(AppLoader.screenH / zoom)
        zoom = Terrarum.ingame?.screenZoom ?: 1f
        zoomSamplePoint = (1f - 1f / zoom) / 2f // will never quite exceed 0.5

        // TOP-LEFT position of camera border

        // some hacky equation to position player at the dead centre
        // implementing the "lag behind" camera the right way
        val pVecSum = if (player is ActorWithBody)
            player.externalV + (player.controllerV ?: nullVec)
        else
            nullVec

        x = ((player.hitbox.centeredX - pVecSum.x).toFloat() - (width / 2)).floorInt() // X only: ROUNDWORLD implementation


        y = (FastMath.clamp(
                (player.hitbox.centeredY - pVecSum.y).toFloat() - height / 2,
                TILE_SIZE.toFloat(),
                world.height * TILE_SIZE - height - TILE_SIZE.toFloat()
        )).floorInt().clampCameraY(world)

        xEnd = x + width
        yEnd = y + height
    }

    private fun Int.clampCameraY(world: GameWorld): Int {
        return if (this < 0)
            0
        else if (this > world.height.times(TILE_SIZE) - AppLoader.screenH)
            world.height.times(TILE_SIZE) - AppLoader.screenH
        else
            this
    }
}

