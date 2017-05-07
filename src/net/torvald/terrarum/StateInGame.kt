package net.torvald.terrarum

import net.torvald.dataclass.CircularArray
import net.torvald.imagefont.GameFontBase
import net.torvald.random.HQRNG
import net.torvald.terrarum.Terrarum.HALFH
import net.torvald.terrarum.Terrarum.HALFW
import net.torvald.terrarum.Terrarum.delta
import net.torvald.terrarum.concurrent.ThreadParallel
import net.torvald.terrarum.console.*
import net.torvald.terrarum.gameactors.ActorHumanoid
import net.torvald.terrarum.gameactors.*
import net.torvald.terrarum.gameactors.physicssolver.CollisionSolver
import net.torvald.terrarum.gamecontroller.GameController
import net.torvald.terrarum.gamecontroller.Key
import net.torvald.terrarum.gamecontroller.KeyToggler
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.gameworld.WorldSimulator
import net.torvald.terrarum.worlddrawer.LightmapRenderer
import net.torvald.terrarum.worlddrawer.LightmapRenderer.constructRGBFromInt
import net.torvald.terrarum.worlddrawer.BlocksDrawer
import net.torvald.terrarum.worlddrawer.FeaturesDrawer
import net.torvald.terrarum.worlddrawer.FeaturesDrawer.TILE_SIZE
import net.torvald.terrarum.worlddrawer.WorldCamera
import net.torvald.terrarum.worldgenerator.WorldGenerator
import net.torvald.terrarum.worldgenerator.RoguelikeRandomiser
import net.torvald.terrarum.blockproperties.BlockPropUtil
import net.torvald.terrarum.blockstats.BlockStats
import net.torvald.terrarum.ui.*
import net.torvald.terrarum.weather.WeatherMixer
import org.newdawn.slick.*
import org.newdawn.slick.state.BasicGameState
import org.newdawn.slick.state.StateBasedGame
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.swing.JOptionPane

/**
 * Created by minjaesong on 15-12-30.
 */
class StateInGame : BasicGameState() {
    private val ACTOR_UPDATE_RANGE = 4096

    lateinit var world: GameWorld

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
    val player: ActorHumanoid? // currently POSSESSED actor :)
        get() = playableActorDelegate?.actor

    var screenZoom = 1.0f
    val ZOOM_MAX = 4.0f
    val ZOOM_MIN = 0.5f

    val worldDrawFrameBuffer = Image(Terrarum.WIDTH.div(ZOOM_MIN).ceilInt(), Terrarum.HEIGHT.div(ZOOM_MIN).ceilInt())
    val worldG = worldDrawFrameBuffer.graphics
    val backDrawFrameBuffer = Image(Terrarum.WIDTH, Terrarum.HEIGHT)
    val backG = backDrawFrameBuffer.graphics

    //private lateinit var shader12BitCol: Shader // grab LibGDX if you want some shader
    //private lateinit var shaderBlur: Shader

    private val useShader: Boolean = false
    private val shaderProgram = 0

    val KEY_LIGHTMAP_RENDER = Key.F7
    val KEY_LIGHTMAP_SMOOTH = Key.F8



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

    // UI aliases
    lateinit var uiAliases: ArrayList<UIHandler>
        private set
    lateinit var uiAlasesPausing: ArrayList<UIHandler>
        private set

    var paused: Boolean = false
        get() = uiAlasesPausing.map { if (it.isOpened) 1 else 0 }.sum() > 0
    /**
     * Set to false if UI is opened; set to true  if UI is closed.
     */
    var canPlayerControl: Boolean = false
        get() = !paused // FIXME temporary behab (block movement if the game is paused or paused by UIs)

    @Throws(SlickException::class)
    override fun init(gameContainer: GameContainer, stateBasedGame: StateBasedGame) {
        // state init code. Executed before the game goes into any "state" in states in StateBasedGame.java

    }

