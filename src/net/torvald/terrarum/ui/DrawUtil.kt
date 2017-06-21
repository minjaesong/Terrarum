package net.torvald.terrarum.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch


/**
 * Created by minjaesong on 16-08-04.
 */
object DrawUtil {
    fun drawCentered(batch: SpriteBatch, image: Texture, screenPosY: Int, ui: UICanvas? = null) {
        val imageW = image.width
        val targetW = if (ui == null) Gdx.graphics.width else ui.width

        batch.draw(image, targetW.minus(imageW).ushr(1).toFloat(), screenPosY.toFloat())
    }

    fun drawCentered(batch: SpriteBatch, image: Texture, screenPosY: Int, targetW: Int, offsetX: Int = 0, offsetY: Int = 0) {
        val imageW = image.width
        batch.draw(image, targetW.minus(imageW).ushr(1).toFloat() + offsetX, screenPosY.toFloat() + offsetY)
    }
}