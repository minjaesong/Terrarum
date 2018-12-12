
package net.torvald.terrarum.gameworld

import com.badlogic.gdx.graphics.Color
import net.torvald.terrarum.blockproperties.Block
import net.torvald.terrarum.realestate.LandUtil
import net.torvald.terrarum.blockproperties.BlockCodex
import net.torvald.terrarum.blockproperties.Fluid
import net.torvald.terrarum.modulebasegame.gameworld.WorldSimulator
import net.torvald.terrarum.serialise.ReadLayerDataLzma
import org.dyn4j.geometry.Vector2
import kotlin.math.absoluteValue

typealias BlockAddress = Long

open class GameWorld {

    var worldName: String = "New World"
    var worldIndex: Int
    val width: Int
    val height: Int

    val creationTime: Long
    var lastPlayTime: Long
        internal set // there's a case of save-and-continue-playing
    var totalPlayTime: Int
        internal set

    /** Used to calculate play time */
    val loadTime: Long = System.currentTimeMillis() / 1000L

    //layers
    val layerWall: MapLayer
    val layerTerrain: MapLayer
    val layerWire: MapLayer

    val layerWallLowBits: PairedMapLayer
    val layerTerrainLowBits: PairedMapLayer

    //val layerThermal: MapLayerHalfFloat // in Kelvins
    //val layerFluidPressure: MapLayerHalfFloat // (milibar - 1000)

    /** Tilewise spawn point */
    var spawnX: Int
    /** Tilewise spawn point */
    var spawnY: Int

    val wallDamages: HashMap<BlockAddress, Float>
    val terrainDamages: HashMap<BlockAddress, Float>
    val fluidTypes: HashMap<BlockAddress, FluidType>
    val fluidFills: HashMap<BlockAddress, Float>

    //public World physWorld = new World( new Vec2(0, -Terrarum.game.gravitationalAccel) );
    //physics
    /** Meter per second squared. Currently only the downward gravity is supported. No reverse gravity :p */
    var gravitation: Vector2 = Vector2(0.0, 9.80665)
    /** 0.0..1.0+ */
    var globalLight = Color(0f,0f,0f,0f)
    var averageTemperature = 288f // 15 deg celsius; simulates global warming


    var generatorSeed: Long = 0
        internal set


    constructor(worldIndex: Int, width: Int, height: Int, creationTIME_T: Long, lastPlayTIME_T: Long, totalPlayTime: Int) {
        if (width <= 0 || height <= 0) throw IllegalArgumentException("Non-positive width/height: ($width, $height)")

        this.worldIndex = worldIndex
        this.width = width
        this.height = height

        this.spawnX = width / 2
        this.spawnY = 200

        layerTerrain = MapLayer(width, height)
        layerWall = MapLayer(width, height)
        layerWire = MapLayer(width, height)
        layerTerrainLowBits = PairedMapLayer(width, height)
        layerWallLowBits = PairedMapLayer(width, height)

        wallDamages = HashMap<BlockAddress, Float>()
        terrainDamages = HashMap<BlockAddress, Float>()
        fluidTypes = HashMap<BlockAddress, FluidType>()
        fluidFills = HashMap<BlockAddress, Float>()

        // temperature layer: 2x2 is one cell
        //layerThermal = MapLayerHalfFloat(width, height, averageTemperature)

        // fluid pressure layer: 4 * 8 is one cell
        //layerFluidPressure = MapLayerHalfFloat(width, height, 13f) // 1013 mBar


        creationTime = creationTIME_T
        lastPlayTime = lastPlayTIME_T
        this.totalPlayTime = totalPlayTime
    }

    internal constructor(worldIndex: Int, layerData: ReadLayerDataLzma.LayerData, creationTIME_T: Long, lastPlayTIME_T: Long, totalPlayTime: Int) {
        this.worldIndex = worldIndex

        layerTerrain = layerData.layerTerrain
        layerWall = layerData.layerWall
        layerWire = layerData.layerWire
        layerTerrainLowBits = layerData.layerTerrainLowBits
        layerWallLowBits = layerData.layerWallLowBits

        wallDamages = layerData.wallDamages
        terrainDamages = layerData.terrainDamages
        fluidTypes = layerData.fluidTypes
        fluidFills = layerData.fluidFills

        spawnX = layerData.spawnX
        spawnY = layerData.spawnY

        width = layerTerrain.width
        height = layerTerrain.height


        creationTime = creationTIME_T
        lastPlayTime = lastPlayTIME_T
        this.totalPlayTime = totalPlayTime
    }


    /**
     * Get 2d array data of terrain

     * @return byte[][] terrain layer
     */
    val terrainArray: ByteArray
        get() = layerTerrain.data

