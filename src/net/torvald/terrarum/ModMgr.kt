package net.torvald.terrarum

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import net.torvald.terrarum.AppLoader.*
import net.torvald.terrarum.blockproperties.BlockCodex
import net.torvald.terrarum.gameitem.GameItem
import net.torvald.terrarum.itemproperties.ItemCodex
import net.torvald.terrarum.gameitem.ItemID
import net.torvald.terrarum.itemproperties.MaterialCodex
import net.torvald.terrarum.langpack.Lang
import net.torvald.terrarum.utils.CSVFetcher
import net.torvald.terrarum.utils.JsonFetcher
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.file.FileSystems
import java.util.*



/**
 * Modules (or Mods) Resource Manager
 *
 *
 * NOTE!!: Usage of Groovy is only temporary; if Kotlin's "JSR 223" is no longer experimental and
 *         is readily available, ditch that Groovy.
 *
 *
 * Created by minjaesong on 2017-04-17.
 */
object ModMgr {

    val metaFilename = "metadata.properties"
    val defaultConfigFilename = "default.json"

    data class ModuleMetadata(
            val order: Int,
            val isDir: Boolean,
            val properName: String,
            val description: String,
            val author: String,
            val entryPoint: String,
            val releaseDate: String,
            val version: String,
            val libraries: Array<String>,
            val dependencies: Array<String>
    ) {
        override fun toString() =
                "\tModule #$order -- $properName | $version | $author\n" +
                "\t$description | $releaseDate\n" +
                "\tEntry point: $entryPoint\n" +
                "\tExternal libraries: ${libraries.joinToString(", ")}\n" +
                "\tDependencies: ${dependencies.joinToString("\n\t")}"
    }
    const val modDir = "./assets/mods"

    /** Module name (directory name), ModuleMetadata */
    val moduleInfo = HashMap<String, ModuleMetadata>()
    val entryPointClasses = ArrayList<ModuleEntryPoint>()

    init {
        // load modules
        val loadOrderCSVparser = CSVParser.parse(
                FileSystems.getDefault().getPath("$modDir/LoadOrder.csv").toFile(),
                Charsets.UTF_8,
                CSVFormat.DEFAULT.withCommentMarker('#')
        )
        val loadOrder = loadOrderCSVparser.records
        loadOrderCSVparser.close()


        loadOrder.forEachIndexed { index, it ->
            val moduleName = it[0]
            printmsg(this, "Loading module $moduleName")

            try {
                val modMetadata = Properties()
                modMetadata.load(FileInputStream("$modDir/$moduleName/$metaFilename"))

                if (File("$modDir/$moduleName/$defaultConfigFilename").exists()) {
                    val defaultConfig = JsonFetcher("$modDir/$moduleName/$defaultConfigFilename")
                    // read config and store it to the game

                    // write to user's config file
                }



                val properName = modMetadata.getProperty("propername")
                val description = modMetadata.getProperty("description")
                val author = modMetadata.getProperty("author")
                val entryPoint = modMetadata.getProperty("entrypoint")
                val releaseDate = modMetadata.getProperty("releasedate")
                val version = modMetadata.getProperty("version")
                val libs = modMetadata.getProperty("libraries").split(Regex(""";[ ]*""")).toTypedArray()
                val dependency = modMetadata.getProperty("dependency").split(Regex(""";[ ]*""")).toTypedArray()
                val isDir = FileSystems.getDefault().getPath("$modDir/$moduleName").toFile().isDirectory
                moduleInfo[moduleName] = ModuleMetadata(index, isDir, properName, description, author, entryPoint, releaseDate, version, libs, dependency)

                printdbg(this, moduleInfo[moduleName])


                // run entry script in entry point
                if (entryPoint.isNotBlank()) {
                    val newClass = Class.forName(entryPoint)
                    val newClassConstructor = newClass.getConstructor(/* no args defined */)
                    val newClassInstance = newClassConstructor.newInstance(/* no args defined */)

                    entryPointClasses.add(newClassInstance as ModuleEntryPoint)
                    (newClassInstance as ModuleEntryPoint).invoke()
                }


                printdbg(this, "$moduleName loaded successfully")
            }
            catch (noSuchModule: FileNotFoundException) {
                printdbgerr(this, "No such module: $moduleName, skipping...")
                moduleInfo.remove(moduleName)
            }
            catch (e: ClassNotFoundException) {
                printdbgerr(this, "$moduleName has nonexisting entry point, skipping...")
                moduleInfo.remove(moduleName)
            }
        }


        // lists available engines
        /*val manager = ScriptEngineManager()
        val factories = manager.engineFactories
        for (f in factories) {
            println("engine name:" + f.engineName)
            println("engine version:" + f.engineVersion)
            println("language name:" + f.languageName)
            println("language version:" + f.languageVersion)
            println("names:" + f.names)
            println("mime:" + f.mimeTypes)
            println("extension:" + f.extensions)
            println("-----------------------------------------------")
        }*/
    }

