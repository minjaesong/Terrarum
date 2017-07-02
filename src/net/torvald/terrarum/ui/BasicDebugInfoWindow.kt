package net.torvald.terrarum.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.jme3.math.FastMath
import net.torvald.terrarum.worlddrawer.LightmapRenderer
import net.torvald.terrarum.worlddrawer.FeaturesDrawer
import net.torvald.terrarum.TerrarumGDX
import net.torvald.terrarum.blendNormal
import net.torvald.terrarum.blendScreen
import net.torvald.terrarum.fillRect
import net.torvald.terrarum.worlddrawer.WorldCamera
import net.torvald.terrarumsansbitmap.gdx.GameFontBase

/**
 * Created by minjaesong on 16-03-14.
 */
class BasicDebugInfoWindow : UICanvas {

    override var width: Int = Gdx.graphics.width
    override var height: Int = Gdx.graphics.height

    override var openCloseTime: Float = 0f

    override var handler: UIHandler? = null

    private var prevPlayerX = 0.0
    private var prevPlayerY = 0.0

    private var xdelta = 0.0
    private var ydelta = 0.0

    val ccW = GameFontBase.toColorCode(0xFFFF)
    val ccY = GameFontBase.toColorCode(0xFE8F)
    val ccO = GameFontBase.toColorCode(0xFB2F)
    val ccR = GameFontBase.toColorCode(0xF88F)
    val ccF = GameFontBase.toColorCode(0xFAEF)
    val ccM = GameFontBase.toColorCode(0xEAFF)
    val ccB = GameFontBase.toColorCode(0x88FF)
    val ccC = GameFontBase.toColorCode(0x8FFF)
    val ccG = GameFontBase.toColorCode(0x8F8F)
    val ccV = GameFontBase.toColorCode(0x080F)
    val ccX = GameFontBase.toColorCode(0x853F)
    val ccK = GameFontBase.toColorCode(0x888F)



    override fun update(delta: Float) {
        val player = TerrarumGDX.ingame!!.player!!
        val hitbox = player.hitbox

        xdelta = hitbox.canonicalX - prevPlayerX
        ydelta = hitbox.canonicalY - prevPlayerY

        prevPlayerX = hitbox.canonicalX
        prevPlayerY = hitbox.canonicalY
    }