    /**
     * Get 2d array data of wall

     * @return byte[][] wall layer
     */
    val wallArray: ByteArray
        get() = layerWall.data

    /**
     * Get 2d array data of wire

     * @return byte[][] wire layer
     */
    val wireArray: ByteArray
        get() = layerWire.data

    /**
     * Get paired array data of damage codes.
     * Format: 0baaaabbbb, aaaa for x = 0, 2, 4, ..., bbbb for x = 1, 3, 5, ...
     * @return byte[][] damage code pair
     */
    val damageDataArray: ByteArray
        get() = layerTerrainLowBits.data

    fun getTileFromWall(x: Int, y: Int): Int? {
        val wall: Int? = layerWall.getTile(x fmod width, y.coerceWorld().coerceWorld())
        val wallDamage: Int? = getWallLowBits(x fmod width, y.coerceWorld())
        return if (wall == null || wallDamage == null)
            null
        else
            wall * PairedMapLayer.RANGE + wallDamage
    }

    fun getTileFromTerrain(x: Int, y: Int): Int? {
        val terrain: Int? = layerTerrain.getTile(x fmod width, y.coerceWorld())
        val terrainDamage: Int? = getTerrainLowBits(x fmod width, y.coerceWorld())
        return if (terrain == null || terrainDamage == null)
            null
        else
            terrain * PairedMapLayer.RANGE + terrainDamage
    }

    fun getTileFromWire(x: Int, y: Int): Int? {
        return layerWire.getTile(x fmod width, y.coerceWorld())
    }

    fun getWallLowBits(x: Int, y: Int): Int? {
        return layerWallLowBits.getData(x fmod width, y.coerceWorld())
    }

    fun getTerrainLowBits(x: Int, y: Int): Int? {
        return layerTerrainLowBits.getData(x fmod width, y.coerceWorld())
    }

    /**
     * Set the tile of wall as specified, with damage value of zero.
     * @param x
     * *
     * @param y
     * *
     * @param combinedTilenum (tilenum * 16) + damage
     */
    fun setTileWall(x: Int, y: Int, combinedTilenum: Int) {
        setTileWall(x fmod width, y.coerceWorld(), (combinedTilenum / PairedMapLayer.RANGE).toByte(), combinedTilenum % PairedMapLayer.RANGE)
    }

    /**
     * Set the tile of wall as specified, with damage value of zero.
     * @param x
     * *
     * @param y
     * *
     * @param combinedTilenum (tilenum * 16) + damage
     */
    fun setTileTerrain(x: Int, y: Int, combinedTilenum: Int) {
        setTileTerrain(x fmod width, y.coerceWorld(), (combinedTilenum / PairedMapLayer.RANGE).toByte(), combinedTilenum % PairedMapLayer.RANGE)
    }

    fun setTileWall(x: Int, y: Int, tile: Byte, damage: Int) {
        layerWall.setTile(x fmod width, y.coerceWorld(), tile)
        layerWallLowBits.setData(x fmod width, y.coerceWorld(), damage)
        wallDamages.remove(LandUtil.getBlockAddr(this, x, y))
    }

    fun setTileTerrain(x: Int, y: Int, tile: Byte, damage: Int) {
        layerTerrain.setTile(x fmod width, y.coerceWorld(), tile)
        layerTerrainLowBits.setData(x fmod width, y.coerceWorld(), damage)
        terrainDamages.remove(LandUtil.getBlockAddr(this, x, y))
    }

    fun setTileWire(x: Int, y: Int, tile: Byte) {
        layerWire.setTile(x fmod width, y.coerceWorld(), tile)
    }

    fun getTileFrom(mode: Int, x: Int, y: Int): Int? {
        if (mode == TERRAIN) {
            return getTileFromTerrain(x, y)
        }
        else if (mode == WALL) {
            return getTileFromWall(x, y)
        }
        else if (mode == WIRE) {
            return getTileFromWire(x, y)
        }
        else
            throw IllegalArgumentException("illegal mode input: " + mode.toString())
    }

    fun terrainIterator(): Iterator<Int> {
        return object : Iterator<Int> {

            private var iteratorCount = 0

            override fun hasNext(): Boolean {
                return iteratorCount < width * height
            }

            override fun next(): Int {
                val y = iteratorCount / width
                val x = iteratorCount % width
                // advance counter
                iteratorCount += 1

                return getTileFromTerrain(x, y)!!
            }

        }
    }

