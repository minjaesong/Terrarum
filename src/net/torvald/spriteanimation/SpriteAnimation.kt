/* Original code author: Sean Laurvick
 * This code is based on the original author's code written in Lua.
 */

package net.torvald.spriteanimation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.jme3.math.FastMath
import net.torvald.terrarum.Second
import net.torvald.terrarum.gameactors.ActorWithBody
import net.torvald.terrarumsansbitmap.gdx.TextureRegionPack

/**
 * This class should not be serialised; save its Animation Description Language instead.
 */
class SpriteAnimation(@Transient val parentActor: ActorWithBody) {

    lateinit var textureRegion: TextureRegionPack; private set

    var currentFrame = 0
    var currentRow = 0

    var nFrames: IntArray = intArrayOf(1)
        internal set
    var nRows: Int = 1
        internal set

    private val currentDelay: Second
        get() = delays[currentRow]

    /**
     * Sets delays for each rows. Array size must be the same as the rows of the sheet
     */
    var delays: FloatArray = floatArrayOf(0.2f)
        set(value) {
            if (value.filter { it <= 0f }.isNotEmpty()) {
                throw IllegalArgumentException("Delay array contains zero or negative value: $delays")
            }

            field = value
        }

    private var delta = 0f

    val looping = true
    private var animationRunning = true
    var flipHorizontal = false
    var flipVertical = false
    private val visible: Boolean
        get() = parentActor.isVisible

    private val offsetX = 0
    private val offsetY = 0

    var cellWidth: Int = 0
    var cellHeight: Int = 0

    var colorFilter = Color.WHITE

    fun setSpriteImage(regionPack: TextureRegionPack) {
        textureRegion = regionPack

        cellWidth = regionPack.tileW
        cellHeight = regionPack.tileH
    }

    /**
     * Sets sheet rows and animation frames. Will default to
     * 1, 1 (still image of top left from the sheet) if not called.
     * @param nRows
     * *
     * @param nFrames
     */
    fun setRowsAndFrames(nRows: Int, nFrames: Int) {
        this.nRows = nRows
        this.nFrames = IntArray(nRows) { nFrames }
    }

    fun setFramesOf(row: Int, frameCount: Int) {
        nFrames[row] = frameCount
    }

    fun setFramesCount(framesCount: IntArray) {
        nFrames = framesCount
    }

    fun update(delta: Float) {
        if (animationRunning) {
            //skip this if animation is stopped
            this.delta += delta

            //println("delta accumulation: $delta, currentDelay: $currentDelay")

            //check if it's time to advance the frame
            while (this.delta >= currentDelay) {
                // advance frame
                if (looping) { // looping, wrap around
                    currentFrame = (currentFrame + 1) % nFrames[currentRow]
                }
                else if (currentFrame < nFrames[currentRow] - 1) { // not looping and haven't reached the end
                    currentFrame += 1
                }

                // discount counter
                this.delta -= currentDelay
            }

            //println("row, frame: $currentRow, $currentFrame")
        }
    }

    /**
     * Render to specific coordinates. Will assume bottom-center point as image position.
     * Will floor to integer.
     * @param g
     * *
     * @param posX bottom-center point
     * *
     * @param posY bottom-center point
     * *
     * @param scale
     */
    fun render(batch: SpriteBatch, posX: Float, posY: Float, scale: Float = 1f) {
        assert(cellWidth > 0 || cellHeight > 0) {
            "Sprite width or height is set to zero! ($cellWidth, $cellHeight); master: $parentActor"
        }

        if (visible) {
            val region = textureRegion.get(currentFrame, currentRow)
            batch.color = colorFilter

            val scale = parentActor.scale.toFloat()

            if (flipHorizontal && flipVertical) {
                batch.draw(region,
                        FastMath.floor(posX).toFloat() + (2f * parentActor.hitboxTranslateX * scale + parentActor.hitbox.width.toFloat()),
                        FastMath.floor(posY).toFloat() + (2f * parentActor.hitboxTranslateY * scale + parentActor.hitbox.height.toFloat()),
                        -FastMath.floor(cellWidth * scale).toFloat(),
                        -FastMath.floor(cellHeight * scale).toFloat()
                )
            }
            else if (flipHorizontal) {
                batch.draw(region,
                        FastMath.floor(posX).toFloat() + (2f * parentActor.hitboxTranslateX * scale + parentActor.hitbox.width.toFloat()),
                        FastMath.floor(posY).toFloat(),
                        -FastMath.floor(cellWidth * scale).toFloat(),
                        FastMath.floor(cellHeight * scale).toFloat()
                )
            }
            else if (flipVertical) {
                batch.draw(region,
                        FastMath.floor(posX).toFloat(),
                        FastMath.floor(posY).toFloat() + (2f * parentActor.hitboxTranslateY * scale + parentActor.hitbox.height.toFloat()),
                        FastMath.floor(cellWidth * scale).toFloat(),
                        -FastMath.floor(cellHeight * scale).toFloat()
                )
            }
            else {
                batch.draw(region,
                        FastMath.floor(posX).toFloat(),
                        FastMath.floor(posY).toFloat(),
                        FastMath.floor(cellWidth * scale).toFloat(),
                        FastMath.floor(cellHeight * scale).toFloat()
                )
            }
        }
    }

    fun switchRow(newRow: Int) {
        if (newRow != currentRow) {
            currentRow = newRow
            currentFrame = 1
        }
    }

    fun reset() {
        currentFrame = 1
    }

    fun start() {
        //starts the animation
        animationRunning = true
    }

    fun start(selectFrame: Int) {
        //starts the animation
        animationRunning = true

        //optional: seleft the frame no which to start the animation
        currentFrame = selectFrame
    }

    fun stop() {
        animationRunning = false
    }

    fun stop(selectFrame: Int) {
        animationRunning = false

        currentFrame = selectFrame
    }

    fun flip(horizontal: Boolean, vertical: Boolean) {
        flipHorizontal = horizontal
        flipVertical = vertical
    }

    fun dispose() {
        textureRegion.dispose()
    }
}
