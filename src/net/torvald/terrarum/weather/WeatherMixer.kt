package net.torvald.terrarum.weather

import com.jme3.math.FastMath
import net.torvald.JsonFetcher
import net.torvald.colourutil.*
import net.torvald.random.HQRNG
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.blendMul
import net.torvald.terrarum.blendNormal
import net.torvald.terrarum.gameactors.ParticleTestRain
import net.torvald.terrarum.gamecontroller.Key
import net.torvald.terrarum.gamecontroller.KeyToggler
import net.torvald.terrarum.gameworld.WorldTime
import net.torvald.terrarum.getPixel
import org.newdawn.slick.Color
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics
import org.newdawn.slick.Image
import org.newdawn.slick.fills.GradientFill
import org.newdawn.slick.geom.Rectangle
import java.io.File
import java.util.*

/**
 * Current, next are there for cross-fading two weathers
 *
 *
 * Building a CLUT:
 *     Brightest:Darkest must be "around" 10:1
 *     Is RGBA-formatted (32-bit)
 *
 * Created by minjaesong on 16-07-11.
 */
object WeatherMixer {
    lateinit var weatherList: HashMap<String, ArrayList<BaseModularWeather>>

    lateinit var currentWeather: BaseModularWeather
    lateinit var nextWeather: BaseModularWeather

    lateinit var mixedWeather: BaseModularWeather

    val globalLightNow = Color(0)
    private val world = Terrarum.ingame!!.world

    // Weather indices
    const val WEATHER_GENERIC = "generic"
    const val WEATHER_GENERIC_RAIN = "genericrain"
    // TODO add weather classification indices manually

    const val RAW_DIR = "./assets/raw/weathers"

    init {
        weatherList = HashMap<String, ArrayList<BaseModularWeather>>()

        // read weather descriptions from assets/weather (modular weather)
        val weatherRawValidList = ArrayList<File>()
        val weatherRaws = File(RAW_DIR).listFiles()
        weatherRaws.forEach {
            if (!it.isDirectory && it.name.endsWith(".json"))
                weatherRawValidList.add(it)
        }
        // --> read from directory and store file that looks like RAW
        for (raw in weatherRawValidList) {
            val weather = readFromJson(raw)

            // if List for the classification does not exist, make one
            if (!weatherList.containsKey(weather.classification))
                weatherList.put(weather.classification, ArrayList())

            weatherList[weather.classification]!!.add(weather)
        }



        // initialise
        currentWeather = weatherList[WEATHER_GENERIC]!![0]
        nextWeather = getRandomWeather(WEATHER_GENERIC)
    }

    fun update(gc: GameContainer, delta: Int) {
        currentWeather = weatherList[WEATHER_GENERIC]!![0]


        // test rain toggled by F2
        if (KeyToggler.isOn(Key.F2)) {
            val playerPos = Terrarum.ingame!!.player.centrePosPoint
            kotlin.repeat(4) { // 4 seems good
                val rainParticle = ParticleTestRain(
                        playerPos.x + HQRNG().nextInt(Terrarum.WIDTH) - Terrarum.HALFW,
                        playerPos.y - Terrarum.HALFH
                )
                Terrarum.ingame!!.addParticle(rainParticle)
            }
            globalLightNow.set(getGlobalLightOfTime(world.time.todaySeconds).darker(0.3f))
        }
    }

    private fun Color.set(other: Color) {
        this.r = other.r
        this.g = other.g
        this.b = other.b
        this.a = other.a
    }

    fun render(g: Graphics) {

        // we will not care for nextSkybox for now
        val timeNow = Terrarum.ingame!!.world.time.todaySeconds
        val skyboxColourMap = currentWeather.skyboxGradColourMap
        val lightColourMap = currentWeather.globalLightColourMap

        // calculate global light
        val gradCol = getGradientColour(lightColourMap, 0, timeNow)
        globalLightNow.r = gradCol.r
        globalLightNow.g = gradCol.g
        globalLightNow.b = gradCol.b

        // draw skybox to provided (should be main) graphics instance
        val skyColourFill = GradientFill(
                0f, 0f,
                getGradientColour(skyboxColourMap, 0, timeNow),
                0f, Terrarum.HEIGHT.toFloat(),// / Terrarum.ingame!!.screenZoom,
                getGradientColour(skyboxColourMap, 1, timeNow)
        )

        blendNormal()
        g.fill(Rectangle(
                0f, 0f,
                Terrarum.WIDTH.toFloat(),// / Terrarum.ingame!!.screenZoom,
                Terrarum.HEIGHT.toFloat()// / Terrarum.ingame!!.screenZoom
        ), skyColourFill)
    }

    fun Float.clampOne() = if (this > 1) 1f else this