    fun wallIterator(): Iterator<Int> {
        return object : Iterator<Int> {

            private var iteratorCount = 0

            override fun hasNext(): Boolean =
                    iteratorCount < width * height

            override fun next(): Int {
                val y = iteratorCount / width
                val x = iteratorCount % width
                // advance counter
                iteratorCount += 1

                return getTileFromWall(x, y)!!
            }

        }
    }

    /**
     * @return true if block is broken
     */
    fun inflictTerrainDamage(x: Int, y: Int, damage: Double): Boolean {
        val damage = damage.toFloat()
        val addr = LandUtil.getBlockAddr(this, x, y)

        //println("[GameWorld] ($x, $y) Damage: $damage")

        if (terrainDamages[addr] == null) { // add new
            terrainDamages[addr] = damage
        }
        else if (terrainDamages[addr]!! + damage <= 0) { // tile is (somehow) fully healed
            terrainDamages.remove(addr)
        }
        else { // normal situation
            terrainDamages[addr] = terrainDamages[addr]!! + damage
        }

        //println("[GameWorld] accumulated damage: ${terrainDamages[addr]}")

        // remove tile from the world
        if (terrainDamages[addr] ?: 0f >= BlockCodex[getTileFromTerrain(x, y)].strength) {
            setTileTerrain(x, y, 0)
            terrainDamages.remove(addr)
            return true
        }

        return false
    }
    fun getTerrainDamage(x: Int, y: Int): Float =
            terrainDamages[LandUtil.getBlockAddr(this, x, y)] ?: 0f

    /**
     * @return true if block is broken
     */
    fun inflictWallDamage(x: Int, y: Int, damage: Double): Boolean {
        val damage = damage.toFloat()
        val addr = LandUtil.getBlockAddr(this, x, y)

        if (wallDamages[addr] == null) { // add new
            wallDamages[addr] = damage
        }
        else if (wallDamages[addr]!! + damage <= 0) { // tile is (somehow) fully healed
            wallDamages.remove(addr)
        }
        else { // normal situation
            wallDamages[addr] = wallDamages[addr]!! + damage
        }

        // remove tile from the world
        if (wallDamages[addr]!! >= BlockCodex[getTileFromWall(x, y)].strength) {
            setTileWall(x, y, 0)
            wallDamages.remove(addr)
            return true
        }

        return false
    }
    fun getWallDamage(x: Int, y: Int): Float =
            wallDamages[LandUtil.getBlockAddr(this, x, y)] ?: 0f

    fun setFluid(x: Int, y: Int, fluidType: FluidType, fill: Float) {
        val addr = LandUtil.getBlockAddr(this, x, y)
        // fluid completely drained
        if (fill <= WorldSimulator.FLUID_MIN_MASS) {
            /**********/ fluidTypes.remove(addr)
            val oldMap = fluidFills.remove(addr)

            // oldMap not being null means there actually was a fluid there, so we can put AIR onto it
            // otherwise, it means it was some solid and therefore we DON'T want to put AIR onto it
            if (oldMap != null) {
                setTileTerrain(x, y, 0)
            }
        }
        // update the fluid amount
        else {
            fluidTypes[addr] = fluidType
            fluidFills[addr] = fill
            setTileTerrain(x, y, Block.FLUID_MARKER)
        }
    }

    fun getFluid(x: Int, y: Int): FluidInfo {
        val addr = LandUtil.getBlockAddr(this, x, y)
        val fill = fluidFills[addr]
        val type = fluidTypes[addr]

        return if (type == null) FluidInfo(Fluid.NULL, 0f) else FluidInfo(type, fill!!)
    }

    data class FluidInfo(val type: FluidType, val amount: Float)


    fun getTemperature(worldTileX: Int, worldTileY: Int): Float? {
        return null
    }

    fun getAirPressure(worldTileX: Int, worldTileY: Int): Float? {
        return null
    }


    private fun Int.coerceWorld() = this.coerceIn(0, height - 1)
    
    companion object {
        @Transient val WALL = 0
        @Transient val TERRAIN = 1
        @Transient val WIRE = 2

        @Transient val TILES_SUPPORTED = MapLayer.RANGE * PairedMapLayer.RANGE
        @Transient val SIZEOF: Byte = MapLayer.SIZEOF
        @Transient val LAYERS: Byte = 4 // terrain, wall (layerTerrainLowBits + layerWallLowBits), wire

        fun makeNullWorld() = GameWorld(-1, 1, 1, 0, 0, 0)
    }
}

infix fun Int.fmod(other: Int) = Math.floorMod(this, other)
infix fun Float.fmod(other: Float) = if (this >= 0f) this % other else (this % other) + other

inline class FluidType(val value: Int) {
    infix fun sameAs(other: FluidType) = this.value.absoluteValue == other.value.absoluteValue
}