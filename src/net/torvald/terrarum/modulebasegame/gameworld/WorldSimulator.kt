package net.torvald.terrarum.modulebasegame.gameworld

import com.badlogic.gdx.graphics.Color
import net.torvald.terrarum.AppLoader.printdbg
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.blockproperties.Block
import net.torvald.terrarum.roundInt
import net.torvald.terrarum.worlddrawer.FeaturesDrawer
import net.torvald.terrarum.blockproperties.BlockCodex
import net.torvald.terrarum.blockproperties.Fluid
import net.torvald.terrarum.gameworld.FluidType
import net.torvald.terrarum.modulebasegame.gameactors.ActorHumanoid

/**
 * Created by minjaesong on 2016-08-03.
 */
object WorldSimulator {

    // FLUID-RELATED STUFFS //

    /**
     * In tiles;
     * square width/height = field * 2
     */
    const val FLUID_UPDATING_SQUARE_RADIUS = 80 // larger value will have dramatic impact on performance
    const private val DOUBLE_RADIUS = FLUID_UPDATING_SQUARE_RADIUS * 2

    // maps are separated as old-new for obvious reason, also it'll allow concurrent modification
    private val fluidMap = Array(DOUBLE_RADIUS, { FloatArray(DOUBLE_RADIUS) })
    private val fluidTypeMap = Array(DOUBLE_RADIUS, { Array<FluidType>(DOUBLE_RADIUS) { Fluid.NULL } })
    private val fluidNewMap = Array(DOUBLE_RADIUS, { FloatArray(DOUBLE_RADIUS) })
    private val fluidNewTypeMap = Array(DOUBLE_RADIUS, { Array<FluidType>(DOUBLE_RADIUS) { Fluid.NULL } })

    const val FLUID_MAX_MASS = 1f // The normal, un-pressurized mass of a full water cell
    const val FLUID_MAX_COMP = 0.02f // How much excess water a cell can store, compared to the cell above it. A tile of fluid can contain more than MaxMass water.
    const val FLUID_MIN_MASS = 0.0001f //Ignore cells that are almost dry
    const val minFlow = 0.01f
    const val maxSpeed = 1f // max units of water moved out of one block to another, per timestamp

    // END OF FLUID-RELATED STUFFS

    var updateXFrom = 0
    var updateXTo = 0
    var updateYFrom = 0
    var updateYTo = 0

    val colourNone = Color(0x808080FF.toInt())
    val colourWater = Color(0x66BBFFFF.toInt())

    private val world = (Terrarum.ingame!!.world)

    operator fun invoke(p: ActorHumanoid?, delta: Float) {
        //printdbg(this, "============================")

        if (p != null) {
            updateXFrom = p.hitbox.centeredX.div(FeaturesDrawer.TILE_SIZE).minus(FLUID_UPDATING_SQUARE_RADIUS).roundInt()
            updateYFrom = p.hitbox.centeredY.div(FeaturesDrawer.TILE_SIZE).minus(FLUID_UPDATING_SQUARE_RADIUS).roundInt()
            updateXTo = updateXFrom + DOUBLE_RADIUS
            updateYTo = updateYFrom + DOUBLE_RADIUS
        }

        moveFluids(delta)
        displaceFallables(delta)

        //printdbg(this, "============================")
    }

    /**
     * displace fluids. Note that the code assumes the gravity pulls things downward ONLY,
     * which means you'll need to modify the code A LOT if you're going to implement zero- or
     * reverse-gravity.
     *
     * Procedure: CP world fluidmap -> sim on fluidmap -> CP fluidmap world
     * TODO multithread
     */
    fun moveFluids(delta: Float) {
        makeFluidMapFromWorld()

        //simCompression()
        for (y in 1 until fluidMap.size - 1) {
            for (x in 1 until fluidMap[0].size - 1) {
                val worldX = x + updateXFrom
                val worldY = y + updateYFrom

                /*if (worldX == 60 && worldY == 256) {
                    printdbg(this, "tile: ${world.getTileFromTerrain(worldX, worldY)}, isSolid = ${isSolid(worldX, worldY)}")
                }*/

                if (isSolid(worldX, worldY)) continue
                val remainingMass = fluidMap[y][x]

                /*if (worldX == 60 && worldY == 256) {
                    printdbg(this, "remainimgMass: $remainingMass at ($worldX, $worldY)")
                }*/

                if (!isSolid(worldX, worldY + 1)) {
                    fluidNewMap[y][x] -= remainingMass
                    fluidNewMap[y + 1][x] += remainingMass
                }
            }
        }

        fluidmapToWorld()
    }

