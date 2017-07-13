package net.torvald.terrarum

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer

import net.torvald.dataclass.CircularArray
import net.torvald.terrarum.blockproperties.BlockPropUtil
import net.torvald.terrarum.blockstats.BlockStats
import net.torvald.terrarum.concurrent.ThreadParallel
import net.torvald.terrarum.console.*
import net.torvald.terrarum.gameactors.*
import net.torvald.terrarum.gameactors.physicssolver.CollisionSolver
import net.torvald.terrarum.gamecontroller.GameController
import net.torvald.terrarum.gamecontroller.KeyToggler
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.gameworld.WorldSimulator
import net.torvald.terrarum.weather.WeatherMixer
import net.torvald.terrarum.worlddrawer.BlocksDrawer
import net.torvald.terrarum.worlddrawer.FeaturesDrawer
import net.torvald.terrarum.worlddrawer.LightmapRenderer
import net.torvald.terrarum.worlddrawer.WorldCamera

import java.util.ArrayList
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

import javax.swing.JOptionPane

import com.badlogic.gdx.graphics.OrthographicCamera
import net.torvald.random.HQRNG
import net.torvald.terrarum.ui.*
import net.torvald.terrarum.worldgenerator.RoguelikeRandomiser
import net.torvald.terrarum.worldgenerator.WorldGenerator
import net.torvald.terrarumsansbitmap.gdx.TextureRegionPack


/**
 * Created by minjaesong on 2017-06-16.
 */

class Ingame(val batch: SpriteBatch) : Screen {


    private val ACTOR_UPDATE_RANGE = 4096

    lateinit var world: GameWorld
    lateinit var historicalFigureIDBucket: ArrayList<Int>

    /**
     * list of Actors that is sorted by Actors' referenceID
     */
    val ACTORCONTAINER_INITIAL_SIZE = 64
    val PARTICLES_MAX = Terrarum.getConfigInt("maxparticles")
    val actorContainer = ArrayList<Actor>(ACTORCONTAINER_INITIAL_SIZE)
    val actorContainerInactive = ArrayList<Actor>(ACTORCONTAINER_INITIAL_SIZE)
    val particlesContainer = CircularArray<ParticleBase>(PARTICLES_MAX)
    val uiContainer = ArrayList<UIHandler>()

    private val actorsRenderBehind = ArrayList<ActorWithBody>(ACTORCONTAINER_INITIAL_SIZE)
    private val actorsRenderMiddle = ArrayList<ActorWithBody>(ACTORCONTAINER_INITIAL_SIZE)
    private val actorsRenderMidTop = ArrayList<ActorWithBody>(ACTORCONTAINER_INITIAL_SIZE)
    private val actorsRenderFront  = ArrayList<ActorWithBody>(ACTORCONTAINER_INITIAL_SIZE)

    var playableActorDelegate: PlayableActorDelegate? = null // DO NOT LATEINIT!
        private set
    inline val player: ActorHumanoid? // currently POSSESSED actor :)
        get() = playableActorDelegate?.actor

    var screenZoom = 1.0f
    val ZOOM_MAXIMUM = 4.0f
    val ZOOM_MINIMUM = 0.5f

    companion object {
        val lightmapDownsample = 2f //2f: still has choppy look when the camera moves but unnoticeable when blurred
    }


    private val worldFBOformat = if (Terrarum.environment == RunningEnvironment.MOBILE) Pixmap.Format.RGBA4444 else Pixmap.Format.RGBA8888
    private val lightFBOformat = Pixmap.Format.RGB888

    var worldDrawFrameBuffer = FrameBuffer(worldFBOformat, Terrarum.WIDTH, Terrarum.HEIGHT, true)
    var worldGlowFrameBuffer = FrameBuffer(worldFBOformat, Terrarum.WIDTH, Terrarum.HEIGHT, true)
    var worldBlendFrameBuffer = FrameBuffer(worldFBOformat, Terrarum.WIDTH, Terrarum.HEIGHT, true)
    // RGB elements of Lightmap for Color  Vec4(R, G, B, 1.0)  24-bit
    var lightmapFboA = FrameBuffer(lightFBOformat, Terrarum.WIDTH.div(lightmapDownsample.toInt()), Terrarum.HEIGHT.div(lightmapDownsample.toInt()), true)
    var lightmapFboB = FrameBuffer(lightFBOformat, Terrarum.WIDTH.div(lightmapDownsample.toInt()), Terrarum.HEIGHT.div(lightmapDownsample.toInt()), true)


    init {
        println("worldDrawFrameBuffer.colorBufferTexture.textureData.format: ${worldDrawFrameBuffer.colorBufferTexture.textureData.format}")
        println("lightmapFboB.colorBufferTexture.textureData.format: ${lightmapFboB.colorBufferTexture.textureData.format}")
    }



    private val useShader: Boolean = false
    private val shaderProgram = 0

    val KEY_LIGHTMAP_RENDER = Input.Keys.F7



    lateinit var consoleHandler: UIHandler
    lateinit var debugWindow: UIHandler
    lateinit var notifier: UIHandler

    lateinit var uiPieMenu: UIHandler
    lateinit var uiQuickBar: UIHandler
    lateinit var uiInventoryPlayer: UIHandler
    lateinit var uiInventoryContainer: UIHandler
    lateinit var uiVitalPrimary: UIHandler
    lateinit var uiVitalSecondary: UIHandler
    lateinit var uiVitalItem: UIHandler // itemcount/durability of held block or active ammo of held gun. As for the block, max value is 500.

    lateinit var uiWatchBasic: UIHandler
    lateinit var uiWatchTierOne: UIHandler


    // UI aliases
    lateinit var uiAliases: ArrayList<UIHandler>
        private set
    lateinit var uiAlasesPausing: ArrayList<UIHandler>
        private set

    inline val paused: Boolean
        get() = uiAlasesPausing.map { if (it.isOpened) return true else 0 }.isEmpty() // isEmply is always false, which we want
    /**
     * Set to false if UI is opened; set to true  if UI is closed.
     */
    inline val canPlayerControl: Boolean
        get() = !paused // FIXME temporary behab (block movement if the game is paused or paused by UIs)

    var particlesActive = 0
        private set



    private lateinit var ingameUpdateThread: ThreadIngameUpdate
    private lateinit var updateThreadWrapper: Thread
    //private val ingameDrawThread: ThreadIngameDraw // draw must be on the main thread


    private var gameFullyLoaded = false



    //////////////
    // GDX code //
    //////////////

    var camera = OrthographicCamera(Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT.toFloat())

    /** Actually just a mesh of four vertices, two triangles -- not a literal glQuad */
    var fullscreenQuad = Mesh(
            true, 4, 6,
            VertexAttribute.Position(),
            VertexAttribute.ColorUnpacked(),
            VertexAttribute.TexCoords(0)
    )

