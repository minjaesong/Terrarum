package net.torvald.terrarum

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import kotlin.system.measureNanoTime

/**
 * Must be called by the App Loader
 */
object PostProcessor {

    private lateinit var batch: SpriteBatch // not nulling to save some lines of code
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var camera: OrthographicCamera

    private lateinit var lutTex: Texture

    private var init = false

    fun reloadLUT(filename: String) {
        lutTex = Texture(Gdx.files.internal("assets/clut/$filename"))
    }

    private val defaultResCol = Color(0x66ffff66)
    private val safeAreaCol = Color(0xffffff66.toInt())
    private val safeAreaCol2 = Color(0xffffff44.toInt())

    fun draw(projMat: Matrix4, fbo: FrameBuffer) {

        // init
        if (!init) {
            batch = SpriteBatch()
            camera = OrthographicCamera(AppLoader.screenW.toFloat(), AppLoader.screenH.toFloat())
            camera.setToOrtho(true)

            batch.projectionMatrix = camera.combined

            shapeRenderer = ShapeRenderer()
            Gdx.gl20.glViewport(0, 0, AppLoader.appConfig.width, AppLoader.appConfig.height)
        }




        AppLoader.debugTimers["Renderer.PostProcessor"] = measureNanoTime {

            gdxClearAndSetBlend(.094f, .094f, .094f, 0f)

            val shader: ShaderProgram? =
                    if (AppLoader.getConfigBoolean("fxdither"))
                            AppLoader.shaderHicolour
                    else
                        null

            fbo.colorBufferTexture.bind(0)

            shader?.begin()
            shader?.setUniformMatrix("u_projTrans", projMat)
            shader?.setUniformi("u_texture", 0)
            AppLoader.fullscreenQuad.render(shader, GL20.GL_TRIANGLES)
            shader?.end()


            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0) // so that batch that comes next will bind any tex to it

            if (AppLoader.IS_DEVELOPMENT_BUILD) {
                val tvSafeAreaW = AppLoader.getTvSafeGraphicsWidth().toFloat()
                val tvSafeAreaH = AppLoader.getTvSafeGraphicsHeight().toFloat()
                val tvSafeArea2W = AppLoader.getTvSafeActionWidth().toFloat()
                val tvSafeArea2H = AppLoader.getTvSafeActionHeight().toFloat()

                shapeRenderer.inUse(ShapeRenderer.ShapeType.Line) {
                    shapeRenderer.color = safeAreaCol2
                    shapeRenderer.rect(
                            tvSafeArea2W, tvSafeArea2H, AppLoader.screenW - 2 * tvSafeArea2W, AppLoader.screenH - 2 * tvSafeArea2H
                    )

                    shapeRenderer.color = safeAreaCol
                    shapeRenderer.rect(
                        tvSafeAreaW, tvSafeAreaH, AppLoader.screenW - 2 * tvSafeAreaW, AppLoader.screenH - 2 * tvSafeAreaH
                    )

                    shapeRenderer.color = defaultResCol
                    shapeRenderer.rect(
                            (AppLoader.screenW - AppLoader.defaultW).div(2).toFloat(),
                            (AppLoader.screenH - AppLoader.defaultH).div(2).toFloat(),
                            AppLoader.defaultW.toFloat(),
                            AppLoader.defaultH.toFloat()
                    )
                }

                try {
                    batch.inUse {
                        batch.color = safeAreaCol
                        AppLoader.fontSmallNumbers.draw(
                                batch, safeAreaStr,
                                tvSafeAreaW, tvSafeAreaH - 10
                        )

                        batch.color = defaultResCol
                        AppLoader.fontSmallNumbers.draw(
                                batch, defaultResStr,
                                (AppLoader.screenW - AppLoader.defaultW).div(2).toFloat(),
                                (AppLoader.screenH - AppLoader.defaultH).div(2).minus(10).toFloat()
                        )
                    }
                }
                catch (doNothing: NullPointerException) { }
            }
        }
    }

    private val defaultResStr = "${AppLoader.defaultW}x${AppLoader.defaultH}"
    private val safeAreaStr = "TV Safe Area"

    /**
     * Camera will be moved so that (newX, newY) would be sit on the top-left edge.
     */
    /*private fun setCameraPosition(newX: Float, newY: Float) {
        camera.position.set((-newX + Terrarum.HALFW).round(), (-newY + Terrarum.HALFH).round(), 0f)
        camera.update()
        batch.projectionMatrix = camera.combined
    }*/

}