    private operator fun Color.times(other: Color) = Color(this.r * other.r, this.g * other.g, this.b * other.b, 1f)

    /**
     * Get a GL of specific time
     */
    fun getGlobalLightOfTime(timeInSec: Int): Color =
            getGradientColour(currentWeather.globalLightColourMap, 0, timeInSec)

    fun getGradientColour(image: Image, row: Int, timeInSec: Int): Color {
        val dataPointDistance = WorldTime.DAY_LENGTH / image.width

        val phaseThis: Int = timeInSec / dataPointDistance // x-coord in gradmap
        val phaseNext: Int = (phaseThis + 1) % image.width

        val colourThis = image.getPixel(phaseThis, row).toColor()
        val colourNext = image.getPixel(phaseNext, row).toColor()

        // interpolate R, G and B
        val scale = (timeInSec % dataPointDistance).toFloat() / dataPointDistance // [0.0, 1.0]

        val newCol = CIELChabUtil.getGradient(scale, colourThis, colourNext)

        /* // very nice monitor code
        // 65 -> 66 | 300 | 19623 | RGB8(255, 0, 255) -[41%]-> RGB8(193, 97, 23) | * `230`40`160`
        // ^ step   |width| time  | colour from        scale   colour to         | output
        if (dataPointDistance == 300)
            println("$phaseThis -> $phaseNext | $dataPointDistance | $timeInSec" +
                    " | ${colourThis.toStringRGB()} -[${scale.times(100).toInt()}%]-> ${colourNext.toStringRGB()}" +
                    " | * `$r`$g`$b`")*/

        return newCol
    }

    fun Color.toStringRGB() = "RGB8(${this.red}, ${this.green}, ${this.blue})"

    fun getWeatherList(classification: String) = weatherList[classification]!!
    fun getRandomWeather(classification: String) =
            getWeatherList(classification)[HQRNG().nextInt(getWeatherList(classification).size)]

    fun readFromJson(file: File): BaseModularWeather = readFromJson(file.path)

    fun readFromJson(path: String): BaseModularWeather {
        /* JSON structure:
{
  "globalLight": "colourmap/sky_colour.tga", // integer for static, string (path to image) for dynamic
  "skyboxGradColourMap": "colourmap/sky_colour.tga", // integer for static, string (path to image) for dynamic
  "extraImages": [
      // if any, it will be like:
      sun01.tga,
      clouds01.tga,
      clouds02.tga,
      auroraBlueViolet.tga
  ]
}
         */
        val pathToImage = "./assets/graphics/weathers"

        val JSON = JsonFetcher(path)

        val globalLightInJson = JSON.get("globalLight").asJsonPrimitive
        val skyboxInJson = JSON.get("skyboxGradColourMap").asJsonPrimitive
        val extraImagesPath = JSON.getAsJsonArray("extraImages")

        val globalLight: Image
        val skybox: Image
        val extraImages = ArrayList<Image>()
        val classification = JSON.get("classification").asJsonPrimitive.asString
        var mixFrom: String?
        try { mixFrom = JSON.get("mixFrom").asJsonPrimitive.asString }
        catch (e: NullPointerException) { mixFrom = null }
        var mixPercentage: Double?
        try { mixPercentage = JSON.get("mixPercentage").asJsonPrimitive.asDouble }
        catch (e: NullPointerException) { mixPercentage = null }

        // parse globalLight
        if (globalLightInJson.isString)
            globalLight = Image("$pathToImage/${globalLightInJson.asString}")
        else if (globalLightInJson.isNumber) {
            // make 1x1 image with specified colour
            globalLight = Image(1, 1)
            globalLight.graphics.color = Color(globalLightInJson.asNumber.toInt())
            globalLight.graphics.fillRect(0f, 0f, 1f, 1f)
        }
        else
            throw IllegalStateException("In weather descriptor $path -- globalLight seems malformed.")

        // parse skyboxGradColourMap
        if (skyboxInJson.isString)
            skybox = Image("$pathToImage/${skyboxInJson.asString}")
        else if (globalLightInJson.isNumber) {
            // make 1x2 image with specified colour
            skybox = Image(1, 2)
            skybox.graphics.color = Color(skyboxInJson.asNumber.toInt())
            skybox.graphics.fillRect(0f, 0f, 1f, 2f)
        }
        else
            throw IllegalStateException("In weather descriptor $path -- skyboxGradColourMap seems malformed.")

        // get extra images
        for (i in extraImagesPath)
            extraImages.add(Image("$pathToImage/${i.asString}"))

        // get mix from


        return BaseModularWeather(globalLight, skybox, classification, extraImages)
    }
}

fun IntArray.toColor() = Color(this[0], this[1], this[2], this[3])
