package net.torvald.terrarum.modulebasegame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ScreenUtils
import net.torvald.gdx.graphics.PixmapIO2
import net.torvald.terrarum.*
import net.torvald.terrarum.AppLoader.printdbg
import net.torvald.terrarum.gameactors.ActorWithBody
import net.torvald.terrarum.gamecontroller.KeyToggler
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.gameworld.fmod
import net.torvald.terrarum.modulebasegame.gameactors.ParticleBase
import net.torvald.terrarum.modulebasegame.weather.WeatherMixer
import net.torvald.terrarum.ui.UICanvas
import net.torvald.terrarum.worlddrawer.*
import net.torvald.util.CircularArray
import javax.swing.JFileChooser
import kotlin.system.exitProcess

/**
 * This will be rendered to a postprocessor FBO.
 *
 * For the entire render path, see AppLoader.
 */
object IngameRenderer : Disposable {
    /** for non-private use, use with care! */
    lateinit var batch: SpriteBatch
    private lateinit var camera: OrthographicCamera

    private lateinit var blurWriteQuad: Mesh

    private lateinit var lightmapFboA: FrameBuffer
    private lateinit var lightmapFboB: FrameBuffer
    private lateinit var fboRGB: FrameBuffer
    private lateinit var fboRGB_lightMixed: FrameBuffer
    private lateinit var fboA: FrameBuffer
    private lateinit var fboA_lightMixed: FrameBuffer
    private lateinit var rgbTex: TextureRegion
    private lateinit var aTex: TextureRegion
    private lateinit var lightTex: TextureRegion
    private lateinit var blurTex: TextureRegion

    // you must have lightMixed FBO; otherwise you'll be reading from unbaked FBO and it freaks out GPU

    val shaderBlur: ShaderProgram
    val shaderBayer: ShaderProgram
    val shaderBlendGlow: ShaderProgram
    val shaderRGBOnly: ShaderProgram
    val shaderAtoGrey: ShaderProgram
    val shaderPassthru = SpriteBatch.createDefaultShader()

    private val WIDTH = AppLoader.screenW
    private val HEIGHT = AppLoader.screenH
    private val WIDTHF = WIDTH.toFloat()
    private val HEIGHTF = HEIGHT.toFloat()

    private var initDone = false

    private var player: ActorWithBody? = null

    var uiListToDraw: List<UICanvas?> = arrayListOf()

    const val lightmapDownsample = 4f //2f: still has choppy look when the camera moves but unnoticeable when blurred

    private var debugMode = 0

    var renderingActorsCount = 0
        private set
    var renderingUIsCount = 0
        private set
    //var renderingParticleCount = 0
    //    private set

    var world: GameWorld = GameWorld.makeNullWorld()
        private set // the grammar "IngameRenderer.world = gameWorld" seemes mundane and this function needs special care!


    // these codes will run regardless of the invocation of the "initialise()" function
    // the "initialise()" function will also be called
    init {
        shaderBlur = AppLoader.loadShaderFromFile("assets/blur.vert", "assets/blur.frag")


        if (AppLoader.getConfigBoolean("fxdither")) {
            shaderBayer = AppLoader.loadShaderFromFile("assets/4096.vert", "assets/4096_bayer.frag")
            shaderBayer.begin()
            shaderBayer.setUniformf("rcount", 64f)
            shaderBayer.setUniformf("gcount", 64f)
            shaderBayer.setUniformf("bcount", 64f)
            shaderBayer.end()
        }
        else {
            shaderBayer = AppLoader.loadShaderFromFile("assets/4096.vert", "assets/passthrurgb.frag")
        }


        shaderBlendGlow = AppLoader.loadShaderFromFile("assets/blendGlow.vert", "assets/blendGlow.frag")

        shaderRGBOnly = AppLoader.loadShaderFromFile("assets/4096.vert", "assets/rgbonly.frag")
        shaderAtoGrey = AppLoader.loadShaderFromFile("assets/4096.vert", "assets/aonly.frag")


        if (!shaderBlendGlow.isCompiled) {
            Gdx.app.log("shaderBlendGlow", shaderBlendGlow.log)
            exitProcess(1)
        }


        if (AppLoader.getConfigBoolean("fxdither")) {
            if (!shaderBayer.isCompiled) {
                Gdx.app.log("shaderBayer", shaderBayer.log)
                exitProcess(1)
            }
        }

        initialise()
    }

