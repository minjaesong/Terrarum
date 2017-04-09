package net.torvald.terrarum.ui

import net.torvald.colourutil.CIELabUtil.darkerLab
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameactors.ActorHumanoid
import net.torvald.terrarum.gameactors.floorInt
import net.torvald.terrarum.gameactors.roundInt
import net.torvald.terrarum.mapdrawer.MapCamera
import org.newdawn.slick.Color
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics
import org.newdawn.slick.Input

/**
 * Created by SKYHi14 on 2017-03-03.
 */
class UIVitalMetre(
        var player: ActorHumanoid?,
        var vitalGetterVal: () -> Float?,
        var vitalGetterMax: () -> Float?,
        var color: Color?,
        val order: Int
) : UICanvas {

    private val margin = 25
    private val gap = 4f

    override var width: Int = 80 + 2 * margin; set(value) { throw Error("operation not permitted") }
    override var height: Int; get() = player?.baseHitboxH ?: 0 * 3 + margin; set(value) { throw Error("operation not permitted") }
    override var handler: UIHandler? = null
        set(value) {
            // override customPositioning to be true
            if (value != null) {
                value.customPositioning = true
            }
            field = value
        }
    override var openCloseTime: Int = 50

    //private val relativePX = width / 2f
    private val offsetY: Float; get() = (player?.baseHitboxH ?: 0) * 1.5f
    private val circleRadius: Float; get() = (player?.baseHitboxH ?: 0) * 3f

    private val theta = 33f
    private val halfTheta = theta / 2f

    private val backColor: Color; get() = color?.darkerLab(0.4f) ?: Color.black


    override fun update(gc: GameContainer, delta: Int) {
        handler!!.setPosition(
                Terrarum.HALFW,
                Terrarum.HALFH
        )

        handler!!.customPositioning = true
    }

    /**
     * g must be same as World Graphics!
     */
    override fun render(gc: GameContainer, g: Graphics) {
        if (vitalGetterVal() != null && vitalGetterMax() != null && player != null) {

            // FIXME does not work well with screen zoom, because of my custom g.translate

            g.translate(
                    -MapCamera.x + player!!.centrePosPoint.x.toFloat(),
                    -MapCamera.y + player!!.centrePosPoint.y.toFloat()
            )



            g.lineWidth = 2f

            // background
            g.color = backColor
            g.drawArc(
                    -circleRadius - order * gap,
                    -circleRadius - order * gap - offsetY,
                    circleRadius * 2f + order * gap * 2,
                    circleRadius * 2f + order * gap * 2,
                    90f - halfTheta,
                    90f + halfTheta - theta * (vitalGetterVal()!! / vitalGetterMax()!!)
            )

            g.color = color
            g.drawArc(
                    -circleRadius - order * gap,
                    -circleRadius - order * gap - offsetY,
                    circleRadius * 2f + order * gap * 2,
                    circleRadius * 2f + order * gap * 2,
                    90f + halfTheta - theta * (vitalGetterVal()!! / vitalGetterMax()!!),
                    90f + halfTheta
            )



            g.flush()
        }
    }

    override fun processInput(gc: GameContainer, delta: Int, input: Input) {
    }

    override fun doOpening(gc: GameContainer, delta: Int) {
        UICanvas.doOpeningFade(handler, openCloseTime)
    }

    override fun doClosing(gc: GameContainer, delta: Int) {
        UICanvas.doClosingFade(handler, openCloseTime)
    }

    override fun endOpening(gc: GameContainer, delta: Int) {
        UICanvas.endOpeningFade(handler)
    }

    override fun endClosing(gc: GameContainer, delta: Int) {
        UICanvas.endClosingFade(handler)
    }
}

/*

+-------------+ (84)
|             |
|             |
|      X      |
|      @      |
|,           ,|
| ''-------'' |
+-------------+

X: UICanvas position

 */