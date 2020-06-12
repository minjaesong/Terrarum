package net.torvald.terrarum.modulebasegame.worldgenerator

import com.badlogic.gdx.graphics.Pixmap
import com.sudoplay.joise.Joise
import com.sudoplay.joise.module.ModuleAutoCorrect
import com.sudoplay.joise.module.ModuleBasisFunction
import com.sudoplay.joise.module.ModuleFractal
import com.sudoplay.joise.module.ModuleScaleDomain
import net.torvald.terrarum.AppLoader
import net.torvald.terrarum.blockproperties.Block
import net.torvald.terrarum.concurrent.ThreadExecutor
import net.torvald.terrarum.concurrent.sliceEvenly
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.gameworld.fmod
import java.util.concurrent.Future
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by minjaesong on 2019-09-02.
 */
class Biomegen(world: GameWorld, seed: Long, params: Any) : Gen(world, seed, params) {

    private val genSlices = maxOf(ThreadExecutor.threadCount, world.width / 8)

    private var genFutures: Array<Future<*>?> = arrayOfNulls(genSlices)
    override var generationStarted: Boolean = false
    override val generationDone: Boolean
        get() = generationStarted && genFutures.fold(true) { acc, f -> acc and (f?.isDone ?: true) }

    private val YHEIGHT_MAGIC = 2800.0 / 3.0
    private val YHEIGHT_DIVISOR = 2.0 / 7.0

    override fun run() {

        generationStarted = true

        ThreadExecutor.renew()
        (0 until world.width).sliceEvenly(genSlices).mapIndexed { i, xs ->
            genFutures[i] = ThreadExecutor.submit {
                val localJoise = getGenerator(seed, params as BiomegenParams)
                for (x in xs) {
                    for (y in 0 until world.height) {
                        val sampleTheta = (x.toDouble() / world.width) * TWO_PI
                        val sampleOffset = world.width / 8.0
                        val sampleX = sin(sampleTheta) * sampleOffset + sampleOffset // plus sampleOffset to make only
                        val sampleZ = cos(sampleTheta) * sampleOffset + sampleOffset // positive points are to be sampled
                        val sampleY = y - (world.height - YHEIGHT_MAGIC) * YHEIGHT_DIVISOR // Q&D offsetting to make ratio of sky:ground to be constant
                        // DEBUG NOTE: it is the OFFSET FROM THE IDEAL VALUE (observed land height - (HEIGHT * DIVISOR)) that must be constant
                        val noise = localJoise.map { it.get(sampleX, sampleY, sampleZ) }

                        draw(x, y, noise, world)
                    }
                }
            }
        }

        ThreadExecutor.join()

        AppLoader.printdbg(this, "Waking up Worldgen")
    }

    val nearbyArr = arrayOf(
            (-1 to -1), // tileTL
            (+1 to -1), // tileTR
            (-1 to +1), // tileBL
            (+1 to +1), // tileBR
            (0 to -1), // tileT
            (0 to +1), // tileB
            (-1 to 0), // tileL
            (+1 to 0) // tileR
    )

    private fun draw(x: Int, y: Int, noiseValue: List<Double>, world: GameWorld) {
        val control = noiseValue[0].times(4).minus(0.00001f).toInt().fmod(4)



        if (y > 0) {
            val tileThis = world.getTileFromTerrain(x, y)
            val wallThis = world.getTileFromWall(x, y)
            val nearbyTerr = nearbyArr.map { world.getTileFromTerrain(x + it.first, y + it.second) }
            val nearbyWall = nearbyArr.map { world.getTileFromWall(x + it.first, y + it.second) }

            when (control) {
                0 -> { // woodlands
                    if (world.getTileFromTerrain(x, y) == Block.DIRT && nearbyTerr.any { it == Block.AIR } && nearbyWall.any { it == Block.AIR }) {
                        world.setTileTerrain(x, y, Block.GRASS)
                    }
                }
                1 -> { // shrublands
                    if (world.getTileFromTerrain(x, y) == Block.DIRT && nearbyTerr.any { it == Block.AIR } && nearbyWall.any { it == Block.AIR }) {
                        world.setTileTerrain(x, y, Block.GRASS)
                    }
                }
                2 -> { // plains
                    if (world.getTileFromTerrain(x, y) == Block.DIRT && nearbyTerr.any { it == Block.AIR } && nearbyWall.any { it == Block.AIR }) {
                        world.setTileTerrain(x, y, Block.GRASS)
                    }
                }
                3 -> { // rockylands
                    if (world.getTileFromTerrain(x, y) == Block.DIRT) {
                        world.setTileTerrain(x, y, Block.STONE)
                        world.setTileWall(x, y, Block.STONE)
                    }
                }
            }
        }
    }

    private fun getGenerator(seed: Long, params: BiomegenParams): List<Joise> {
        //val biome = ModuleBasisFunction()
        //biome.setType(ModuleBasisFunction.BasisType.SIMPLEX)

        // simplex AND fractal for more noisy edges, mmmm..!
        val fractal = ModuleFractal()
        fractal.setType(ModuleFractal.FractalType.MULTI)
        fractal.setAllSourceBasisTypes(ModuleBasisFunction.BasisType.SIMPLEX)
        fractal.setNumOctaves(4)
        fractal.setFrequency(1.0)
        fractal.seed = seed shake 0x7E22A

        val autocorrect = ModuleAutoCorrect()
        autocorrect.setSource(fractal)
        autocorrect.setRange(0.0, 1.0)

        val scale = ModuleScaleDomain()
        scale.setSource(autocorrect)
        scale.setScaleX(1.0 / params.featureSize) // adjust this value to change features size
        scale.setScaleY(1.0 / params.featureSize)
        scale.setScaleZ(1.0 / params.featureSize)

        val last = scale

        return listOf(Joise(last))
    }

}

data class BiomegenParams(
        val featureSize: Double = 80.0
)