    /** Whether or not "initialise()" method had been called */
    private var initialisedExternally = false

    /** To make it more convenient to be initialised by the Java code, and for the times when the order of the call
     * actually matter */
    @JvmStatic fun initialise() {
        if (!initialisedExternally) {
            AppLoader.disposableSingletonsPool.add(this)

            // also initialise these sinigletons
            BlocksDrawer
            LightmapRenderer


            initialisedExternally = true
        }
    }

    /**
     * Your game/a scene that renders the world must call this method at least once!
     *
     * For example:
     * - When the main scene that renders the world is first created
     * - When the game make transition to the new world (advancing to the next level/entering or exiting the room)
     */
    fun setRenderedWorld(world: GameWorld) {
            try {
                if (this.world != world) {
                    printdbg(this, "World change detected -- " +
                                   "old world: ${this.world.hashCode()}, " +
                                   "new world: ${world.hashCode()}")

                    // change worlds from internal methods
                    LightmapRenderer.internalSetWorld(world)
                    BlocksDrawer.world = world
                    FeaturesDrawer.world = world
                }
            }
            catch (e: UninitializedPropertyAccessException) {
                // new init, do nothing
            }
            finally {
                this.world = world
            }
        }

    operator fun invoke(
            gamePaused: Boolean,
            actorsRenderBehind : List<ActorWithBody>? = null,
            actorsRenderMiddle : List<ActorWithBody>? = null,
            actorsRenderMidTop : List<ActorWithBody>? = null,
            actorsRenderFront  : List<ActorWithBody>? = null,
            actorsRenderOverlay: List<ActorWithBody>? = null,
            particlesContainer : CircularArray<ParticleBase>? = null,
            player: ActorWithBody? = null,
            uiContainer: UIContainer? = null
    ) {
        renderingActorsCount = (actorsRenderBehind?.size ?: 0) +
                               (actorsRenderMiddle?.size ?: 0) +
                               (actorsRenderMidTop?.size ?: 0) +
                               (actorsRenderFront?.size ?: 0) +
                               (actorsRenderOverlay?.size ?: 0)
        //renderingParticleCount = particlesContainer?.size ?: 0
        //renderingParticleCount = (particlesContainer?.buffer?.map { (!it.flagDespawn).toInt() } ?: listOf(0)).sum()
        renderingUIsCount = uiContainer?.countVisible() ?: 0
        val zoom = Terrarum.ingame?.screenZoom ?: 1f

        invokeInit()

        batch.color = Color.WHITE


        this.player = player


        if (!gamePaused) {
            LightmapRenderer.fireRecalculateEvent(actorsRenderBehind, actorsRenderFront, actorsRenderMidTop, actorsRenderMiddle, actorsRenderOverlay)

            prepLightmapRGBA()
            BlocksDrawer.renderData()
            drawToRGB(actorsRenderBehind, actorsRenderMiddle, actorsRenderMidTop, actorsRenderFront, particlesContainer)
            drawToA(actorsRenderBehind, actorsRenderMiddle, actorsRenderMidTop, actorsRenderFront, particlesContainer)
            drawOverlayActors(actorsRenderOverlay)
        }
        // clear main or whatever super-FBO being used
        //clearBuffer()
        gdxClearAndSetBlend(.64f, .754f, .84f, 0f)

        ///////////////////////////////////////////////////////////////////////

        // draw sky
        WeatherMixer.render(camera, batch, world)

        ///////////////////////////////////////////////////////////////////////

        // use shader to mix RGB and A
        setCameraPosition(0f, 0f)

        rgbTex.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        aTex.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)

        // normal behaviour
        if (!KeyToggler.isOn(Input.Keys.F6) &&
            !KeyToggler.isOn(Input.Keys.F7)
        ) {
            debugMode = 0

            aTex.texture.bind(1)
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0) // so that batch that comes next will bind any tex to it