    override fun render(batch: SpriteBatch) {
        fun Int.rawR() = this / LightmapRenderer.MUL_2
        fun Int.rawG() = this % LightmapRenderer.MUL_2 / LightmapRenderer.MUL
        fun Int.rawB() = this % LightmapRenderer.MUL

        val player = TerrarumGDX.ingame!!.player

        val mouseTileX = ((WorldCamera.x + TerrarumGDX.mouseX / TerrarumGDX.ingame!!.screenZoom) / FeaturesDrawer.TILE_SIZE).toInt()
        val mouseTileY = ((WorldCamera.y + TerrarumGDX.mouseY / TerrarumGDX.ingame!!.screenZoom) / FeaturesDrawer.TILE_SIZE).toInt()

        batch.color = Color(0xFFEE88FF.toInt())

        val hitbox = player?.hitbox

        /**
         * First column
         */

        printLineColumn(batch, 1, 1, "startX "
                                 + ccG
                                 + "${hitbox?.startX}"
                                 + " ("
                                 + "${(hitbox?.startX?.div(FeaturesDrawer.TILE_SIZE))?.toInt()}"
                                 + ")")
        printLineColumn(batch, 2, 1, "endX "
                                 + ccG
                                 + "${hitbox?.endX}"
                                 + " ("
                                 + "${(hitbox?.endX?.div(FeaturesDrawer.TILE_SIZE))?.toInt()}"
                                 + ")")
        printLineColumn(batch, 1, 2, "startY "
                                 + ccG
                                 + "${hitbox?.startY}"
                                 + " ("
                                 + "${(hitbox?.startY?.div(FeaturesDrawer.TILE_SIZE))?.toInt()}"
                                 + ")")
        printLineColumn(batch, 2, 2, "endY "
                                 + ccG
                                 + "${hitbox?.endY}"
                                 + " ("
                                 + "${(hitbox?.endY?.div(FeaturesDrawer.TILE_SIZE))?.toInt()}"
                                 + ")")

        printLine(batch, 3, "veloX reported $ccG${player?.externalForce?.x}")
        printLine(batch, 4, "veloY reported $ccG${player?.externalForce?.y}")

        printLine(batch, 5, "p_WalkX $ccG${player?.controllerMoveDelta?.x}")
        printLine(batch, 6, "p_WalkY $ccG${player?.controllerMoveDelta?.y}")

        printLineColumn(batch, 2, 3, "veloX measured $ccG${xdelta}")
        printLineColumn(batch, 2, 4, "veloY measured $ccG${ydelta}")

        if (player != null) {
            printLineColumn(batch, 1, 7,
                    "walled " +
                    "${if (player.walledLeft) "$ccR" else "$ccG"}L" +
                    "${if (player.walledBottom) "$ccR" else "$ccG"}${0x1F.toChar()}" +
                    "${if (player.walledTop) "$ccR" else "$ccG"}${0x1E.toChar()}" +
                    "${if (player.walledRight) "$ccR" else "$ccG"}R" +
                    "${if (player.colliding) "$ccR" else "$ccG"}${0x08.toChar()}"
            )
        }



        //printLine(batch, 7, "jump $ccG${player.jumpAcc}")

        val lightVal: String
        val mtX = mouseTileX.toString()
        val mtY = mouseTileY.toString()
        val valRaw = LightmapRenderer.getValueFromMap(mouseTileX, mouseTileY) ?: -1
        val rawR = valRaw.rawR()
        val rawG = valRaw.rawG()
        val rawB = valRaw.rawB()

        lightVal = if (valRaw == -1) "—"
                   else valRaw.toString() + " (" +
                    rawR.toString() + " " +
                    rawG.toString() + " " +
                    rawB.toString() + ")"
        printLine(batch, 8, "light@cursor $ccG$lightVal")

        val tileNum = TerrarumGDX.ingame!!.world.getTileFromTerrain(mouseTileX, mouseTileY) ?: -1

        printLine(batch, 9, "tile@cursor $ccG$tileNum ($mtX, $mtY)")

        /**
         * Second column
         */

        //printLineColumn(batch, 2, 1, "VSync $ccG" + Terrarum.appgc.isVSyncRequested)
        //printLineColumn(batch, 2, 2, "Env colour temp $ccG" + FeaturesDrawer.colTemp)

        printLineColumn(batch, 2, 5, "Time $ccG${TerrarumGDX.ingame!!.world.time.todaySeconds.toString().padStart(5, '0')}" +
                                 " (${TerrarumGDX.ingame!!.world.time.getFormattedTime()})")
        printLineColumn(batch, 2, 6, "Mass $ccG${player?.mass}")

        printLineColumn(batch, 2, 7, "noClip $ccG${player?.noClip}")


        drawHistogram(batch, LightmapRenderer.histogram,
                Gdx.graphics.width - histogramW - 30,
                Gdx.graphics.height - histogramH - 30
        )

        batch.color = Color.WHITE

        if (TerrarumGDX.controller != null) {
            drawGamepadAxis(batch,
                    TerrarumGDX.controller!!.getAxisValue(3),
                    TerrarumGDX.controller!!.getAxisValue(2),
                    Gdx.graphics.width - 135,
                    40
            )
        }

        /**
         * Top right
         */

        //g.color = GameFontBase.codeToCol["y"]
        TerrarumGDX.fontSmallNumbers.draw(batch, "${ccY}MEM ", (Gdx.graphics.width - 21 * 8 - 2).toFloat(), 2f)
        //g.draw(batch, "${ccY}FPS $ccG${Terrarum.appgc.fps}", (Gdx.graphics.width - 6 * 8 - 2).toFloat(), 10f)
        TerrarumGDX.fontSmallNumbers.draw(batch, "${ccY}CPUs ${if (TerrarumGDX.MULTITHREAD) ccG else ccR}${TerrarumGDX.THREADS}",
                (Gdx.graphics.width - 2 - 6*8).toFloat(), 10f)

        //g.color = GameFontBase.codeToCol["g"]
        TerrarumGDX.fontSmallNumbers.draw(batch, "${TerrarumGDX.memInUse}M",
                (Gdx.graphics.width - 17 * 8 - 2).toFloat(), 2f)
        TerrarumGDX.fontSmallNumbers.draw(batch, "/${TerrarumGDX.memTotal}M/",
                (Gdx.graphics.width - 12 * 8 - 2).toFloat(), 2f)
        //TerrarumGDX.fontSmallNumbers.color = GameFontBase.codeToCol["m"]
        TerrarumGDX.fontSmallNumbers.draw(batch, "${TerrarumGDX.memXmx}M",
                (Gdx.graphics.width - 5 * 8 - 2).toFloat(), 2f)

        /**
         * Bottom left
         */

        TerrarumGDX.fontSmallNumbers.draw(batch, "${ccY}Actors total $ccG${TerrarumGDX.ingame!!.actorContainer.size + TerrarumGDX.ingame!!.actorContainerInactive.size}",
                2f, Gdx.graphics.height - 10f)
        TerrarumGDX.fontSmallNumbers.draw(batch, "${ccY}Active $ccG${TerrarumGDX.ingame!!.actorContainer.size}",
                (2 + 17*8).toFloat(), Gdx.graphics.height - 10f)
        TerrarumGDX.fontSmallNumbers.draw(batch, "${ccY}Dormant $ccG${TerrarumGDX.ingame!!.actorContainerInactive.size}",
                (2 + 28*8).toFloat(), Gdx.graphics.height - 10f)
        TerrarumGDX.fontSmallNumbers.draw(batch, "${ccM}Particles $ccG${TerrarumGDX.ingame!!.particlesActive}",
                (2 + 41*8).toFloat(), Gdx.graphics.height - 10f)
    }