    init {
        fullscreenQuad.setVertices(floatArrayOf(
                0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f, 1f,
                Terrarum.WIDTH.toFloat(), 0f, 0f, 1f, 1f, 1f, 1f, 1f, 1f,
                Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT.toFloat(), 0f, 1f, 1f, 1f, 1f, 1f, 0f,
                0f, Terrarum.HEIGHT.toFloat(), 0f, 1f, 1f, 1f, 1f, 0f, 0f
        ))
        fullscreenQuad.setIndices(shortArrayOf(0, 1, 2, 2, 3, 0))
    }

    // invert Y
    fun initViewPort(width: Int, height: Int) {
        //val width = if (width % 1 == 1) width + 1 else width
        //val height = if (height % 1 == 1) height + 1 else width

        // Set Y to point downwards
        camera.setToOrtho(true, width.toFloat(), height.toFloat())

        // Update camera matrix
        camera.update()

        // Set viewport to restrict drawing
        Gdx.gl20.glViewport(0, 0, width, height)
    }


    lateinit var gameLoadMode: GameLoadMode
    lateinit var gameLoadInfoPayload: Any

    enum class GameLoadMode {
        CREATE_NEW, LOAD_FROM
    }

    override fun show() {
        initViewPort(Terrarum.WIDTH, Terrarum.HEIGHT)


        // gameLoadMode and gameLoadInfoPayload must be set beforehand!!

        when (gameLoadMode) {
            GameLoadMode.CREATE_NEW -> enter(gameLoadInfoPayload as NewWorldParameters)
            GameLoadMode.LOAD_FROM -> enter(gameLoadInfoPayload as GameSaveData)
        }
    }

    data class GameSaveData(
            val world: GameWorld,
            val historicalFigureIDBucket: ArrayList<Int>,
            val realGamePlayer: ActorHumanoid
    )

    data class NewWorldParameters(
            val width: Int,
            val height: Int,
            val worldGenSeed: Long
            // other worldgen options
    )

    /**
     * Init instance by loading saved world
     */
    private fun enter(gameSaveData: GameSaveData) {
        if (gameFullyLoaded) {
            Error("You are doing horribly wrong, fucknugget.")
        }

        world = gameSaveData.world
        historicalFigureIDBucket = gameSaveData.historicalFigureIDBucket
        playableActorDelegate = PlayableActorDelegate(gameSaveData.realGamePlayer)
        addNewActor(player!!)



        initGame()

        gameFullyLoaded = true
    }


    /**
     * Init instance by creating new world
     */
    private fun enter(worldParams: NewWorldParameters) {

        // init map as chosen size
        world = GameWorld(worldParams.width, worldParams.height)

        // generate terrain for the map
        WorldGenerator.attachMap(world)
        WorldGenerator.SEED = worldParams.worldGenSeed
        WorldGenerator.generateMap()


        historicalFigureIDBucket = ArrayList<Int>()


        RoguelikeRandomiser.seed = HQRNG().nextLong()


        // add new player and put it to actorContainer
        playableActorDelegate = PlayableActorDelegate(PlayerBuilderSigrid())
        //playableActorDelegate = PlayableActorDelegate(PlayerBuilderTestSubject1())
        addNewActor(player!!)


        // test actor
        //addNewActor(PlayerBuilderCynthia())



        initGame()
    }

    fun initGame() {

        Gdx.input.inputProcessor = GameController


        initViewPort(Terrarum.WIDTH, Terrarum.HEIGHT)


        // init console window
        consoleHandler = UIHandler(ConsoleWindow())
        consoleHandler.setPosition(0, 0)


        // init debug window
        debugWindow = UIHandler(BasicDebugInfoWindow())
        debugWindow.setPosition(0, 0)

        // init notifier
        notifier = UIHandler(Notification())
        notifier.UI.handler = notifier
        notifier.setPosition(
                (Terrarum.WIDTH - notifier.UI.width) / 2, Terrarum.HEIGHT - notifier.UI.height)




        // >- queue up game UIs that should pause the world -<
        // inventory
        uiInventoryPlayer = UIHandler(
                UIInventory(player,
                        width = 840,
                        height = Terrarum.HEIGHT - 160,
                        categoryWidth = 210
                ),
                toggleKey = Terrarum.getConfigInt("keyinventory")
        )
        uiInventoryPlayer.setPosition(
                -uiInventoryPlayer.UI.width,
                70
        )

        // >- lesser UIs -<
        // quick bar
        uiQuickBar = UIHandler(UIQuickBar())
        uiQuickBar.isVisible = true
        uiQuickBar.setPosition(0, 0)

        // pie menu
        uiPieMenu = UIHandler(UIPieMenu())
        uiPieMenu.setPosition(Terrarum.HALFW, Terrarum.HALFH)

        // vital metre
        // fill in getter functions by
        //      (uiAliases[UI_QUICK_BAR]!!.UI as UIVitalMetre).vitalGetterMax = { some_function }
        //uiVitalPrimary = UIHandler(UIVitalMetre(player, { 80f }, { 100f }, Color.red, 2), customPositioning = true)
        //uiVitalPrimary.setAsAlwaysVisible()
        //uiVitalSecondary = UIHandler(UIVitalMetre(player, { 73f }, { 100f }, Color(0x00dfff), 1), customPositioning = true)
        //uiVitalSecondary.setAsAlwaysVisible()
        //uiVitalItem = UIHandler(UIVitalMetre(player, { null }, { null }, Color(0xffcc00), 0), customPositioning = true)
        //uiVitalItem.setAsAlwaysVisible()

        // basic watch-style notification bar (temperature, new mail)
        uiWatchBasic = UIHandler(UIBasicNotifier(player))
        uiWatchBasic.setAsAlwaysVisible()
        uiWatchBasic.setPosition(Terrarum.WIDTH - uiWatchBasic.UI.width, 0)

        uiWatchTierOne = UIHandler(UITierOneWatch(player))
        uiWatchTierOne.setAsAlwaysVisible()
        uiWatchTierOne.setPosition(Terrarum.WIDTH - uiWatchTierOne.UI.width, uiWatchBasic.UI.height - 2)


        // batch-process uiAliases
        uiAliases = arrayListOf(
                // drawn first
                //uiVitalPrimary,
                //uiVitalSecondary,
                //uiVitalItem,
                uiPieMenu,
                uiQuickBar,
                uiWatchBasic,
                uiWatchTierOne
                // drawn last
        )
        uiAlasesPausing = arrayListOf(
                uiInventoryPlayer,
                //uiInventoryContainer,
                consoleHandler
        )
        uiAlasesPausing.forEach { addUI(it) } // put them all to the UIContainer
        uiAliases.forEach { addUI(it) } // put them all to the UIContainer



        ingameUpdateThread = ThreadIngameUpdate(this)
        updateThreadWrapper = Thread(ingameUpdateThread, "Terrarum UpdateThread")


        LightmapRenderer.fireRecalculateEvent()
    }// END enter


