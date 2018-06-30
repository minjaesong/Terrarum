package net.torvald.terrarum

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.jme3.math.FastMath
import net.torvald.random.HQRNG
import net.torvald.terrarum.blockproperties.BlockCodex
import net.torvald.terrarum.gameactors.Actor
import net.torvald.terrarum.gameactors.ai.ActorAI
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.gameworld.fmod
import net.torvald.terrarum.langpack.Lang
import net.torvald.terrarum.modulebasegame.Ingame
import net.torvald.terrarum.modulebasegame.IngameRenderer
import net.torvald.terrarum.modulebasegame.gameactors.*
import net.torvald.terrarum.serialise.ReadLayerData
import net.torvald.terrarum.ui.UICanvas
import net.torvald.terrarum.modulebasegame.ui.UITitleRemoConRoot
import net.torvald.terrarum.weather.WeatherMixer
import net.torvald.terrarum.worlddrawer.*
import java.io.FileInputStream

/**
 * Created by minjaesong on 2017-09-02.
 */
class TitleScreen(val batch: SpriteBatch) : Screen {

    var camera = OrthographicCamera(Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT.toFloat())


    // invert Y
    fun initViewPort(width: Int, height: Int) {
        // Set Y to point downwards
        camera.setToOrtho(true, width.toFloat(), height.toFloat())

        // Update camera matrix
        camera.update()

        // Set viewport to restrict drawing
        Gdx.gl20.glViewport(0, 0, width, height)
    }


    private var loadDone = false

    private lateinit var demoWorld: GameWorld
    private lateinit var cameraNodes: FloatArray // camera Y-pos
    private val cameraAI = object : ActorAI {
        private val axisMax = 1f

        private var firstTime = true

        override fun update(actor: Actor, delta: Float) {
            val actor = actor as HumanoidNPC

            // fuck
            val avSpeed = 1.0 // FIXME camera goes faster when FPS is high
            actor.actorValue[AVKey.SPEED] = avSpeed
            actor.actorValue[AVKey.ACCEL] = avSpeed / 6.0
            // end fuck



            val tileSize = FeaturesDrawer.TILE_SIZE.toFloat()
            val catmullRomTension = 0f

            // pan camera
            actor.moveRight(axisMax)


            val domainSize = demoWorld.width * tileSize
            val codomainSize = cameraNodes.size
            val x = actor.hitbox.canonicalX.toFloat()

            val p1 = (x / (domainSize / codomainSize)).floorInt() fmod cameraNodes.size
            val p0 = ((p1 - 1) fmod codomainSize) fmod cameraNodes.size
            val p2 = ((p1 + 1) fmod codomainSize) fmod cameraNodes.size
            val p3 = ((p1 + 2) fmod codomainSize) fmod cameraNodes.size
            val u: Float = 1f - (p2 - (x / (domainSize / codomainSize))) / (p2 - p1)

            //val targetYPos = FastMath.interpolateCatmullRom(u, catmullRomTension, cameraNodes[p0], cameraNodes[p1], cameraNodes[p2], cameraNodes[p3])
            val targetYPos = FastMath.interpolateLinear(u, cameraNodes[p1], cameraNodes[p2])
            val yDiff = targetYPos - actor.hitbox.canonicalY

            /*if (!firstTime) {
                actor.moveDown(yDiff.bipolarClamp(axisMax.toDouble()).toFloat())
            }
            else {
                actor.hitbox.setPosition(actor.hitbox.canonicalX, targetYPos.toDouble())
                firstTime = false
            }*/
            actor.hitbox.setPosition(actor.hitbox.canonicalX, targetYPos.toDouble()) // just move the cameraY to interpolated path


            //println("${actor.hitbox.canonicalX}, ${actor.hitbox.canonicalY}")
        }
    }
    private lateinit var cameraPlayer: HumanoidNPC

    private val gradWhiteTop = Color(0xf8f8f8ff.toInt())
    private val gradWhiteBottom = Color(0xd8d8d8ff.toInt())


    lateinit var logo: TextureRegion

    val uiContainer = ArrayList<UICanvas>()
    private lateinit var uiMenu: UICanvas

    private lateinit var worldFBO: FrameBuffer

    private val TILE_SIZE = FeaturesDrawer.TILE_SIZE
    private val TILE_SIZEF = TILE_SIZE.toFloat()

    private fun loadThingsWhileIntroIsVisible() {
        println("[TitleScreen] Intro pre-load")


        demoWorld = ReadLayerData(FileInputStream(ModMgr.getFile("basegame", "demoworld")))


        // construct camera nodes
        val nodeCount = 100
        cameraNodes = kotlin.FloatArray(nodeCount, { it ->
            val tileXPos = (demoWorld.width.toFloat() * it / nodeCount).floorInt()
            var travelDownCounter = 0
            while (!BlockCodex[demoWorld.getTileFromTerrain(tileXPos, travelDownCounter)].isSolid) {
                travelDownCounter += 4
            }
            travelDownCounter * FeaturesDrawer.TILE_SIZE.toFloat()
        })


        cameraPlayer = object : HumanoidNPC(demoWorld, cameraAI, GameDate(1, 1), usePhysics = false, forceAssignRefID = Player.PLAYER_REF_ID) {
            init {
                setHitboxDimension(2, 2, 0, 0)
                hitbox.setPosition(
                        HQRNG().nextInt(demoWorld.width) * FeaturesDrawer.TILE_SIZE.toDouble(),
                        0.0 // Y pos: placeholder; camera AI will take it over
                )
                noClip = true
            }
        }

        demoWorld.time.timeDelta = 150


        LightmapRenderer.world = demoWorld
        BlocksDrawer.world = demoWorld
        FeaturesDrawer.world = demoWorld


        uiMenu = UITitleRemoConRoot()
        uiMenu.setPosition(0, 0)
        uiMenu.setAsOpen()


        uiContainer.add(uiMenu)

        loadDone = true
    }