    override fun enter(gc: GameContainer, sbg: StateBasedGame) {
        // load things when the game entered this "state"
        // load necessary shaders
        //shader12BitCol = Shader.makeShader("./assets/4096.vert", "./assets/4096.frag")
        //shaderBlur = Shader.makeShader("./assets/blur.vert", "./assets/blur.frag")

        // init map as chosen size
        world = GameWorld(8192, 2048)

        // generate terrain for the map
        WorldGenerator.attachMap(world)
        //WorldGenerator.SEED = 0x51621D2
        WorldGenerator.SEED = HQRNG().nextLong()
        WorldGenerator.generateMap()


        RoguelikeRandomiser.seed = HQRNG().nextLong()


        // add new player and put it to actorContainer
        playableActorDelegate = PlayableActorDelegate(PlayerBuilderSigrid())
        //playableActorDelegate = PlayableActorDelegate(PlayerBuilderTestSubject1())
        addNewActor(player!!)


        // test actor
        //addNewActor(PlayerBuilderCynthia())



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

        // set smooth lighting as in config
        KeyToggler.forceSet(KEY_LIGHTMAP_SMOOTH, Terrarum.getConfigBoolean("smoothlighting"))



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
        uiPieMenu.setPosition(HALFW, HALFH)

        // vital metre
        // fill in getter functions by
        //      (uiAliases[UI_QUICK_BAR]!!.UI as UIVitalMetre).vitalGetterMax = { some_function }
        uiVitalPrimary = UIHandler(UIVitalMetre(player, { 80f }, { 100f }, Color.red, 2), customPositioning = true)
        uiVitalPrimary.setAsAlwaysVisible()
        uiVitalSecondary = UIHandler(UIVitalMetre(player, { 73f }, { 100f }, Color(0x00dfff), 1), customPositioning = true)
        uiVitalSecondary.setAsAlwaysVisible()
        uiVitalItem = UIHandler(UIVitalMetre(player, { null }, { null }, Color(0xffcc00), 0), customPositioning = true)
        uiVitalItem.setAsAlwaysVisible()


        // batch-process uiAliases
        uiAliases = arrayListOf(
                // drawn first
                uiVitalPrimary,
                uiVitalSecondary,
                uiVitalItem,
                uiPieMenu,
                uiQuickBar
                // drawn last
        )
        uiAlasesPausing = arrayListOf(
                uiInventoryPlayer,
                //uiInventoryContainer,
                consoleHandler
        )
        uiAlasesPausing.forEach { addUI(it) } // put them all to the UIContainer
        uiAliases.forEach { addUI(it) } // put them all to the UIContainer






        // audio test
        //AudioResourceLibrary.ambientsWoods[0].play()
    }

    var particlesActive = 0
        private set

    override fun update(gc: GameContainer, sbg: StateBasedGame, delta: Int) {
        particlesActive = 0
        Terrarum.delta = delta
        setAppTitle()


        KeyToggler.update(gc.input)
        GameController.processInput(gc, delta, gc.input)


        if (!paused) {

            ///////////////////////////
            // world-related updates //
            ///////////////////////////
            BlockPropUtil.dynamicLumFuncTickClock()
            world.updateWorldTime(delta)
            //WorldSimulator(player, delta)
            WeatherMixer.update(gc, delta)
            BlockStats.update()
            if (!(CommandDict["setgl"] as SetGlobalLightOverride).lightOverride)
                world.globalLight = constructRGBFromInt(
                        WeatherMixer.globalLightNow.redByte,
                        WeatherMixer.globalLightNow.greenByte,
                        WeatherMixer.globalLightNow.blueByte
                )


            ///////////////////////////
            // input-related updates //
            ///////////////////////////
            uiContainer.forEach { it.processInput(gc, delta, gc.input) }


            ////////////////////////////
            // camera-related updates //
            ////////////////////////////
            FeaturesDrawer.update(gc, delta)
            WorldCamera.update()



            ///////////////////////////
            // actor-related updates //
            ///////////////////////////
            repossessActor()

            // determine whether the inactive actor should be activated
            wakeDormantActors()
            // determine whether the actor should keep being activated or be dormant
            KillOrKnockdownActors()
            updateActors(gc, delta)
            particlesContainer.forEach { if (!it.flagDespawn) particlesActive++; it.update(gc, delta) }
            // TODO thread pool(?)
            CollisionSolver.process()
        }


        ////////////////////////
        // ui-related updates //
        ////////////////////////
        uiContainer.forEach { it.update(gc, delta) }
        debugWindow.update(gc, delta)
        notifier.update(gc, delta)

        // update debuggers using javax.swing //
        if (Authenticator.b()) {
            AVTracker.update()
            ActorsList.update()
        }



        /////////////////////////
        // app-related updates //
        /////////////////////////

        // determine if lightmap blending should be done
        Terrarum.setConfig("smoothlighting", KeyToggler.isOn(KEY_LIGHTMAP_SMOOTH))
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
        WorldSimulator(player, delta)
    }