    protected var updateDeltaCounter = 0.0
    protected val updateRate = 1.0 / Terrarum.TARGET_INTERNAL_FPS

    private var firstTimeRun = true

    ///////////////
    // prod code //
    ///////////////
    private class ThreadIngameUpdate(val ingame: Ingame): Runnable {
        override fun run() {
            while (ingame.updateDeltaCounter >= ingame.updateRate) {
                ingame.updateGame(Gdx.graphics.deltaTime)
                ingame.updateDeltaCounter -= ingame.updateRate
            }
        }
    }

    override fun render(delta: Float) {
        Gdx.graphics.setTitle(GAME_NAME +
                              " — F: ${Gdx.graphics.framesPerSecond} (${Terrarum.TARGET_INTERNAL_FPS})" +
                              " — M: ${Terrarum.memInUse}M / ${Terrarum.memTotal}M / ${Terrarum.memXmx}M"
        )

        // ASYNCHRONOUS UPDATE AND RENDER //


        /** UPDATE CODE GOES HERE */
        updateDeltaCounter += delta



        if (false && Terrarum.getConfigBoolean("multithread")) { // NO MULTITHREADING: camera don't like concurrent modification (jittery actor movements)
            if (firstTimeRun || updateThreadWrapper.state == Thread.State.TERMINATED) {
                updateThreadWrapper = Thread(ingameUpdateThread, "Terrarum UpdateThread")
                updateThreadWrapper.start()

                if (firstTimeRun) firstTimeRun = false
            }
            // else, NOP;
        }
        else {
            while (updateDeltaCounter >= updateRate) {
                updateGame(delta)
                updateDeltaCounter -= updateRate
            }
        }



        /** RENDER CODE GOES HERE */
        renderGame(batch)
    }

    protected fun updateGame(delta: Float) {
        particlesActive = 0


        KeyToggler.update()
        GameController.processInput(delta)


        if (!paused) {

            ///////////////////////////
            // world-related updates //
            ///////////////////////////
            BlockPropUtil.dynamicLumFuncTickClock()
            world.updateWorldTime(delta)
            //WorldSimulator(player, delta)
            WeatherMixer.update(delta)
            BlockStats.update()
            if (!(CommandDict["setgl"] as SetGlobalLightOverride).lightOverride)
                world.globalLight = WeatherMixer.globalLightNow


            ///////////////////////////
            // input-related updates //
            ///////////////////////////
            uiContainer.forEach { it.processInput(delta) }


            ////////////////////////////
            // camera-related updates //
            ////////////////////////////
            FeaturesDrawer.update(delta)
            WorldCamera.update()



            ///////////////////////////
            // actor-related updates //
            ///////////////////////////
            repossessActor()

            // determine whether the inactive actor should be activated
            wakeDormantActors()
            // determine whether the actor should keep being activated or be dormant
            KillOrKnockdownActors()
            updateActors(delta)
            particlesContainer.forEach { if (!it.flagDespawn) particlesActive++; it.update(delta) }
            // TODO thread pool(?)
            CollisionSolver.process()
        }


        ////////////////////////
        // ui-related updates //
        ////////////////////////
        uiContainer.forEach { it.update(delta) }
        debugWindow.update(delta)
        notifier.update(delta)

        // update debuggers using javax.swing //
        if (Authenticator.b()) {
            AVTracker.update()
            ActorsList.update()
        }
    }


    private var blurWriteBuffer = lightmapFboA
    private var blurReadBuffer = lightmapFboB