    fun isFlowable(type: FluidType, worldX: Int, worldY: Int): Boolean {
        val targetFluid = world.getFluid(worldX, worldY)

        // true if target's type is the same as mine, or it's NULL (air)
        return (targetFluid.type sameAs type || targetFluid.type sameAs Fluid.NULL)
    }

    fun isSolid(worldX: Int, worldY: Int): Boolean {
        val tile = world.getTileFromTerrain(worldX, worldY)
        if (tile != Block.WATER) {
            // check for block properties isSolid
            return BlockCodex[tile].isSolid
        }
        else {
            // check for fluid

            // no STATIC is implement yet, just return false
            return false
        }
    }

    /*
    Explanation of get_stable_state_b (well, kind-of) :

    if x <= 1, all water goes to the lower cell
        * a = 0
        * b = 1

    if x > 1 & x < 2*MaxMass + MaxCompress, the lower cell should have MaxMass + (upper_cell/MaxMass) * MaxCompress
        b = MaxMass + (a/MaxMass)*MaxCompress
        a = x - b

        ->

        b = MaxMass + ((x - b)/MaxMass)*MaxCompress ->
            b = MaxMass + (x*MaxCompress - b*MaxCompress)/MaxMass
            b*MaxMass = MaxMass^2 + (x*MaxCompress - b*MaxCompress)
            b*(MaxMass + MaxCompress) = MaxMass*MaxMass + x*MaxCompress

            * b = (MaxMass*MaxMass + x*MaxCompress)/(MaxMass + MaxCompress)
        * a = x - b;

    if x >= 2 * MaxMass + MaxCompress, the lower cell should have upper+MaxCompress

        b = a + MaxCompress
        a = x - b

        ->

        b = x - b + MaxCompress ->
        2b = x + MaxCompress ->

        * b = (x + MaxCompress)/2
        * a = x - b
      */
    private fun getStableStateB(totalMass: Float): Float {
        if (totalMass <= 1)
            return 1f
        else if (totalMass < 2f * FLUID_MAX_MASS + FLUID_MAX_COMP)
            return (FLUID_MAX_MASS * FLUID_MAX_MASS + totalMass * FLUID_MAX_COMP) / (FLUID_MAX_MASS + FLUID_MAX_COMP)
        else
            return (totalMass + FLUID_MAX_COMP) / 2f
    }

