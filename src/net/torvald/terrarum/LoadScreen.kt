package net.torvald.terrarum

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.jme3.math.FastMath
import net.torvald.dataclass.HistoryArray
import net.torvald.terrarum.gameactors.floor
import net.torvald.terrarum.langpack.Lang

/**
 * Created by minjaesong on 2017-07-13.
 */
object LoadScreen : ScreenAdapter() {

    var screenToLoad: Ingame? = null
    private lateinit var screenLoadingThread: Thread


    private val messages = HistoryArray<String>(20)

    fun addMessage(msg: String) {
        messages.add(msg)
    }



    private var arrowObjPos = 0f // 0 means at starting position, regardless of screen position
    private var arrowObjGlideOffsetX = 0f
    private var arrowObjGlideSize = 0f
    private val arrowGlideSpeed: Float; get() = Terrarum.WIDTH * 1.5f // pixels per sec
    private lateinit var arrowObjTex: Texture
    private var glideTimer = 0f
    private var glideDispY = 0f
    private var arrowColours = arrayOf(
            Color(0xff4c4cff.toInt()),
            Color(0xffd24cff.toInt()),
            Color(0x4cb5ffff.toInt())
    )

    private lateinit var textOverlayTex: Texture
    private lateinit var textFbo: FrameBuffer

    private val ghostMaxZoomX = 1.25f
    private val ghostAlphaMax = 1f

    var camera = OrthographicCamera(Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT.toFloat())

    fun initViewPort(width: Int, height: Int) {
        // Set Y to point downwards
        camera.setToOrtho(true, width.toFloat(), height.toFloat())

        // Update camera matrix
        camera.update()

        // Set viewport to restrict drawing
        Gdx.gl20.glViewport(0, 0, width, height)
    }



    override fun show() {
        messages.clear()
        doContextChange = false
        glideTimer = 0f


        if (screenToLoad == null) {
            println("[LoadScreen] Screen to load is not set. Are you testing the UI?")
        }
        else {
            val runnable = object : Runnable {
                override fun run() {
                    screenToLoad!!.show()
                }
            }
            screenLoadingThread = Thread(runnable, "LoadScreen GameLoader")

            screenLoadingThread.start()
        }


        initViewPort(Terrarum.WIDTH, Terrarum.HEIGHT)

        textFbo = FrameBuffer(
                Pixmap.Format.RGBA4444,
                Terrarum.fontGame.getWidth(Lang["MENU_IO_LOADING"]),
                Terrarum.fontGame.lineHeight.toInt(),
                true
        )

        arrowObjTex = Texture(Gdx.files.internal("assets/graphics/test_loading_arrow_atlas.tga"))
        arrowObjGlideOffsetX = -arrowObjTex.width.toFloat()

        textOverlayTex = Texture(Gdx.files.internal("assets/graphics/test_loading_text_tint.tga"))
    }


    val textX: Float; get() = (Terrarum.WIDTH * 0.75f).floor()

    private var genuineSonic = false // the "NOW LOADING..." won't appear unless the arrow first run passes it  (it's totally not a GenuineIntel tho)
    private var doContextChange = false

    private var messageBackgroundColour = Color(0x404040ff)
    private var messageForegroundColour = Color.WHITE

