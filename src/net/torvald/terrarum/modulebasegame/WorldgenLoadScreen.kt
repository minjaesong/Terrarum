package net.torvald.terrarum.modulebasegame

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import net.torvald.terrarum.*
import net.torvald.terrarum.AppLoader.printdbg
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.util.CircularArray
import kotlin.math.roundToInt

/**
 * World loading screen with minecraft 1.14-style preview
 *
 * Created by minjaesong on 2019-11-09.
 */
class WorldgenLoadScreen(screenToBeLoaded: IngameInstance, private val worldwidth: Int, private val worldheight: Int) : LoadScreenBase() {

    // a Class impl is chosen to make resize-handling easier, there's not much benefit making this a singleton anyway

    init {
        AppLoader.disposableSingletonsPool.add(this)
    }

    override var screenToLoad: IngameInstance? = screenToBeLoaded
    private val world: GameWorld // must use Getter, as the field WILL BE redefined by the TerrarumIngame.enterCreateNewWorld() !
        get() = screenToLoad!!.world

    companion object {
        private const val WIDTH_RATIO = 0.7
        private const val PREVIEW_UPDATE_RATE = 1 / 5f

        private val COL_WALL = Color.WHITE
        private val COL_TERR = Color(.5f, .5f, .5f, 1f)
        private val COL_AIR = Color.BLACK
    }

    private val previewWidth = (AppLoader.screenW * WIDTH_RATIO).roundToInt()
    private val previewHeight = (AppLoader.screenW * WIDTH_RATIO * worldheight / worldwidth).roundToInt()

    private lateinit var previewPixmap: Pixmap
    private lateinit var previewTexture: Texture

    private var previewRenderCounter = 0f

    // NOTE: actual world init and terragen is called by TerrarumIngame.enterLoadFromSave()

    override fun show() {
        super.show()

        previewPixmap = Pixmap(previewWidth, previewHeight, Pixmap.Format.RGBA8888)
        previewTexture = Texture(1, 1, Pixmap.Format.RGBA8888)

        previewPixmap.setColor(Color.BLACK)
        previewPixmap.fill()
    }

    override fun render(delta: Float) {
        //println("WorldgenLoadScreenRender")

        gdxClearAndSetBlend(.094f, .094f, .094f, 0f)

        previewRenderCounter += delta
        if (previewRenderCounter >= PREVIEW_UPDATE_RATE) {
            previewRenderCounter -= PREVIEW_UPDATE_RATE
            renderToPreview()
            previewTexture.dispose()
            previewTexture = Texture(previewPixmap)
        }


        AppLoader.batch.inUse {
            it.color = Color.WHITE
            it.draw(previewTexture,
                    (AppLoader.screenW - previewWidth).div(2f).round(),
                    (AppLoader.screenH - previewHeight.times(1.5f)).div(2f).round()
            )
            val text = messages.getHeadElem() ?: ""
            AppLoader.fontGame.draw(it,
                    text,
                    (AppLoader.screenW - AppLoader.fontGame.getWidth(text)).div(2f).round(),
                    (AppLoader.screenH + previewHeight.times(1.5f)).div(2f).round() - AppLoader.fontGame.lineHeight
            )
        }


        super.render(delta)
    }

    private fun renderToPreview() {
        for (y in 0 until previewHeight) {
            for (x in 0 until previewWidth) {
                val wx = (world.width.toFloat() / previewWidth * x).roundToInt()
                val wy = (world.height.toFloat() / previewHeight * y).roundToInt()

                val colT = if (world.getTileFromTerrain(wx, wy) != 0) COL_WALL else COL_TERR
                val colW = if (world.getTileFromWall(wx, wy) != 0) COL_WALL else COL_AIR
                val outCol = colW mul colT

                previewPixmap.setColor(outCol)
                previewPixmap.drawPixel(x, previewHeight - 1 - y) // this flips Y
            }
        }

    }

    override fun addMessage(msg: String) {
        super.addMessage(msg)
    }

    override fun dispose() {
        if (!previewPixmap.isDisposed)
            previewPixmap.dispose()

        previewTexture.dispose()
    }
}