    private fun simCompression() {
        // before data: fluidMap/fluidTypeMap
        // after data: fluidNewMap/fluidNewTypeMap
        var flow = 0f
        var remainingMass = 0f

        for (y in 1 until fluidMap.size - 1) {
            for (x in 1 until fluidMap[0].size - 1) {
                val worldX = x + updateXFrom
                val worldY = y + updateYFrom

                // check solidity
                if (isSolid(worldX, worldY)) continue
                // check if the fluid is a same kind
                //if (!isFlowable(type, worldX, worldY))) continue


                // Custom push-only flow
                flow = 0f
                remainingMass = fluidMap[y][x]
                if (remainingMass <= 0) continue

                // The block below this one
                if (!isSolid(worldX, worldY + 1)) { // TODO use isFlowable
                    flow = getStableStateB(remainingMass + fluidMap[y + 1][x]) - fluidMap[y + 1][x]
                    if (flow > minFlow) {
                        flow *= 0.5f // leads to smoother flow
                    }
                    flow.coerceIn(0f, minOf(maxSpeed, remainingMass))

                    fluidNewMap[y][x] -= flow
                    fluidNewMap[y + 1][x] += flow
                    remainingMass -= flow
                }

                if (remainingMass <= 0) continue

                // Left
                if (!isSolid(worldX - 1, worldY)) { // TODO use isFlowable
                    // Equalise the amount fo water in this block and its neighbour
                    flow = (fluidMap[y][x] - fluidMap[y][x - 1]) / 4f
                    if (flow > minFlow) {
                        flow *= 0.5f
                    }
                    flow.coerceIn(0f, remainingMass)

                    fluidNewMap[y][x] -= flow
                    fluidNewMap[y][x - 1] += flow
                    remainingMass -= flow
                }

                if (remainingMass <= 0) continue

                // Right
                if (!isSolid(worldX + 1, worldY)) { // TODO use isFlowable
                    // Equalise the amount fo water in this block and its neighbour
                    flow = (fluidMap[y][x] - fluidMap[y][x + 1]) / 4f
                    if (flow > minFlow) {
                        flow *= 0.5f
                    }
                    flow.coerceIn(0f, remainingMass)

                    fluidNewMap[y][x] -= flow
                    fluidNewMap[y][x + 1] += flow
                    remainingMass -= flow
                }

                if (remainingMass <= 0) continue

                // Up; only compressed water flows upwards
                if (!isSolid(worldX, worldY - 1)) { // TODO use isFlowable
                    flow = remainingMass - getStableStateB(remainingMass + fluidMap[y - 1][x])
                    if (flow > minFlow) {
                        flow *= 0.5f
                    }
                    flow.coerceIn(0f, minOf(maxSpeed, remainingMass))

                    fluidNewMap[y][x] -= flow
                    fluidNewMap[y - 1][x] += flow
                    remainingMass -= flow
                }


            }
        }
    }

    /**
     * displace fallable tiles. It is scanned bottom-left first. To achieve the sens ofreal
     * falling, each tiles are displaced by ONLY ONE TILE below.
     */
    fun displaceFallables(delta: Float) {
        /*for (y in updateYFrom..updateYTo) {
            for (x in updateXFrom..updateXTo) {
                val tile = world.getTileFromTerrain(x, y) ?: Block.STONE
                val tileBelow = world.getTileFromTerrain(x, y + 1) ?: Block.STONE

                if (tile.isFallable()) {
                    // displace fluid. This statement must precede isSolid()
                    if (tileBelow.isFluid()) {
                        // remove tileThis to create air pocket
                        world.setTileTerrain(x, y, Block.AIR)

                        pour(x, y, drain(x, y, tileBelow.fluidLevel().toInt()))
                        // place our tile
                        world.setTileTerrain(x, y + 1, tile)
                    }
                    else if (!tileBelow.isSolid()) {
                        world.setTileTerrain(x, y, Block.AIR)
                        world.setTileTerrain(x, y + 1, tile)
                    }
                }
            }
        }*/
    }


    fun disperseHeat(delta: Float) {

    }

    private fun makeFluidMapFromWorld() {
        //printdbg(this, "Scan area: ($updateXFrom,$updateYFrom)..(${updateXFrom + fluidMap[0].size},${updateYFrom + fluidMap.size})")

        for (y in 0 until fluidMap.size) {
            for (x in 0 until fluidMap[0].size) {
                val fluidData = world.getFluid(x + updateXFrom, y + updateYFrom)
                fluidMap[y][x] = fluidData.amount
                fluidTypeMap[y][x] = fluidData.type
                fluidNewMap[y][x] = fluidData.amount
                fluidNewTypeMap[y][x] = fluidData.type

                if (x + updateXFrom == 60 && y + updateYFrom == 256) {
                    printdbg(this, "making array amount ${fluidData.amount} for (60,256)")
                }
            }
        }
    }

    private fun fluidmapToWorld() {
        for (y in 0 until fluidMap.size) {
            for (x in 0 until fluidMap[0].size) {
                world.setFluid(x + updateXFrom, y + updateYFrom, fluidNewTypeMap[y][x], fluidNewMap[y][x])
            }
        }
    }


    fun Int.isFallable() = BlockCodex[this].isFallable



}