    override fun render(delta: Float) {
        glideDispY = Terrarum.HEIGHT - 100f - Terrarum.fontGame.lineHeight
        arrowObjGlideSize = arrowObjTex.width + 2f * Terrarum.WIDTH



        Gdx.gl.glClearColor(.094f, .094f, .094f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        textFbo.inAction(null, null) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }

        glideTimer += delta
        // reset timer
        if (glideTimer >= arrowObjGlideSize / arrowGlideSpeed) {
            glideTimer -= arrowObjGlideSize / arrowGlideSpeed

            // change screen WHEN the timer is reset.
            // In other words, the arrow must hit the goal BEFORE context change take place
            if (screenToLoad?.gameFullyLoaded ?: false) {
                doContextChange = true
            }
        }
        arrowObjPos = glideTimer * arrowGlideSpeed


        if (!doContextChange) {
            // draw text to FBO
            textFbo.inAction(camera, Terrarum.batch) {
                Terrarum.batch.inUse {
                    blendNormal()
                    Terrarum.fontGame
                    it.color = Color.WHITE
                    Terrarum.fontGame.draw(it, Lang["MENU_IO_LOADING"], 0.33f, 0f) // x 0.5? I dunno but it breaks w/o it


                    blendMul()
                    // draw flipped
                    it.draw(textOverlayTex,
                            0f,
                            Terrarum.fontGame.lineHeight,
                            textOverlayTex.width.toFloat(),
                            -Terrarum.fontGame.lineHeight
                    )
                }
            }


            Terrarum.batch.inUse {
                initViewPort(Terrarum.WIDTH, Terrarum.HEIGHT) // dunno, no render without this
                it.projectionMatrix = camera.combined
                blendNormal()

                // draw text FBO to screen
                val textTex = textFbo.colorBufferTexture
                textTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

                // --> original text
                if (genuineSonic) {
                    it.color = Color.WHITE
                    it.draw(textTex, textX, glideDispY - 2f)
                }

                // --> ghost
                it.color = getPulseEffCol()

                if (it.color.a != 0f) genuineSonic = true

                val drawWidth = getPulseEffWidthMul() * textTex.width
                val drawHeight = getPulseEffWidthMul() * textTex.height
                it.draw(textTex,
                        textX - (drawWidth - textTex.width) / 2f,
                        glideDispY - 2f - (drawHeight - textTex.height) / 2f,
                        drawWidth,
                        drawHeight
                )


                // draw coloured arrows
                arrowColours.forEachIndexed { index, color ->
                    it.color = color
                    it.draw(arrowObjTex, arrowObjPos + arrowObjGlideOffsetX + arrowObjTex.width * index, glideDispY)
                }




                // message backgrounds
                it.color = messageBackgroundColour
                it.fillRect(0f, 60f, Terrarum.WIDTH.toFloat(), 40f + (messages.size) * Terrarum.fontGame.lineHeight)

                // log messages
                it.color = messageForegroundColour
                for (i in 0 until messages.elemCount) {
                    Terrarum.fontGame.draw(it,
                            messages[i] ?: "",
                            40f,
                            80f + (messages.size - i - 1) * Terrarum.fontGame.lineHeight
                    )
                }
            }
        }
        else {
            Terrarum.batch.inUse {
                // recycling part of the draw code //

                initViewPort(Terrarum.WIDTH, Terrarum.HEIGHT) // dunno, no render without this
                it.projectionMatrix = camera.combined
                blendNormal()



                // message backgrounds
                it.color = messageBackgroundColour
                it.fillRect(0f, 60f, Terrarum.WIDTH.toFloat(), 40f + (messages.size) * Terrarum.fontGame.lineHeight)

                // log messages
                it.color = messageForegroundColour
                for (i in 0 until messages.elemCount) {
                    Terrarum.fontGame.draw(it,
                            messages[i] ?: "",
                            40f,
                            80f + (messages.size - i - 1) * Terrarum.fontGame.lineHeight
                    )
                }
            }

            Terrarum.batch.flush()

            Thread.sleep(80)

            Terrarum.changeScreen(screenToLoad!!)
        }
    }

    private fun getPulseEffCol(): Color {
        if (arrowObjPos + arrowObjTex.width * 3f < textX)
            return Color(1f, 1f, 1f, 0f)
        else {
            // ref point: top-left of arrow drawn to the screen, 0 being start of the RAIL
            val scaleStart = textX - arrowObjTex.width * 3f
            val scaleEnd = arrowObjGlideSize - arrowObjTex.width * 3f
            val scale = (arrowObjPos - scaleStart) / (scaleEnd - scaleStart)

            val alpha = FastMath.interpolateLinear(scale, ghostAlphaMax, 0f)

            return Color(1f, 1f, 1f, alpha)
        }
    }

    private fun getPulseEffWidthMul(): Float {
        if (arrowObjPos + arrowObjTex.width * 3f < textX)
            return 1f
        else {
            // ref point: top-left of arrow drawn to the screen, 0 being start of the RAIL
            val scaleStart = textX - arrowObjTex.width * 3f
            val scaleEnd = arrowObjGlideSize - arrowObjTex.width * 3f
            val scale = (arrowObjPos - scaleStart) / (scaleEnd - scaleStart)

            return FastMath.interpolateLinear(scale, 1f, ghostMaxZoomX)
        }
    }

    override fun dispose() {
        arrowObjTex.dispose()
        textFbo.dispose()
        textOverlayTex.dispose()
    }

    override fun hide() {
        dispose()
    }

    override fun resize(width: Int, height: Int) {
        initViewPort(Terrarum.WIDTH, Terrarum.HEIGHT)
    }
}