    private fun changePossession(refid: Int) {
        // TODO prevent possessing other player on multiplayer

        if (!theGameHasActor(refid)) {
            throw IllegalArgumentException("No such actor in the game: $refid (elemsActive: ${actorContainer.size}, elemsInactive: ${actorContainerInactive.size})")
        }

        // take care of old delegate
        playableActorDelegate!!.actor.collisionType = HumanoidNPC.DEFAULT_COLLISION_TYPE
        // accept new delegate
        playableActorDelegate = PlayableActorDelegate(getActorByID(refid) as ActorHumanoid)
        playableActorDelegate!!.actor.collisionType = ActorWithPhysics.COLLISION_KINEMATIC
        WorldSimulator(player, delta)
    }

    private fun setAppTitle() {
        Terrarum.appgc.setTitle(
                Terrarum.NAME +
                " — F: ${Terrarum.appgc.fps} (${Terrarum.TARGET_INTERNAL_FPS})" +
                " — M: ${Terrarum.memInUse}M / ${Terrarum.memTotal}M / ${Terrarum.memXmx}M"
        )
    }

    override fun render(gc: GameContainer, sbg: StateBasedGame, gwin: Graphics) {
        Terrarum.GLOBAL_RENDER_TIMER += 1

        // clean the shit beforehand
        worldG.clear()
        backG.clear()

        blendNormal()


        drawSkybox(backG) // drawing to gwin so that any lights from lamp wont "leak" to the skybox
                         // e.g. Bright blue light on sunset


        // make camara work
        worldG.translate(-WorldCamera.x.toFloat(), -WorldCamera.y.toFloat())


        blendNormal()

        /////////////////////////////
        // draw map related stuffs //
        /////////////////////////////
        BlocksDrawer.renderWall(worldG)
        actorsRenderBehind.forEach { it.drawBody(worldG) }
        actorsRenderBehind.forEach { it.drawGlow(worldG) }
        particlesContainer.forEach { it.drawBody(worldG) }
        particlesContainer.forEach { it.drawGlow(worldG) }
        BlocksDrawer.renderTerrain(worldG)

        /////////////////
        // draw actors //
        /////////////////
        actorsRenderMiddle.forEach { it.drawBody(worldG) }
        actorsRenderMidTop.forEach { it.drawBody(worldG) }
        player?.drawBody(worldG)
        actorsRenderFront.forEach { it.drawBody(worldG) }
        // --> Change of blend mode <-- introduced by childs of ActorWithBody //


        /////////////////////////////
        // draw map related stuffs //
        /////////////////////////////
        LightmapRenderer.renderLightMap()

        BlocksDrawer.renderFront(worldG, false)
        // --> blendNormal() <-- by BlocksDrawer.renderFront
        FeaturesDrawer.render(gc, worldG)


        FeaturesDrawer.drawEnvOverlay(worldG)

        if (!KeyToggler.isOn(KEY_LIGHTMAP_RENDER)) blendMul()
        else blendNormal()
        LightmapRenderer.draw(worldG)


        //////////////////////
        // draw actor glows //
        //////////////////////
        actorsRenderMiddle.forEach { it.drawGlow(worldG) }
        actorsRenderMidTop.forEach { it.drawGlow(worldG) }
        player?.drawGlow(worldG)
        actorsRenderFront.forEach { it.drawGlow(worldG) }
        // --> blendLightenOnly() <-- introduced by childs of ActorWithBody //


        ////////////////////////
        // debug informations //
        ////////////////////////
        blendNormal()
        // draw reference ID if debugWindow is open
        if (debugWindow.isVisible) {
            actorContainer.forEachIndexed { i, actor ->
                if (actor is ActorWithBody) {
                    worldG.color = Color.white
                    worldG.font = Terrarum.fontSmallNumbers
                    worldG.drawString(
                            actor.referenceID.toString(),
                            actor.hitbox.posX.toFloat(),
                            actor.hitbox.pointedY.toFloat() + 4
                    )
                }
            }
        }
        // debug physics
        if (KeyToggler.isOn(Key.F11)) {
            actorContainer.forEachIndexed { i, actor ->
                if (actor is ActorWithPhysics) {
                    worldG.color = Color(1f, 0f, 1f, 1f)
                    worldG.font = Terrarum.fontSmallNumbers
                    worldG.lineWidth = 1f
                    worldG.drawRect(
                            actor.hitbox.posX.toFloat(),
                            actor.hitbox.posY.toFloat(),
                            actor.hitbox.width.toFloat(),
                            actor.hitbox.height.toFloat()
                    )

                    // velocity
                    worldG.color = GameFontBase.codeToCol["g"]
                    worldG.drawString(
                            "${0x7F.toChar()}X ${actor.moveDelta.x}",
                            actor.hitbox.posX.toFloat(),
                            actor.hitbox.pointedY.toFloat() + 4 + 8
                    )
                    worldG.drawString(
                            "${0x7F.toChar()}Y ${actor.moveDelta.y}",
                            actor.hitbox.posX.toFloat(),
                            actor.hitbox.pointedY.toFloat() + 4 + 8 * 2
                    )
                }
            }
        }
        // fluidmap debug
        if (KeyToggler.isOn(Key.F4))
            WorldSimulator.drawFluidMapDebug(worldG)




        /////////////////
        // GUI Predraw //
        /////////////////
        worldG.flush()
        backG.drawImage(worldDrawFrameBuffer.getScaledCopy(screenZoom), 0f, 0f)
        backG.flush()


        /////////////////////
        // draw UIs  ONLY! //
        /////////////////////
        uiContainer.forEach { if (it != consoleHandler) it.render(gc, sbg, backG) }
        debugWindow.render(gc, sbg, backG)
        // make sure console draws on top of other UIs
        consoleHandler.render(gc, sbg, backG)
        notifier.render(gc, sbg, backG)


        //////////////////
        // GUI Postdraw //
        //////////////////
        backG.flush()
        gwin.drawImage(backDrawFrameBuffer, 0f, 0f)


        // centre marker
        /*gwin.color = Color(0x00FFFF)
        gwin.lineWidth = 1f
        gwin.drawLine(Terrarum.WIDTH / 2f, 0f, Terrarum.WIDTH / 2f, Terrarum.HEIGHT.toFloat())
        gwin.drawLine(0f, Terrarum.HEIGHT / 2f, Terrarum.WIDTH.toFloat(), Terrarum.HEIGHT / 2f)*/
    }

