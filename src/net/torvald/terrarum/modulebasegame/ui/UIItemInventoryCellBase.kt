package net.torvald.terrarum.modulebasegame.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import net.torvald.terrarum.GdxColorMap
import net.torvald.terrarum.gameitem.GameItem
import net.torvald.terrarum.ui.UIItem
import kotlin.math.roundToInt

/**
 * Cross section of two inventory cell types
 *
 * Created by minjaesong on 2017-10-22.
 */
abstract class UIItemInventoryCellBase(
        parentUI: UIInventoryFull,
        override var posX: Int,
        override var posY: Int,
        open var item: GameItem?,
        open var amount: Int,
        open var itemImage: TextureRegion?,
        open var quickslot: Int? = null,
        open var equippedSlot: Int? = null
) : UIItem(parentUI) {
    abstract override fun update(delta: Float)
    abstract override fun render(batch: SpriteBatch, camera: Camera)
}

object UIItemInventoryCellCommonRes {
    val meterColourMap = GdxColorMap(Gdx.files.internal("./assets/clut/health_bar_colouring_4096.tga"))
    val meterBackDarkening = Color(0x828282ff.toInt())

    fun getHealthMeterColour(value: Float, start: Float, end: Float): Color {
        if (start > end) throw IllegalArgumentException("Start value is greater than end value: $start..$end")

        return if (value <= start)
            meterColourMap[0]
        else if (value >= end)
            meterColourMap[meterColourMap.width - 1]
        else {
            val scale = (value - start) / (end - start)
            meterColourMap[scale.times(meterColourMap.width - 1).roundToInt()]
        }
    }

    fun getHealthMeterColour(value: Int, start: Int, end: Int) = getHealthMeterColour(value.toFloat(), start.toFloat(), end.toFloat())
}