            batch.inUse {
                blendNormal(batch)
                batch.shader = shaderBlendGlow
                shaderBlendGlow.setUniformi("tex1", 1)
                batch.draw(rgbTex,
                        -0.5f * rgbTex.regionWidth * zoom + 0.5f * rgbTex.regionWidth,
                        -0.5f * rgbTex.regionHeight * zoom + 0.5f * rgbTex.regionHeight,
                        rgbTex.regionWidth * zoom,
                        rgbTex.regionHeight * zoom
                )
            }


            // blending is correct... somewhat. Alpha must be premultiplied
        }
        // something about RGB
        else if (KeyToggler.isOn(Input.Keys.F6) &&
                 !KeyToggler.isOn(Input.Keys.F7)
        ) {
            debugMode = 1
            batch.inUse {
                blendNormal(batch)
                batch.shader = null
                batch.draw(rgbTex,
                        -0.5f * rgbTex.regionWidth * zoom + 0.5f * rgbTex.regionWidth,
                        -0.5f * rgbTex.regionHeight * zoom + 0.5f * rgbTex.regionHeight,
                        rgbTex.regionWidth * zoom,
                        rgbTex.regionHeight * zoom
                )

                // indicator
                batch.color = Color.RED
                batch.fillRect(0f, 0f, 6f, 10f)
                batch.color = Color.LIME
                batch.fillRect(6f, 0f, 6f, 10f)
                batch.color = Color.ROYAL
                batch.fillRect(12f, 0f, 6f, 10f)
                batch.color = Color.WHITE
            }

            // works as intended
        }
        // something about A
        else if (!KeyToggler.isOn(Input.Keys.F6) &&
                 KeyToggler.isOn(Input.Keys.F7)
        ) {
            debugMode = 2
            batch.inUse {
                blendNormal(batch)
                batch.shader = null
                batch.draw(aTex,
                        -0.5f * aTex.regionWidth * zoom + 0.5f * aTex.regionWidth,
                        -0.5f * aTex.regionHeight * zoom + 0.5f * aTex.regionHeight,
                        aTex.regionWidth * zoom,
                        aTex.regionHeight * zoom
                )

                // indicator
                batch.color = Color.WHITE
                batch.fillRect(18f, 0f, 18f, 10f)
            }

            // works as intended
        }
        else {
            if (debugMode == 1) {
                KeyToggler.forceSet(Input.Keys.F6, false)
                KeyToggler.forceSet(Input.Keys.F7, true)
            }
            else if (debugMode == 2) {
                KeyToggler.forceSet(Input.Keys.F6, true)
                KeyToggler.forceSet(Input.Keys.F7, false)
            }
            else {
                KeyToggler.forceSet(Input.Keys.F6, false)
                KeyToggler.forceSet(Input.Keys.F7, false)
            }

            // works as intended
        }


        blendNormal(batch)


        ///////////////////////////////////////////////////////////////////////

        // draw UI
        setCameraPosition(0f, 0f)

        batch.inUse {
            batch.shader = null
            batch.color = Color.WHITE

            uiContainer?.forEach {
                it?.render(batch, camera)
            }
        }

        // works but some UI elements have wrong transparency -> should be fixed with Terrarum.gdxCleanAndSetBlend -- Torvald 2019-01-12
        blendNormal(batch)
        batch.color = Color.WHITE
    }


    private fun prepLightmapRGBA() {
        lightmapFboA.inAction(null, null) {
            clearBuffer()
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }
        lightmapFboB.inAction(null, null) {
            clearBuffer()
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }

        processBlur(lightmapFboA, lightmapFboB)
    }

    internal var fboRGBexportRequested = false

    /**
     * Which wires should be drawn. Normally this value is set by the wiring item (e.g. wire pieces, wirecutters)
     * This number is directly related with the World's wire bits:
     *
     * ```
     * world.getWires(x, y) -> 0000101 (for example)
     *     value of 3 selects this ^ ^
     *       value of 1 selects this |
     *
     * The wire piece gets rendered when selected bit is set.
     * ```
     */
    var selectedWireBitToDraw = 0

    private fun drawToRGB(
            actorsRenderBehind: List<ActorWithBody>?,
            actorsRenderMiddle: List<ActorWithBody>?,
            actorsRenderMidTop: List<ActorWithBody>?,
            actorsRenderFront : List<ActorWithBody>?,
            particlesContainer: CircularArray<ParticleBase>?
    ) {
        fboRGB.inAction(null, null) { clearBuffer() }
        fboRGB_lightMixed.inAction(null, null) { clearBuffer() }

        fboRGB.inAction(camera, batch) {

            batch.inUse {
                batch.shader = null
                batch.color = Color.WHITE
            }

            setCameraPosition(0f, 0f)
            BlocksDrawer.drawWall(batch.projectionMatrix, false)

            batch.inUse {
                moveCameraToWorldCoord()
                actorsRenderBehind?.forEach { it.drawBody(batch) }
                particlesContainer?.forEach { it.drawBody(batch) }
            }

            setCameraPosition(0f, 0f)
            BlocksDrawer.drawTerrain(batch.projectionMatrix, false)

            batch.inUse {
                /////////////////
                // draw actors //
                /////////////////
                moveCameraToWorldCoord()
                actorsRenderMiddle?.forEach { it.drawBody(batch) }
                actorsRenderMidTop?.forEach { it.drawBody(batch) }
                player?.drawBody(batch)
                actorsRenderFront?.forEach { it.drawBody(batch) }
                // --> Change of blend mode <-- introduced by children of ActorWithBody //
            }

            setCameraPosition(0f, 0f)
            BlocksDrawer.drawFront(batch.projectionMatrix, selectedWireBitToDraw) // blue coloured filter of water, etc.

            batch.inUse {
                FeaturesDrawer.drawEnvOverlay(batch)
            }
        }

        if (fboRGBexportRequested) {
            fboRGBexportRequested = false
            val fileChooser = JFileChooser()
            fileChooser.showSaveDialog(null)

            try {
                if (fileChooser.selectedFile != null) {
                    fboRGB.inAction(null, null) {
                        val p = ScreenUtils.getFrameBufferPixmap(0, 0, fboRGB.width, fboRGB.height)
                        PixmapIO2.writeTGA(Gdx.files.absolute(fileChooser.selectedFile.absolutePath), p, false)
                        p.dispose()
                    }
                }
            }
            catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fboRGB_lightMixed.inAction(camera, batch) {

            setCameraPosition(0f, 0f)
            val (xrem, yrem) = worldCamToRenderPos()

            gdxSetBlend()

            batch.inUse {

                blendNormal(batch)

                // draw world
                batch.draw(fboRGB.colorBufferTexture, 0f, 0f)
                batch.flush()

                // multiply light on top of it
                lightTex.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)

                if (KeyToggler.isOn(Input.Keys.F8))
                    blendNormal(batch)
                else
                    blendMul(batch)

                batch.shader = shaderRGBOnly
                batch.draw(lightTex,
                        xrem, yrem,
                        lightTex.regionWidth * lightmapDownsample,
                        lightTex.regionHeight * lightmapDownsample
                )
            }


            // NOTE TO SELF: this works.
        }


        blendNormal(batch)
    }

    private fun drawToA(
            actorsRenderBehind: List<ActorWithBody>?,
            actorsRenderMiddle: List<ActorWithBody>?,
            actorsRenderMidTop: List<ActorWithBody>?,
            actorsRenderFront : List<ActorWithBody>?,
            particlesContainer: CircularArray<ParticleBase>?
    ) {
        fboA.inAction(null, null) {
            clearBuffer()
            // paint black
            gdxClearAndSetBlend(0f,0f,0f,1f) // solid black: so that unused area will be also black
        }
        fboA_lightMixed.inAction(null, null) { clearBuffer() }

        fboA.inAction(camera, batch) {

            batch.inUse {
                batch.shader = null
                batch.color = Color.WHITE
            }

            setCameraPosition(0f, 0f)
            BlocksDrawer.drawWall(batch.projectionMatrix, true)

            batch.inUse {
                moveCameraToWorldCoord()
                actorsRenderBehind?.forEach { it.drawGlow(batch) }
                particlesContainer?.forEach { it.drawGlow(batch) }
            }

            setCameraPosition(0f, 0f)
            BlocksDrawer.drawTerrain(batch.projectionMatrix, true)

            batch.inUse {
                /////////////////
                // draw actors //
                /////////////////
                moveCameraToWorldCoord()
                actorsRenderMiddle?.forEach { it.drawGlow(batch) }
                actorsRenderMidTop?.forEach { it.drawGlow(batch) }
                player?.drawGlow(batch)
                actorsRenderFront?.forEach { it.drawGlow(batch) }
                // --> Change of blend mode <-- introduced by children of ActorWithBody //
            }
        }


        fboA_lightMixed.inAction(camera, batch) {

            setCameraPosition(0f, 0f)
            val (xrem, yrem) = worldCamToRenderPos()

            batch.inUse {
                // draw world
                batch.draw(fboA.colorBufferTexture, 0f, 0f)
                batch.flush()

                // multiply light on top of it
                lightTex.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)

                if (KeyToggler.isOn(Input.Keys.F8))
                    blendNormal(batch)
                else
                    blendMul(batch)

                batch.shader = shaderAtoGrey
                batch.draw(lightTex,
                        xrem, yrem,
                        lightTex.regionWidth * lightmapDownsample,
                        lightTex.regionHeight * lightmapDownsample
                )
            }


            // NOTE TO SELF: this works.
        }


        blendNormal(batch)
    }

    private fun drawOverlayActors(actors: List<ActorWithBody>?) {
        fboRGB_lightMixed.inAction(camera, batch) {

            batch.inUse {
                batch.shader = null
                batch.color = Color.WHITE
            }

            setCameraPosition(0f, 0f)
            // BlocksDrawer.renderWhateverGlow_WALL

            batch.inUse {
                moveCameraToWorldCoord()
                actors?.forEach { it.drawBody(batch) }
            }

            setCameraPosition(0f, 0f)
            // BlocksDrawer.renderWhateverGlow_TERRAIN
        }
    }


    private fun invokeInit() {
        if (!initDone) {
            batch = SpriteBatch()
            camera = OrthographicCamera(WIDTHF, HEIGHTF)

            camera.setToOrtho(true, WIDTHF, HEIGHTF)
            camera.update()
            Gdx.gl20.glViewport(0, 0, WIDTH, HEIGHT)

            resize(WIDTH, HEIGHT)

            initDone = true
        }
    }

    private fun clearBuffer() {
        gdxClearAndSetBlend(0f,0f,0f,0f)
    }

    private fun moveCameraToWorldCoord() {
        // using custom code for camera; this is obscure and tricky
        camera.position.set(WorldCamera.gdxCamX, WorldCamera.gdxCamY, 0f) // make camara work
        camera.update()
        batch.projectionMatrix = camera.combined
    }

    /**
     * Camera will be moved so that (newX, newY) would be sit on the top-left edge.
     */
    private fun setCameraPosition(newX: Float, newY: Float) {
        camera.position.set((-newX + AppLoader.halfScreenW).round(), (-newY + AppLoader.halfScreenH).round(), 0f)
        camera.update()
        batch.projectionMatrix = camera.combined
    }

    fun processBlur(lightmapFboA: FrameBuffer, lightmapFboB: FrameBuffer) {
        val blurIterations = 5 // ideally, 4 * radius; must be even/odd number -- odd/even number will flip the image
        val blurRadius = 4f / lightmapDownsample // (5, 4f); using low numbers for pixel-y aesthetics

        var blurWriteBuffer = lightmapFboA
        var blurReadBuffer = lightmapFboB


        // buffers must be cleared beforehand


        // initialise readBuffer with untreated lightmap
        blurReadBuffer.inAction(camera, batch) {
            val texture = LightmapRenderer.draw()
            texture.bind(0)

            shaderPassthru.begin()
            shaderPassthru.setUniformMatrix("u_projTrans", camera.combined)
            shaderPassthru.setUniformi("u_texture", 0)
            blurWriteQuad.render(shaderPassthru, GL20.GL_TRIANGLES)
            shaderPassthru.end()
        }

        // do blurring
        for (i in 0 until blurIterations) {
            blurWriteBuffer.inAction(camera, batch) {

                blurTex.texture = blurReadBuffer.colorBufferTexture
                blurTex.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                blurTex.texture.bind(0)

                shaderBlur.begin()
                shaderBlur.setUniformMatrix("u_projTrans", camera.combined)
                shaderBlur.setUniformi("u_texture", 0)
                shaderBlur.setUniformf("iResolution",
                        blurWriteBuffer.width.toFloat(), blurWriteBuffer.height.toFloat())
                shaderBlur.setUniformf("flip", 1f)
                if (i % 2 == 0)
                    shaderBlur.setUniformf("direction", blurRadius, 0f)
                else
                    shaderBlur.setUniformf("direction", 0f, blurRadius)
                blurWriteQuad.render(shaderBlur, GL20.GL_TRIANGLES)
                shaderBlur.end()


                // swap
                val t = blurWriteBuffer
                blurWriteBuffer = blurReadBuffer
                blurReadBuffer = t
            }
        }



        blendNormal(batch)
    }

    private var init = false

    fun resize(width: Int, height: Int) {
        if (!init) {
            blurWriteQuad = Mesh(
                    true, 4, 6,
                    VertexAttribute.Position(),
                    VertexAttribute.ColorUnpacked(),
                    VertexAttribute.TexCoords(0)
            )

            init = true
        }
        else {
            fboRGB.dispose()
            fboRGB_lightMixed.dispose()
            fboA.dispose()
            fboA_lightMixed.dispose()
            lightmapFboA.dispose()
            lightmapFboB.dispose()
        }

        fboRGB = FrameBuffer(Pixmap.Format.RGBA8888, width, height, true)
        fboRGB_lightMixed = FrameBuffer(Pixmap.Format.RGBA8888, width, height, true)
        fboA = FrameBuffer(Pixmap.Format.RGBA8888, width, height, true)
        fboA_lightMixed = FrameBuffer(Pixmap.Format.RGBA8888, width, height, true)
        lightmapFboA = FrameBuffer(
                Pixmap.Format.RGBA8888,
                LightmapRenderer.lightBuffer.width * LightmapRenderer.DRAW_TILE_SIZE.toInt(),
                LightmapRenderer.lightBuffer.height * LightmapRenderer.DRAW_TILE_SIZE.toInt(),
                true
        )
        lightmapFboB = FrameBuffer(
                Pixmap.Format.RGBA8888,
                LightmapRenderer.lightBuffer.width * LightmapRenderer.DRAW_TILE_SIZE.toInt(),
                LightmapRenderer.lightBuffer.height * LightmapRenderer.DRAW_TILE_SIZE.toInt(),
                true
        )
        rgbTex = TextureRegion(fboRGB_lightMixed.colorBufferTexture)
        aTex = TextureRegion(fboA_lightMixed.colorBufferTexture)
        lightTex = TextureRegion(lightmapFboB.colorBufferTexture)
        blurTex = TextureRegion()

        BlocksDrawer.resize(width, height)
        LightmapRenderer.resize(width, height)


        blurWriteQuad.setVertices(floatArrayOf(
                0f,0f,0f, 1f,1f,1f,1f, 0f,1f,
                lightmapFboA.width.toFloat(),0f,0f, 1f,1f,1f,1f, 1f,1f,
                lightmapFboA.width.toFloat(),lightmapFboA.height.toFloat(),0f, 1f,1f,1f,1f, 1f,0f,
                0f,lightmapFboA.height.toFloat(),0f, 1f,1f,1f,1f, 0f,0f))
        blurWriteQuad.setIndices(shortArrayOf(0, 1, 2, 2, 3, 0))

    }

    private val TILE_SIZEF = CreateTileAtlas.TILE_SIZE.toFloat()

    override fun dispose() {
        fboRGB.dispose()
        fboA.dispose()
        fboRGB_lightMixed.dispose()
        fboA_lightMixed.dispose()
        lightmapFboA.dispose()
        lightmapFboB.dispose()

        LightmapRenderer.dispose()
        BlocksDrawer.dispose()
        WeatherMixer.dispose()

        batch.dispose()

        shaderBlur.dispose()
        shaderBayer.dispose()
        shaderBlendGlow.dispose()
        shaderRGBOnly.dispose()
        shaderAtoGrey.dispose()
        shaderPassthru.dispose()
    }

    private fun worldCamToRenderPos(): Pair<Float, Float> {
        // for some reason it does not like integer. No, really; it breaks (jitter when you move) when you try to "fix" that.
        val xrem = -(WorldCamera.x.toFloat() fmod TILE_SIZEF)
        val yrem = -(WorldCamera.y.toFloat() fmod TILE_SIZEF)

        return xrem to yrem
    }
}