    private fun printLine(batch: SpriteBatch, l: Int, s: String) {
        TerrarumGDX.fontSmallNumbers.draw(batch,
                s, 10f, line(l)
        )
    }

    private fun printLineColumn(batch: SpriteBatch, col: Int, row: Int, s: String) {
        TerrarumGDX.fontSmallNumbers.draw(batch,
                s, (10 + column(col)), line(row)
        )
    }

    val histogramW = 256
    val histogramH = 200

    private fun drawHistogram(batch: SpriteBatch, histogram: LightmapRenderer.Histogram, x: Int, y: Int) {
        val uiColour = Color(0x000000_80.toInt())
        val barR = Color(0xDD0000_FF.toInt())
        val barG = Color(0x00DD00_FF.toInt())
        val barB = Color(0x0000DD_FF.toInt())
        val barColour = arrayOf(barR, barG, barB)
        val w = histogramW.toFloat()
        val h = histogramH.toFloat()
        val range = histogram.range
        val histogramMax = histogram.screen_tiles.toFloat()

        batch.color = uiColour
        batch.fillRect(x.toFloat(), y.toFloat(), w.plus(1), h)
        batch.color = Color.GRAY
        TerrarumGDX.fontSmallNumbers.draw(batch, "0", x.toFloat(), y.toFloat() + h + 2)
        TerrarumGDX.fontSmallNumbers.draw(batch, "255", x.toFloat() + w + 1 - 8*3, y.toFloat() + h + 2)
        TerrarumGDX.fontSmallNumbers.draw(batch, "Histogramme", x + w / 2 - 5.5f * 8, y.toFloat() + h + 2)

        //blendScreen()
        for (c in 0..2) {
            for (i in 0..255) {
                var histogram_value = if (i == 255) 0 else histogram.get(c)[i]
                if (i == 255) {
                    for (k in 255..range - 1) {
                        histogram_value += histogram.get(c)[k]
                    }
                }

                val bar_x = x + (w / w.minus(1f)) * i.toFloat()
                val bar_h = FastMath.ceil(h / histogramMax * histogram_value.toFloat()).toFloat()
                val bar_y = y + (h / histogramMax) - bar_h + h
                val bar_w = 1f

                batch.color = barColour[c]
                batch.fillRect(bar_x, bar_y, bar_w, bar_h)
            }
        }
        //blendNormal()
    }

    private fun drawGamepadAxis(batch: SpriteBatch, axisX: Float, axisY: Float, uiX: Int, uiY: Int) {
        val uiColour = Color(0xAA000000.toInt())
        val w = 128f
        val h = 128f
        val halfW = w / 2f
        val halfH = h / 2f

        val pointDX = axisX * halfW
        val pointDY = axisY * halfH

        val padName = if (TerrarumGDX.controller!!.name.isEmpty()) "Gamepad"
                      else TerrarumGDX.controller!!.name

        blendNormal()

        batch.end()
        TerrarumGDX.inShapeRenderer {
            it.color = uiColour
            it.rect(uiX.toFloat(), uiY.toFloat(), w, h)
            it.color = Color.WHITE
            it.line(uiX + halfW, uiY + halfH, uiX + halfW + pointDX, uiY + halfH + pointDY)
            it.color = Color.GRAY
        }
        batch.begin()

        TerrarumGDX.fontSmallNumbers.draw(batch, padName, uiX + w / 2 - (padName.length) * 4, uiY.toFloat() + h + 2)

    }

    private fun line(i: Int): Float = i * 10f

    private fun column(i: Int): Float = 300f * (i - 1)

    override fun processInput(delta: Float) {
    }

    override fun doOpening(delta: Float) {
    }

    override fun doClosing(delta: Float) {
    }

    override fun endOpening(delta: Float) {
    }

    override fun endClosing(delta: Float) {
    }
}