    override fun hide() {
    }

    override fun show() {
        println("[TitleScreen] atrniartsientsarinoetsar")

        initViewPort(Terrarum.WIDTH, Terrarum.HEIGHT)

        logo = TextureRegion(Texture(Gdx.files.internal("assets/graphics/logo_placeholder.tga")))
        logo.flip(false, true)


        Gdx.input.inputProcessor = TitleScreenController(this)


        worldFBO = FrameBuffer(Pixmap.Format.RGBA8888, Terrarum.WIDTH, Terrarum.HEIGHT, false)
    }


    private val introUncoverTime: Second = 0.3f
    private var introUncoverDeltaCounter = 0f
    private var updateDeltaCounter = 0.0
    protected val updateRate = 1.0 / Terrarum.TARGET_INTERNAL_FPS

    override fun render(delta: Float) {
        if (!loadDone) {
            loadThingsWhileIntroIsVisible()
        }
        else {
            // async update
            updateDeltaCounter += delta
            var updateTries = 0
            while (updateDeltaCounter >= updateRate) {
                updateScreen(delta)
                updateDeltaCounter -= updateRate
                updateTries++

                if (updateTries >= Terrarum.UPDATE_CATCHUP_MAX_TRIES) {
                    break
                }
            }

            // render? just do it anyway
            renderScreen()
        }
    }

    fun updateScreen(delta: Float) {
        Gdx.graphics.setTitle("WorldRenderTest" +
                              " — F: ${Gdx.graphics.framesPerSecond} (${Terrarum.TARGET_INTERNAL_FPS})" +
                              " — M: ${Terrarum.memInUse}M / ${Terrarum.memTotal}M / ${Terrarum.memXmx}M"
        )

        demoWorld.globalLight = WeatherMixer.globalLightNow
        demoWorld.updateWorldTime(delta)
        WeatherMixer.update(delta, cameraPlayer)
        cameraPlayer.update(delta)

        // worldcamera update AFTER cameraplayer in this case; the other way is just an exception for actual ingame SFX
        WorldCamera.update(demoWorld, cameraPlayer)


        // update UIs //
        uiContainer.forEach { it.update(delta) }



        LightmapRenderer.fireRecalculateEvent() // don't half-frame update; it will jitter!
    }

    fun renderScreen() {

        //camera.setToOrtho(true, Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT.toFloat())

        // render world
        Gdx.gl.glClearColor(.64f, .754f, .84f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)


        IngameRenderer.invoke(world = demoWorld, uisToDraw = uiContainer)


        batch.inUse {
            setCameraPosition(0f, 0f)
            batch.shader = null
            batch.color = Color.WHITE
            renderOverlayTexts()
        }
    }

    private fun renderOverlayTexts() {
        setCameraPosition(0f, 0f)
        blendNormal()
        batch.shader = null

        batch.color = Color.LIGHT_GRAY

        val COPYTING = arrayOf(
                AppLoader.COPYRIGHT_DATE_NAME,
                Lang["COPYRIGHT_GNU_GPL_3"]
        )

        COPYTING.forEachIndexed { index, s ->
            val textWidth = Terrarum.fontGame.getWidth(s)
            Terrarum.fontGame.draw(batch, s,
                    Terrarum.WIDTH - textWidth - 1f - 0.2f,
                    Terrarum.HEIGHT - Terrarum.fontGame.lineHeight * (COPYTING.size - index) - 1f
            )
        }
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
        // Set up viewport when window is resized
        initViewPort(Terrarum.WIDTH, Terrarum.HEIGHT)

        BlocksDrawer.resize(Terrarum.WIDTH, Terrarum.HEIGHT)
        LightmapRenderer.resize(Terrarum.WIDTH, Terrarum.HEIGHT)

        if (loadDone) {
            // resize UI by re-creating it (!!)
            uiMenu.resize(Terrarum.WIDTH, Terrarum.HEIGHT)
            //uiMenu.setPosition(0, UITitleRemoConRoot.menubarOffY)
            uiMenu.setPosition(0, 0) // shitty hack. Could be:
            // 1: Init code and resize code are different
            // 2: The UI is coded shit
        }

        IngameRenderer.resize(Terrarum.WIDTH, Terrarum.HEIGHT)
    }

    override fun dispose() {
        logo.texture.dispose()

        IngameRenderer.dispose()

        uiMenu.dispose()
    }



    fun setCameraPosition(newX: Float, newY: Float) {
        Ingame.setCameraPosition(batch, camera, newX, newY)
    }



    class TitleScreenController(val screen: TitleScreen) : InputAdapter() {
        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            screen.uiContainer.forEach { it.touchUp(screenX, screenY, pointer, button) }
            return true
        }

        override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
            screen.uiContainer.forEach { it.mouseMoved(screenX, screenY) }
            return true
        }

        override fun keyTyped(character: Char): Boolean {
            screen.uiContainer.forEach { it.keyTyped(character) }
            return true
        }

        override fun scrolled(amount: Int): Boolean {
            screen.uiContainer.forEach { it.scrolled(amount) }
            return true
        }

        override fun keyUp(keycode: Int): Boolean {
            screen.uiContainer.forEach { it.keyUp(keycode) }
            return true
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            screen.uiContainer.forEach { it.touchDragged(screenX, screenY, pointer) }
            return true
        }

        override fun keyDown(keycode: Int): Boolean {
            screen.uiContainer.forEach { it.keyDown(keycode) }
            return true
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            screen.uiContainer.forEach { it.touchDown(screenX, screenY, pointer, button) }
            return true
        }
    }
}