    operator fun invoke() { }

    private fun checkExistence(module: String) {
        if (!moduleInfo.containsKey(module))
            throw FileNotFoundException("No such module: $module")
    }
    private fun String.sanitisePath() = if (this[0] == '/' || this[0] == '\\')
        this.substring(1..this.lastIndex)
    else this



    fun getPath(module: String, path: String): String {
        checkExistence(module)
        return "$modDir/$module/${path.sanitisePath()}"
    }
    /** Returning files are read-only */
    fun getGdxFile(module: String, path: String): FileHandle {
        return Gdx.files.internal(getPath(module, path))
    }
    fun getFile(module: String, path: String): File {
        checkExistence(module)
        return FileSystems.getDefault().getPath(getPath(module, path)).toFile()
    }
    fun getFiles(module: String, path: String): Array<File> {
        checkExistence(module)
        val dir = FileSystems.getDefault().getPath(getPath(module, path)).toFile()
        if (!dir.isDirectory) {
            throw FileNotFoundException("The path is not a directory")
        }
        else {
            return dir.listFiles()
        }
    }

    /** Get a common file (literal file or directory) from all the installed mods. Files are guaranteed to exist. If a mod does not
     * contain the file, the mod will be skipped.
     *
     * @return List of pairs<modname, file>
     */
    fun getFilesFromEveryMod(path: String): List<Pair<String, File>> {
        val path = path.sanitisePath()
        val moduleNames = moduleInfo.keys.toList()

        val filesList = ArrayList<Pair<String, File>>()
        moduleNames.forEach {
            val file = File(getPath(it, path))

            if (file.exists()) filesList.add(it to file)
        }

        return filesList.toList()
    }

    /** Get a common file (literal file or directory) from all the installed mods. Files are guaranteed to exist. If a mod does not
     * contain the file, the mod will be skipped.
     *
     * Returning files are read-only.
     * @return List of pairs<modname, filehandle>
     */
    fun getGdxFilesFromEveryMod(path: String): List<Pair<String, FileHandle>> {
        val path = path.sanitisePath()
        val moduleNames = moduleInfo.keys.toList()

        val filesList = ArrayList<Pair<String, FileHandle>>()
        moduleNames.forEach {
            val file = Gdx.files.internal(getPath(it, path))

            if (file.exists()) filesList.add(it to file)
        }

        return filesList.toList()
    }

    fun disposeMods() {
        entryPointClasses.forEach { it.dispose() }
    }


    object GameBlockLoader {
        val blockPath = "blocks/"

        @JvmStatic operator fun invoke(module: String) {
            BlockCodex(module, blockPath + "blocks.csv")
        }
    }

    object GameItemLoader {
        val itemPath = "items/"

        @JvmStatic operator fun invoke(module: String) {
            val csv = CSVFetcher.readFromModule(module, itemPath + "itemid.csv")
            csv.forEach {
                val className: String = it["classname"].toString()
                val internalID: Int = it["id"].toInt()
                val itemName: String = "item@$module:$internalID"

                printdbg(this, "Reading item  ${itemName} <<- internal #$internalID with className $className")

                val loadedClass = Class.forName(className)
                val loadedClassConstructor = loadedClass.getConstructor(ItemID::class.java)
                val loadedClassInstance = loadedClassConstructor.newInstance(itemName)

                ItemCodex[itemName] = loadedClassInstance as GameItem
            }
        }
    }

    object GameLanguageLoader {
        val langPath = "locales/"

        @JvmStatic operator fun invoke(module: String) {
            Lang.load(getPath(module, langPath))
        }
    }

    object GameMaterialLoader {
        val matePath = "materials/"

        @JvmStatic operator fun invoke(module: String) {
            MaterialCodex(module, matePath + "materials.csv")
        }
    }
}