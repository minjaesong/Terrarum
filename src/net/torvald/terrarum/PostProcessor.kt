package net.torvald.terrarum

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4
import kotlin.system.measureNanoTime

/**
 * Must be called by the App Loader
 */
object PostProcessor {

    private lateinit var batch: SpriteBatch // not nulling to save some lines of code
    //private lateinit var camera: OrthographicCamera
    private var textureRegion: TextureRegion? = null


    private lateinit var lutTex: Texture

    fun reloadLUT(filename: String) {
        lutTex = Texture(Gdx.files.internal("assets/clut/$filename"))
    }

    fun draw(projMat: Matrix4, fbo: FrameBuffer) {

        if (textureRegion == null) {
            textureRegion = TextureRegion(fbo.colorBufferTexture)
            batch = SpriteBatch()
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


        }
    }

    /**
     * Camera will be moved so that (newX, newY) would be sit on the top-left edge.
     */
    /*private fun setCameraPosition(newX: Float, newY: Float) {
        camera.position.set((-newX + Terrarum.HALFW).round(), (-newY + Terrarum.HALFH).round(), 0f)
        camera.update()
        batch.projectionMatrix = camera.combined
    }*/

}