    override fun keyPressed(key: Int, c: Char) {
        if (key == Key.GRAVE) {
            consoleHandler.toggleOpening()
        }
        else if (key == Key.F3) {
            debugWindow.toggleOpening()
        }

        GameController.keyPressed(key, c)
    }
    override fun keyReleased(key: Int, c: Char) { GameController.keyReleased(key, c) }
    override fun mouseMoved(oldx: Int, oldy: Int, newx: Int, newy: Int) { GameController.mouseMoved(oldx, oldy, newx, newy) }
    override fun mouseDragged(oldx: Int, oldy: Int, newx: Int, newy: Int) { GameController.mouseDragged(oldx, oldy, newx, newy) }
    override fun mousePressed(button: Int, x: Int, y: Int) { GameController.mousePressed(button, x, y) }
    override fun mouseReleased(button: Int, x: Int, y: Int) { GameController.mouseReleased(button, x, y) }
    override fun mouseWheelMoved(change: Int) { GameController.mouseWheelMoved(change) }
    override fun controllerButtonPressed(controller: Int, button: Int) { GameController.controllerButtonPressed(controller, button) }
    override fun controllerButtonReleased(controller: Int, button: Int) { GameController.controllerButtonReleased(controller, button) }

    override fun getID(): Int = Terrarum.STATE_ID_GAME