    private fun renderGame(batch: SpriteBatch) {
        Gdx.gl.glClearColor(.157f, .157f, .157f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        //camera.position.set(-WorldCamera.x.toFloat(), -WorldCamera.y.toFloat(), 0f) // make camara work
        //camera.position.set(0f, 0f, 0f) // make camara work
        //batch.projectionMatrix = camera.combined



        // Post-update; ones that needs everything is completed //
        FeaturesDrawer.render(batch)                            //
        // update lightmap on every other frames, OR full-frame if the option is true
        if (Terrarum.getConfigBoolean("fullframelightupdate") or (Terrarum.GLOBAL_RENDER_TIMER % 2 == 1)) {       //
            LightmapRenderer.fireRecalculateEvent()             //
        }                                                       //
        // end of post-update                                   /



        worldDrawFrameBuffer.inAction(null, null) {
            Gdx.gl.glClearColor(0f,0f,0f,0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }
        worldGlowFrameBuffer.inAction(null, null) {
            Gdx.gl.glClearColor(0f,0f,0f,1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }




        ///////////////////////////
        // draw world to the FBO //
        ///////////////////////////
        processBlur(LightmapRenderer.DRAW_FOR_RGB)

        worldDrawFrameBuffer.inAction(camera, batch) {
            batch.inUse {
                batch.shader = null

                // using custom code for camera; this is obscure and tricky
                camera.position.set(WorldCamera.gdxCamX, WorldCamera.gdxCamY, 0f) // make camara work
                camera.update()
                batch.projectionMatrix = camera.combined


                batch.color = Color.WHITE
                blendNormal()

                BlocksDrawer.renderWall(batch)
                actorsRenderBehind.forEach { it.drawBody(batch) }
                particlesContainer.forEach { it.drawBody(batch) }
                BlocksDrawer.renderTerrain(batch)

                /////////////////
                // draw actors //
                /////////////////
                actorsRenderMiddle.forEach { it.drawBody(batch) }
                actorsRenderMidTop.forEach { it.drawBody(batch) }
                player?.drawBody(batch)
                actorsRenderFront.forEach { it.drawBody(batch) }
                // --> Change of blend mode <-- introduced by childs of ActorWithBody //


                /////////////////////////////
                // draw map related stuffs //
                /////////////////////////////

                BlocksDrawer.renderFront(batch, false)
                // --> blendNormal() <-- by BlocksDrawer.renderFront
                FeaturesDrawer.drawEnvOverlay(batch)


                // mix lighpmap canvas to this canvas (Colors -- RGB channel)
                if (!KeyToggler.isOn(Input.Keys.F6)) { // F6 to disable lightmap draw
                    setCameraPosition(0f, 0f)
                    batch.shader = null

                    val lightTex = blurWriteBuffer.colorBufferTexture // TODO zoom!

                    lightTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)

                    if (KeyToggler.isOn(KEY_LIGHTMAP_RENDER)) blendNormal()
                    else blendMul()

                    batch.color = Color.WHITE
                    batch.draw(lightTex,
                            0f, 0f,
                            lightTex.width * lightmapDownsample, lightTex.height * lightmapDownsample
                    )

                }


                batch.shader = null


                // move camera back to its former position
                // using custom code for camera; this is obscure and tricky
                camera.position.set(WorldCamera.gdxCamX, WorldCamera.gdxCamY, 0f) // make camara work
                camera.update()
                batch.projectionMatrix = camera.combined

            }
        }


        //////////////////////////
        // draw glow to the FBO //
        //////////////////////////
        processBlur(LightmapRenderer.DRAW_FOR_ALPHA)

        worldGlowFrameBuffer.inAction(camera, batch) {
            batch.inUse {
                batch.shader = null

                // using custom code for camera; this is obscure and tricky
                camera.position.set(WorldCamera.gdxCamX, WorldCamera.gdxCamY, 0f) // make camara work
                camera.update()
                batch.projectionMatrix = camera.combined


                batch.color = Color.WHITE
                blendNormal()


                //////////////////////
                // draw actor glows //
                //////////////////////

                actorsRenderBehind.forEach { it.drawGlow(batch) }
                particlesContainer.forEach { it.drawGlow(batch) }
                actorsRenderMiddle.forEach { it.drawGlow(batch) }
                actorsRenderMidTop.forEach { it.drawGlow(batch) }
                player?.drawGlow(batch)
                actorsRenderFront.forEach { it.drawGlow(batch) }
                // --> blendNormal() <-- introduced by childs of ActorWithBody //


                // mix lighpmap canvas to this canvas (UV lights -- A channel)
                if (!KeyToggler.isOn(Input.Keys.F6)) { // F6 to disable lightmap draw
                    setCameraPosition(0f, 0f)
                    batch.shader = null

                    val lightTex = blurWriteBuffer.colorBufferTexture // TODO zoom!

                    lightTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)

                    if (KeyToggler.isOn(KEY_LIGHTMAP_RENDER)) blendNormal()
                    else blendMul()

                    batch.color = Color.WHITE
                    batch.draw(lightTex,
                            0f, 0f,
                            lightTex.width * lightmapDownsample, lightTex.height * lightmapDownsample
                    )

                }


                blendNormal()
            }
        }


        worldBlendFrameBuffer.inAction(camera, batch) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

            val worldTex = worldDrawFrameBuffer.colorBufferTexture // WORLD: light_color must be applied beforehand
            val glowTex = worldGlowFrameBuffer.colorBufferTexture // GLOW: light_uvlight must be applied beforehand

            worldTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            glowTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)

            //Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)
            worldTex.bind(0)
            glowTex.bind(1)


            /*val quad = Mesh(
                    true, 4, 6,
                    VertexAttribute.Position(),
                    VertexAttribute.ColorUnpacked(),
                    VertexAttribute.TexCoords(0)
            )
            quad.setVertices(floatArrayOf(
                    0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f, 1f,
                    Terrarum.WIDTH.toFloat(), 0f, 0f, 1f, 1f, 1f, 1f, 1f, 1f,
                    Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT.toFloat(), 0f, 1f, 1f, 1f, 1f, 1f, 0f,
                    0f, Terrarum.HEIGHT.toFloat(), 0f, 1f, 1f, 1f, 1f, 0f, 0f
            ))
            quad.setIndices(shortArrayOf(0, 1, 2, 2, 3, 0))*/

            Terrarum.shaderBlendGlow.begin()
            Terrarum.shaderBlendGlow.setUniformMatrix("u_projTrans", camera.combined)
            Terrarum.shaderBlendGlow.setUniformi("u_texture", 0)
            Terrarum.shaderBlendGlow.setUniformi("tex1", 1)
            fullscreenQuad.render(Terrarum.shaderBlendGlow, GL20.GL_TRIANGLES)
            Terrarum.shaderBlendGlow.end()


            batch.inUse {
                batch.color = Color.WHITE
                blendNormal()

                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0) // reset active textureunit to zero (i don't know tbh, but it won't work without this)
                batch.shader = null
            }
        }


        /////////////////////////
        // draw to main screen //
        /////////////////////////
        camera.setToOrtho(true, Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT.toFloat())
        batch.projectionMatrix = camera.combined
        batch.inUse {
            batch.shader = null

            setCameraPosition(0f, 0f)
            batch.color = Color.WHITE
            blendNormal()


            /////////////////////////////////
            // draw framebuffers to screen //
            /////////////////////////////////
            WeatherMixer.render(batch)


            val blendedTex = worldBlendFrameBuffer.colorBufferTexture
            blendedTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            batch.color = Color.WHITE
            batch.shader = null
            blendNormal()
            batch.draw(blendedTex, 0f, 0f, blendedTex.width.toFloat(), blendedTex.height.toFloat())



            // an old code.
            /*batch.shader = null
            val worldTex = worldDrawFrameBuffer.colorBufferTexture // WORLD: light_color must be applied beforehand
            val glowTex = worldGlowFrameBuffer.colorBufferTexture // GLOW: light_uvlight must be applied beforehand


            batch.draw(worldTex, 0f, 0f, worldTex.width.toFloat(), worldTex.height.toFloat())*/



            batch.shader = null

            ////////////////////////
            // debug informations //
            ////////////////////////

            blendNormal()
            // draw reference ID if debugWindow is open
            if (debugWindow.isVisible) {

                actorContainer.forEachIndexed { i, actor ->
                    if (actor is ActorWithBody) {
                        batch.color = Color.WHITE
                        Terrarum.fontSmallNumbers.draw(batch,
                                actor.referenceID.toString(),
                                actor.hitbox.startX.toFloat(),
                                actor.hitbox.canonicalY.toFloat() + 4
                        )
                    }
                }
            }
            // debug physics
            if (KeyToggler.isOn(Input.Keys.F11)) {
                actorContainer.forEachIndexed { i, actor ->
                    if (actor is ActorWithPhysics) {
                        /*shapeRenderer.inUse(ShapeRenderer.ShapeType.Line) {
                            shapeRenderer.color = Color(1f, 0f, 1f, 1f)
                            //shapeRenderer.lineWidth = 1f
                            shapeRenderer.rect(
                                    actor.hitbox.startX.toFloat(),
                                    actor.hitbox.startY.toFloat(),
                                    actor.hitbox.width.toFloat(),
                                    actor.hitbox.height.toFloat()
                            )
                        }*/

                        // velocity
                        batch.color = Color.CHARTREUSE//GameFontBase.codeToCol["g"]
                        Terrarum.fontSmallNumbers.draw(batch,
                                "${0x7F.toChar()}X ${actor.externalForce.x}",
                                actor.hitbox.startX.toFloat(),
                                actor.hitbox.canonicalY.toFloat() + 4 + 8
                        )
                        Terrarum.fontSmallNumbers.draw(batch,
                                "${0x7F.toChar()}Y ${actor.externalForce.y}",
                                actor.hitbox.startX.toFloat(),
                                actor.hitbox.canonicalY.toFloat() + 4 + 8 * 2
                        )
                    }
                }
            }
            // fluidmap debug
            if (KeyToggler.isOn(Input.Keys.F4)) {
                WorldSimulator.drawFluidMapDebug(batch)
            }




            /////////////////////////////
            // draw some overlays (UI) //
            /////////////////////////////
            uiContainer.forEach { if (it != consoleHandler) it.render(batch) }

            debugWindow.render(batch)
            // make sure console draws on top of other UIs
            consoleHandler.render(batch)
            notifier.render(batch)


            blendNormal()
        }



        //      //////    ////    //      ////  //  //
        //      //      //      //  //  //      //  //
        //      ////    //  //  //  //  //        ////
        //      //      //  //  //////  //          //
        //////  //////    ////  //  //    ////  ////


        /////////////////////////////
        // draw map related stuffs //
        /////////////////////////////
        /*worldDrawFrameBuffer.inAction {

            batch.inUse {
                camera.position.set(WorldCamera.x.toFloat(), WorldCamera.y.toFloat(), 0f) // make camara work
                camera.update()
                batch.projectionMatrix = camera.combined


                batch.color = Color.WHITE
                batch.fillRect(WorldCamera.x.toFloat(), WorldCamera.y.toFloat(), 16f, 16f)



                BlocksDrawer.renderWall(batch)
                actorsRenderBehind.forEach { it.drawBody(batch) }
                actorsRenderBehind.forEach { it.drawGlow(batch) }
                particlesContainer.forEach { it.drawBody(batch) }
                particlesContainer.forEach { it.drawGlow(batch) }
                BlocksDrawer.renderTerrain(batch)

                /////////////////
                // draw actors //
                /////////////////
                actorsRenderMiddle.forEach { it.drawBody(batch) }
                actorsRenderMidTop.forEach { it.drawBody(batch) }
                player?.drawBody(batch)
                actorsRenderFront.forEach { it.drawBody(batch) }
                // --> Change of blend mode <-- introduced by childs of ActorWithBody //


                /////////////////////////////
                // draw map related stuffs //
                /////////////////////////////
                LightmapRenderer.renderLightMap()

                BlocksDrawer.renderFront(batch, false)
                // --> blendNormal() <-- by BlocksDrawer.renderFront
                FeaturesDrawer.render(batch)


                FeaturesDrawer.drawEnvOverlay(batch)

                if (!KeyToggler.isOn(KEY_LIGHTMAP_RENDER)) blendMul()
                else blendNormal()
                LightmapRenderer.draw(batch)


                //////////////////////
                // draw actor glows //
                //////////////////////
                actorsRenderMiddle.forEach { it.drawGlow(batch) }
                actorsRenderMidTop.forEach { it.drawGlow(batch) }
                player?.drawGlow(batch)
                actorsRenderFront.forEach { it.drawGlow(batch) }
                // --> blendLightenOnly() <-- introduced by childs of ActorWithBody //


                ////////////////////////
                // debug informations //
                ////////////////////////
                blendNormal()
                // draw reference ID if debugWindow is open
                if (debugWindow.isVisible) {
                    actorContainer.forEachIndexed { i, actor ->
                        if (actor is ActorWithBody) {
                            batch.color = Color.WHITE
                            Terrarum.fontSmallNumbers.draw(batch,
                                    actor.referenceID.toString(),
                                    actor.hitbox.startX.toFloat(),
                                    actor.hitbox.canonicalY.toFloat() + 4
                            )
                        }
                    }
                }
                // debug physics
                if (KeyToggler.isOn(Key.F11)) {
                    actorContainer.forEachIndexed { i, actor ->
                        if (actor is ActorWithPhysics) {
                            /*shapeRenderer.inUse(ShapeRenderer.ShapeType.Line) {
                                shapeRenderer.color = Color(1f, 0f, 1f, 1f)
                                //shapeRenderer.lineWidth = 1f
                                shapeRenderer.rect(
                                        actor.hitbox.startX.toFloat(),
                                        actor.hitbox.startY.toFloat(),
                                        actor.hitbox.width.toFloat(),
                                        actor.hitbox.height.toFloat()
                                )
                            }*/

                            // velocity
                            batch.color = Color.CHARTREUSE//GameFontBase.codeToCol["g"]
                            Terrarum.fontSmallNumbers.draw(batch,
                                    "${0x7F.toChar()}X ${actor.externalForce.x}",
                                    actor.hitbox.startX.toFloat(),
                                    actor.hitbox.canonicalY.toFloat() + 4 + 8
                            )
                            Terrarum.fontSmallNumbers.draw(batch,
                                    "${0x7F.toChar()}Y ${actor.externalForce.y}",
                                    actor.hitbox.startX.toFloat(),
                                    actor.hitbox.canonicalY.toFloat() + 4 + 8 * 2
                            )
                        }
                    }
                }
                // fluidmap debug
                if (KeyToggler.isOn(Key.F4))
                    WorldSimulator.drawFluidMapDebug(batch)


            }
        }
        /////////////////
        // GUI Predraw //
        /////////////////
        //worldG.flush()
        batch.inUse {
            val tex = backDrawFrameBuffer.colorBufferTexture // TODO zoom!
            batch.draw(tex, 0f, 0f)
        }*/
        //backG.drawImage(worldDrawFrameBuffer.getScaledCopy(screenZoom), 0f, 0f)
        //backG.flush()


        /////////////////////
        // draw UIs  ONLY! //
        /////////////////////
        /*batch.inUse {
            uiContainer.forEach { if (it != consoleHandler) it.render(batch) }
            debugWindow.render(batch)
            // make sure console draws on top of other UIs
            consoleHandler.render(batch)
            notifier.render(batch)
        }*/


        //////////////////
        // GUI Postdraw //
        //////////////////
        //backG.flush()
        //gwin.drawImage(backDrawFrameBuffer, 0f, 0f)



        // centre marker
        /*gwin.color = Color(0x00FFFF)
        gwin.lineWidth = 1f
        gwin.drawLine(Terrarum.WIDTH / 2f, 0f, Terrarum.WIDTH / 2f, Terrarum.HEIGHT.toFloat())
        gwin.drawLine(0f, Terrarum.HEIGHT / 2f, Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT / 2f)*/
    }

    private fun processBlur(mode: Int) {
        val blurIterations = 5 // ideally, 4 * radius; must be even/odd number -- odd/even number will flip the image
        val blurRadius = 4f / lightmapDownsample // (5, 4f); using low numbers for pixel-y aesthetics

        blurWriteBuffer = lightmapFboA
        blurReadBuffer = lightmapFboB


        lightmapFboA.inAction(null, null) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }
        lightmapFboB.inAction(null, null) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }


        if (mode == LightmapRenderer.DRAW_FOR_RGB) {
            // initialise readBuffer with untreated lightmap
            blurReadBuffer.inAction(camera, batch) {
                batch.inUse {
                    // using custom code for camera; this is obscure and tricky
                    camera.position.set(
                            (WorldCamera.gdxCamX / lightmapDownsample).round(),
                            (WorldCamera.gdxCamY / lightmapDownsample).round(),
                            0f
                    ) // make camara work
                    camera.update()
                    batch.projectionMatrix = camera.combined


                    blendNormal()
                    batch.color = Color.WHITE
                    LightmapRenderer.draw(batch, LightmapRenderer.DRAW_FOR_RGB)
                }
            }
        }
        else {
            // initialise readBuffer with untreated lightmap
            blurReadBuffer.inAction(camera, batch) {
                batch.inUse {
                    // using custom code for camera; this is obscure and tricky
                    camera.position.set(
                            (WorldCamera.gdxCamX / lightmapDownsample).round(),
                            (WorldCamera.gdxCamY / lightmapDownsample).round(),
                            0f
                    ) // make camara work
                    camera.update()
                    batch.projectionMatrix = camera.combined


                    blendNormal()
                    batch.color = Color.WHITE
                    LightmapRenderer.draw(batch, LightmapRenderer.DRAW_FOR_ALPHA)
                }
            }
        }



        if (!KeyToggler.isOn(Input.Keys.F8)) {
            for (i in 0 until blurIterations) {
                blurWriteBuffer.inAction(camera, batch) {

                    batch.inUse {
                        val texture = blurReadBuffer.colorBufferTexture

                        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)


                        batch.shader = Terrarum.shaderBlur
                        batch.shader.setUniformf("iResolution",
                                blurWriteBuffer.width.toFloat(), blurWriteBuffer.height.toFloat())
                        batch.shader.setUniformf("flip", 1f)
                        if (i % 2 == 0)
                            batch.shader.setUniformf("direction", blurRadius, 0f)
                        else
                            batch.shader.setUniformf("direction", 0f, blurRadius)


                        batch.color = Color.WHITE
                        batch.draw(texture, 0f, 0f)


                        // swap
                        val t = blurWriteBuffer
                        blurWriteBuffer = blurReadBuffer
                        blurReadBuffer = t
                    }
                }
            }
        }
        else {
            blurWriteBuffer = blurReadBuffer
        }
    }



    private fun repossessActor() {
        // check if currently pocessed actor is removed from game
        if (!theGameHasActor(player)) {
            // re-possess canonical player
            if (theGameHasActor(Player.PLAYER_REF_ID))
                changePossession(Player.PLAYER_REF_ID)
            else
                changePossession(0x51621D) // FIXME fallback debug mode (FIXME is there for a reminder visible in ya IDE)
        }
    }

    private fun changePossession(newActor: PlayableActorDelegate) {
        if (!theGameHasActor(player)) {
            throw IllegalArgumentException("No such actor in the game: $newActor")
        }

        playableActorDelegate = newActor
        WorldSimulator(player, Gdx.graphics.deltaTime)
    }

    private fun changePossession(refid: Int) {
        // TODO prevent possessing other player on multiplayer

        if (!theGameHasActor(refid)) {
            throw IllegalArgumentException(
                    "No such actor in the game: $refid (elemsActive: ${actorContainer.size}, " +
                    "elemsInactive: ${actorContainerInactive.size})")
        }

        // take care of old delegate
        playableActorDelegate!!.actor.collisionType = HumanoidNPC.DEFAULT_COLLISION_TYPE
        // accept new delegate
        playableActorDelegate = PlayableActorDelegate(getActorByID(refid) as ActorHumanoid)
        playableActorDelegate!!.actor.collisionType = ActorWithPhysics.COLLISION_KINEMATIC
        WorldSimulator(player, Gdx.graphics.deltaTime)
    }

    /** Send message to notifier UI and toggle the UI as opened. */
    fun sendNotification(msg: Array<String>) {
        (notifier.UI as Notification).sendNotification(msg)
    }

    fun wakeDormantActors() {
        var actorContainerSize = actorContainerInactive.size
        var i = 0
        while (i < actorContainerSize) { // loop through actorContainerInactive
            val actor = actorContainerInactive[i]
            if (actor is ActorWithBody && actor.inUpdateRange()) {
                activateDormantActor(actor) // duplicates are checked here
                actorContainerSize -= 1
                i-- // array removed 1 elem, so we also decrement counter by 1
            }
            i++
        }
    }

    /**
     * determine whether the actor should be active or dormant by its distance from the player.
     * If the actor must be dormant, the target actor will be put to the list specifically for them.
     * if the actor is not to be dormant, it will be just ignored.
     */
    fun KillOrKnockdownActors() {
        var actorContainerSize = actorContainer.size
        var i = 0
        while (i < actorContainerSize) { // loop through actorContainer
            val actor = actorContainer[i]
            val actorIndex = i
            // kill actors flagged to despawn
            if (actor.flagDespawn) {
                removeActor(actor)
                actorContainerSize -= 1
                i-- // array removed 1 elem, so we also decrement counter by 1
            }
            // inactivate distant actors
            else if (actor is ActorWithBody && !actor.inUpdateRange()) {
                if (actor !is Projectile) { // if it's a projectile, don't inactivate it; just kill it.
                    actorContainerInactive.add(actor) // naïve add; duplicates are checked when the actor is re-activated
                }
                actorContainer.removeAt(actorIndex)
                actorContainerSize -= 1
                i-- // array removed 1 elem, so we also decrement counter by 1
            }
            i++
        }
    }

    /**
     * Update actors concurrently.
     *
     * NOTE: concurrency for actor updating is currently disabled because of it's poor performance
     */
    fun updateActors(delta: Float) {
        if (false) { // don't multithread this for now, it's SLOWER //if (Terrarum.MULTITHREAD && actorContainer.size > Terrarum.THREADS) {
            val actors = actorContainer.size.toFloat()
            // set up indices
            for (i in 0..Terrarum.THREADS - 1) {
                ThreadParallel.map(
                        i,
                        ThreadActorUpdate(
                                actors.div(Terrarum.THREADS).times(i).roundInt(),
                                actors.div(Terrarum.THREADS).times(i.plus(1)).roundInt() - 1
                        ),
                        "ActorUpdate"
                )
            }

            ThreadParallel.startAll()

            playableActorDelegate?.update(delta)
        }
        else {
            actorContainer.forEach {
                if (it != playableActorDelegate?.actor) {
                    it.update(delta)

                    if (it is Pocketed) {
                        it.inventory.forEach { inventoryEntry ->
                            inventoryEntry.item.effectWhileInPocket(delta)
                            if (it.equipped(inventoryEntry.item)) {
                                inventoryEntry.item.effectWhenEquipped(delta)
                            }
                        }
                    }
                }
            }
            playableActorDelegate?.update(delta)
            //AmmoMeterProxy(player!!, uiVitalItem.UI as UIVitalMetre)
        }
    }

    fun Double.sqr() = this * this
    fun Int.sqr() = this * this
    fun min(vararg d: Double): Double {
        var ret = Double.MAX_VALUE
        d.forEach { if (it < ret) ret = it }
        return ret
    }
    private fun distToActorSqr(a: ActorWithBody, p: ActorWithBody) =
            min(// take min of normal position and wrapped (x < 0) position
                    (a.hitbox.centeredX - p.hitbox.centeredX).sqr() +
                    (a.hitbox.centeredY - p.hitbox.centeredY).sqr(),
                    (a.hitbox.centeredX - p.hitbox.centeredX + world.width * FeaturesDrawer.TILE_SIZE).sqr() +
                    (a.hitbox.centeredY - p.hitbox.centeredY).sqr(),
                    (a.hitbox.centeredX - p.hitbox.centeredX - world.width * FeaturesDrawer.TILE_SIZE).sqr() +
                    (a.hitbox.centeredY - p.hitbox.centeredY).sqr()
            )
    private fun distToCameraSqr(a: ActorWithBody) =
            min(
                    (a.hitbox.startX - WorldCamera.x).sqr() +
                    (a.hitbox.startY - WorldCamera.y).sqr(),
                    (a.hitbox.startX - WorldCamera.x + world.width * FeaturesDrawer.TILE_SIZE).sqr() +
                    (a.hitbox.startY - WorldCamera.y).sqr(),
                    (a.hitbox.startX - WorldCamera.x - world.width * FeaturesDrawer.TILE_SIZE).sqr() +
                    (a.hitbox.startY - WorldCamera.y).sqr()
            )

    /** whether the actor is within screen */
    private fun ActorWithBody.inScreen() =
            distToCameraSqr(this) <=
            (Terrarum.WIDTH.plus(this.hitbox.width.div(2)).
                    times(1 / Terrarum.ingame!!.screenZoom).sqr() +
             Terrarum.HEIGHT.plus(this.hitbox.height.div(2)).
                     times(1 / Terrarum.ingame!!.screenZoom).sqr())


    /** whether the actor is within update range */
    private fun ActorWithBody.inUpdateRange() = distToCameraSqr(this) <= ACTOR_UPDATE_RANGE.sqr()

    /**
     * actorContainer extensions
     */
    fun theGameHasActor(actor: Actor?) = if (actor == null) false else theGameHasActor(actor.referenceID)

    fun theGameHasActor(ID: Int): Boolean =
            isActive(ID) || isInactive(ID)

    fun isActive(ID: Int): Boolean =
            if (actorContainer.size == 0)
                false
            else
                actorContainer.binarySearch(ID) >= 0

    fun isInactive(ID: Int): Boolean =
            if (actorContainerInactive.size == 0)
                false
            else
                actorContainerInactive.binarySearch(ID) >= 0

    fun removeActor(ID: Int) = removeActor(getActorByID(ID))
    /**
     * get index of the actor and delete by the index.
     * we can do this as the list is guaranteed to be sorted
     * and only contains unique values.
     *
     * Any values behind the index will be automatically pushed to front.
     * This is how remove function of [java.util.ArrayList] is defined.
     */
    fun removeActor(actor: Actor) {
        if (actor.referenceID == player?.referenceID || actor.referenceID == 0x51621D) // do not delete this magic
            throw RuntimeException("Attempted to remove player.")
        val indexToDelete = actorContainer.binarySearch(actor.referenceID)
        if (indexToDelete >= 0) {
            actorContainer.removeAt(indexToDelete)

            // indexToDelete >= 0 means that the actor certainly exists in the game
            // which means we don't need to check if i >= 0 again
            if (actor is ActorWithBody) {
                when (actor.renderOrder) {
                    Actor.RenderOrder.BEHIND -> {
                        val i = actorsRenderBehind.binarySearch(actor.referenceID)
                        actorsRenderBehind.removeAt(i)
                    }
                    Actor.RenderOrder.MIDDLE -> {
                        val i = actorsRenderMiddle.binarySearch(actor.referenceID)
                        actorsRenderMiddle.removeAt(i)
                    }
                    Actor.RenderOrder.MIDTOP -> {
                        val i = actorsRenderMidTop.binarySearch(actor.referenceID)
                        actorsRenderMidTop.removeAt(i)
                    }
                    Actor.RenderOrder.FRONT  -> {
                        val i = actorsRenderFront.binarySearch(actor.referenceID)
                        actorsRenderFront.removeAt(i)
                    }
                }
            }
        }
    }

    /**
     * Check for duplicates, append actor and sort the list
     */
    fun addNewActor(actor: Actor) {
        if (theGameHasActor(actor.referenceID)) {
            throw Error("The actor $actor already exists in the game")
        }
        else {
            actorContainer.add(actor)
            insertionSortLastElem(actorContainer) // we can do this as we are only adding single actor

            if (actor is ActorWithBody) {
                when (actor.renderOrder) {
                    Actor.RenderOrder.BEHIND -> {
                        actorsRenderBehind.add(actor); insertionSortLastElemAV(actorsRenderBehind)
                    }
                    Actor.RenderOrder.MIDDLE -> {
                        actorsRenderMiddle.add(actor); insertionSortLastElemAV(actorsRenderMiddle)
                    }
                    Actor.RenderOrder.MIDTOP -> {
                        actorsRenderMidTop.add(actor); insertionSortLastElemAV(actorsRenderMidTop)
                    }
                    Actor.RenderOrder.FRONT  -> {
                        actorsRenderFront.add(actor); insertionSortLastElemAV(actorsRenderFront)
                    }
                }
            }
        }
    }

    fun activateDormantActor(actor: Actor) {
        if (!isInactive(actor.referenceID)) {
            if (isActive(actor.referenceID))
                throw Error("The actor $actor is already activated")
            else
                throw Error("The actor $actor already exists in the game")
        }
        else {
            actorContainerInactive.remove(actor)
            actorContainer.add(actor)
            insertionSortLastElem(actorContainer) // we can do this as we are only adding single actor

            if (actor is ActorWithBody) {
                when (actor.renderOrder) {
                    Actor.RenderOrder.BEHIND -> {
                        actorsRenderBehind.add(actor); insertionSortLastElemAV(actorsRenderBehind)
                    }
                    Actor.RenderOrder.MIDDLE -> {
                        actorsRenderMiddle.add(actor); insertionSortLastElemAV(actorsRenderMiddle)
                    }
                    Actor.RenderOrder.MIDTOP -> {
                        actorsRenderMidTop.add(actor); insertionSortLastElemAV(actorsRenderMidTop)
                    }
                    Actor.RenderOrder.FRONT  -> {
                        actorsRenderFront.add(actor); insertionSortLastElemAV(actorsRenderFront)
                    }
                }
            }
        }
    }

    fun addParticle(particle: ParticleBase) {
        particlesContainer.add(particle)
    }

    fun addUI(ui: UIHandler) {
        // check for exact duplicates
        if (uiContainer.contains(ui)) {
            throw IllegalArgumentException(
                    "Exact copy of the UI already exists: The instance of ${ui.UI.javaClass.simpleName}"
            )
        }

        uiContainer.add(ui)
    }

    fun getActorByID(ID: Int): Actor {
        if (actorContainer.size == 0 && actorContainerInactive.size == 0)
            throw IllegalArgumentException("Actor with ID $ID does not exist.")

        var index = actorContainer.binarySearch(ID)
        if (index < 0) {
            index = actorContainerInactive.binarySearch(ID)

            if (index < 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Actor with ID $ID does not exist.",
                        null, JOptionPane.ERROR_MESSAGE
                )
                throw IllegalArgumentException("Actor with ID $ID does not exist.")
            }
            else
                return actorContainerInactive[index]
        }
        else
            return actorContainer[index]
    }

    private fun insertionSortLastElem(arr: ArrayList<Actor>) {
        lock(ReentrantLock()) {
            var j = arr.lastIndex - 1
            val x = arr.last()
            while (j >= 0 && arr[j] > x) {
                arr[j + 1] = arr[j]
                j -= 1
            }
            arr[j + 1] = x
        }
    }
    private fun insertionSortLastElemAV(arr: ArrayList<ActorWithBody>) { // out-projection doesn't work, duh
        lock(ReentrantLock()) {
            var j = arr.lastIndex - 1
            val x = arr.last()
            while (j >= 0 && arr[j] > x) {
                arr[j + 1] = arr[j]
                j -= 1
            }
            arr[j + 1] = x
        }
    }

    private fun ArrayList<*>.binarySearch(actor: Actor) = this.binarySearch(actor.referenceID)

    private fun ArrayList<*>.binarySearch(ID: Int): Int {
        // code from collections/Collections.kt
        var low = 0
        var high = this.size - 1

        while (low <= high) {
            val mid = (low + high).ushr(1) // safe from overflows

            val midVal = get(mid)!!

            if (ID > midVal.hashCode())
                low = mid + 1
            else if (ID < midVal.hashCode())
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1)  // key not found
    }

    inline fun lock(lock: Lock, body: () -> Unit) {
        lock.lock()
        try {
            body()
        }
        finally {
            lock.unlock()
        }
    }

    override fun pause() {
        // TODO no pause when off-focus on desktop
    }

    override fun resume() {
    }

    override fun hide() {
        dispose()
    }

    /**
     * @param width same as Terrarum.WIDTH
     * @param height same as Terrarum.HEIGHT
     * @see net.torvald.terrarum.Terrarum
     */
    override fun resize(width: Int, height: Int) {
        worldDrawFrameBuffer.dispose()
        worldDrawFrameBuffer = FrameBuffer(worldFBOformat, Terrarum.WIDTH, Terrarum.HEIGHT, true)
        worldGlowFrameBuffer.dispose()
        worldGlowFrameBuffer = FrameBuffer(worldFBOformat, Terrarum.WIDTH, Terrarum.HEIGHT, true)
        worldBlendFrameBuffer.dispose()
        worldBlendFrameBuffer = FrameBuffer(worldFBOformat, Terrarum.WIDTH, Terrarum.HEIGHT, true)
        lightmapFboA.dispose()
        lightmapFboA = FrameBuffer(lightFBOformat, Terrarum.WIDTH.div(lightmapDownsample.toInt()), Terrarum.HEIGHT.div(lightmapDownsample.toInt()), true)
        lightmapFboB.dispose()
        lightmapFboB = FrameBuffer(lightFBOformat, Terrarum.WIDTH.div(lightmapDownsample.toInt()), Terrarum.HEIGHT.div(lightmapDownsample.toInt()), true)


        // Set up viewport when window is resized
        initViewPort(width, height)



        // re-calculate fullscreen quad
        fullscreenQuad = Mesh(
                true, 4, 6,
                VertexAttribute.Position(),
                VertexAttribute.ColorUnpacked(),
                VertexAttribute.TexCoords(0)
        )
        fullscreenQuad.setVertices(floatArrayOf(
                0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f, 1f,
                Terrarum.WIDTH.toFloat(), 0f, 0f, 1f, 1f, 1f, 1f, 1f, 1f,
                Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT.toFloat(), 0f, 1f, 1f, 1f, 1f, 1f, 0f,
                0f, Terrarum.HEIGHT.toFloat(), 0f, 1f, 1f, 1f, 1f, 0f, 0f
        ))
        fullscreenQuad.setIndices(shortArrayOf(0, 1, 2, 2, 3, 0))



        if (gameFullyLoaded) {
            LightmapRenderer.fireRecalculateEvent()
        }
    }

    override fun dispose() {
        worldDrawFrameBuffer.dispose()
        worldGlowFrameBuffer.dispose()
        worldBlendFrameBuffer.dispose()
        lightmapFboA.dispose()
        lightmapFboB.dispose()

        actorsRenderBehind.forEach { it.dispose() }
        actorsRenderMiddle.forEach { it.dispose() }
        actorsRenderMidTop.forEach { it.dispose() }
        actorsRenderFront.forEach { it.dispose() }

        uiAliases.forEach { it.dispose() }
        uiAlasesPausing.forEach { it.dispose() }
    }


    /**
     * WARNING! this function flushes batch; use this sparingly!
     *
     * Camera will be moved so that (newX, newY) would be sit on the top-left edge.
     */
    fun setCameraPosition(newX: Float, newY: Float) {
        camera.position.set((-newX + Terrarum.HALFW).round(), (-newY + Terrarum.HALFH).round(), 0f)
        camera.update()
        batch.projectionMatrix = camera.combined
    }
}
