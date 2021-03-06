package net.torvald.terrarum.modulebasegame.weather

import com.badlogic.gdx.graphics.Texture
import net.torvald.terrarum.GdxColorMap
import java.util.*

/**
 * Note: Colour maps are likely to have sparse data points
 * (i.e., they have 2 475 px in width and you'll need 79 200 data points for a day)
 * so between two must be interpolated.
 *
 * Created by minjaesong on 2016-07-11.
 */
data class BaseModularWeather(
        var skyboxGradColourMap: GdxColorMap, // row 0: skybox grad top, row 1: skybox grad bottom, row 2: sunlight (RGBA)
        val classification: String,
        var extraImages: ArrayList<Texture>,
        val mixFrom: String? = null,
        val mixPercentage: Double? = null
)