    private fun drawSkybox(g: Graphics) = WeatherMixer.render(g)

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
    fun updateActors(gc: GameContainer, delta: Int) {
        if (false) { // don't multithread this for now, it's SLOWER //if (Terrarum.MULTITHREAD && actorContainer.size > Terrarum.THREADS) {
            val actors = actorContainer.size.toFloat()
            // set up indices
            for (i in 0..Terrarum.THREADS - 1) {
                ThreadParallel.map(
                        i,
                        ThreadActorUpdate(
                                actors.div(Terrarum.THREADS).times(i).roundInt(),
                                actors.div(Terrarum.THREADS).times(i.plus(1)).roundInt() - 1,
                                gc, delta
                        ),
                        "ActorUpdate"
                )
            }

            ThreadParallel.startAll()
        }
        else {
            actorContainer.forEach {
                it.update(gc, delta)

                if (it is Pocketed) {
                    it.inventory.forEach { inventoryEntry ->
                        inventoryEntry.item.effectWhileInPocket(gc, delta)
                        if (it.equipped(inventoryEntry.item)) {
                            inventoryEntry.item.effectWhenEquipped(gc, delta)
                        }
                    }
                }
            }
            AmmoMeterProxy(player!!, uiVitalItem.UI as UIVitalMetre)
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
                    (a.hitbox.centeredX - p.hitbox.centeredX + world.width * TILE_SIZE).sqr() +
                    (a.hitbox.centeredY - p.hitbox.centeredY).sqr(),
                    (a.hitbox.centeredX - p.hitbox.centeredX - world.width * TILE_SIZE).sqr() +
                    (a.hitbox.centeredY - p.hitbox.centeredY).sqr()
            )
    private fun distToCameraSqr(a: ActorWithBody) =
            min(
                    (a.hitbox.posX - WorldCamera.x).sqr() +
                    (a.hitbox.posY - WorldCamera.y).sqr(),
                    (a.hitbox.posX - WorldCamera.x + world.width * TILE_SIZE).sqr() +
                    (a.hitbox.posY - WorldCamera.y).sqr(),
                    (a.hitbox.posX - WorldCamera.x - world.width * TILE_SIZE).sqr() +
                    (a.hitbox.posY - WorldCamera.y).sqr()
            )

    /** whether the actor is within screen */
    private fun ActorWithBody.inScreen() =
            distToCameraSqr(this) <=
            (Terrarum.WIDTH.plus(this.hitbox.width.div(2)).times(1 / Terrarum.ingame!!.screenZoom).sqr() +
             Terrarum.HEIGHT.plus(this.hitbox.height.div(2)).times(1 / Terrarum.ingame!!.screenZoom).sqr())


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
            throw IllegalArgumentException("Exact copy of the UI already exists: The instance of ${ui.UI.javaClass.simpleName}")
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
                JOptionPane.showMessageDialog(null, "Actor with ID $ID does not exist.", null, JOptionPane.ERROR_MESSAGE)
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
}
