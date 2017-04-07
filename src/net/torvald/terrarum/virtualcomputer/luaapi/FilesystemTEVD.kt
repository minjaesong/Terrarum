package net.torvald.terrarum.virtualcomputer.luaapi

import org.luaj.vm2.*
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import net.torvald.terrarum.virtualcomputer.tvd.VDUtil.VDPath
import net.torvald.terrarum.virtualcomputer.computer.TerrarumComputer
import net.torvald.terrarum.virtualcomputer.luaapi.Term.Companion.checkIBM437
import net.torvald.terrarum.virtualcomputer.tvd.VDUtil
import net.torvald.terrarum.virtualcomputer.tvd.*
import net.torvald.terrarum.virtualcomputer.tvd.VDFileWriter
import java.io.*
import java.nio.charset.Charset
import java.util.*

/**
 * computer directory:
 * .../computers/
 *      media/hda/  ->  .../computers/<uuid for the hda>/
 * 
 * Created by minjaesong on 16-09-17.
 *
 *
 * NOTES:
 *      Don't convert '\' to '/'! Rev-slash is used for escape character in sh, and we're sh-compatible!
 *      Use .absoluteFile whenever possible; there's fuckin oddity! (http://bugs.java.com/bugdatabase/view_bug.do;:YfiG?bug_id=4483097)
 */
internal class Filesystem(globals: Globals, computer: TerrarumComputer) {

    init {
        // load things. WARNING: THIS IS MANUAL!
        globals["fs"] = LuaValue.tableOf()
        globals["fs"]["list"] = ListFiles(computer) // CC compliant
        globals["fs"]["exists"] = FileExists(computer) // CC/OC compliant
        globals["fs"]["isDir"] = IsDirectory(computer) // CC compliant
        globals["fs"]["isFile"] = IsFile(computer)
        globals["fs"]["isReadOnly"] = IsReadOnly(computer) // CC compliant
        globals["fs"]["getSize"] = GetSize(computer) // CC compliant
        globals["fs"]["mkdir"] = Mkdir(computer)
        globals["fs"]["mv"] = Mv(computer)
        globals["fs"]["cp"] = Cp(computer)
        globals["fs"]["rm"] = Rm(computer)
        globals["fs"]["concat"] = ConcatPath(computer) // OC compliant
        globals["fs"]["open"] = OpenFile(computer) //CC compliant
        globals["fs"]["parent"] = GetParentDir(computer)
        // fs.dofile defined in BOOT
        // fs.fetchText defined in ROMLIB
    }

    companion object {
        val sysCharset = Charset.forName("CP437")

        fun LuaValue.checkPath(): String {
            if (this.checkIBM437().contains(Regex("""\.\.""")))
                throw LuaError("'..' on path is not supported.")
            return this.checkIBM437().validatePath()
        }

        // Worst-case: we're on Windows or using a FAT32 partition mounted in *nix.
        // Note: we allow / as the path separator and expect all \s to be converted
        // accordingly before the path is passed to the file system.
        private val invalidChars = Regex("""[<>:"|?*\u0000-\u001F]""") // original OC uses Set(); we use regex

        fun isValidFilename(name: String) = !name.contains(invalidChars)

        fun String.validatePath() : String {
            if (!isValidFilename(this)) {
                throw IOException("path contains invalid characters")
            }
            return this
        }

        /**
         * return value is there for chaining only.
         */
        fun VDPath.dropMount(): VDPath {
            if (this.hierarchy.size >= 2 && this[0].toCanonicalString() == "media") {
                this.hierarchy.removeAt(0) // drop "media"
                this.hierarchy.removeAt(0) // drop whatever mount symbol
            }
            return this
        }

        /**
         * if path is {media, someUUID, subpath}, redirects to
         * computer.diskRack[SOMEUUID]->subpath
         * else, computer.diskRack["hda"]->subpath
         */
        fun TerrarumComputer.getFile(path: VDPath) : DiskEntry? {
            val disk = this.getTargetDisk(path)

            if (disk == null) return null

            path.dropMount()

            return VDUtil.getFile(disk, path)?.file
        }

        /**
         * if path is like {media, fd1, subpath}, return
         * computer.diskRack["fd1"]
         * else, computer.diskRack[<boot device>]
         */
        fun TerrarumComputer.getTargetDisk(path: VDPath) : VirtualDisk? {
            if (path.hierarchy.size >= 2 &&
                    Arrays.equals(path[0], "media".toEntryName(DiskEntry.NAME_LENGTH, sysCharset))) {
                val diskName = path[1].toCanonicalString()
                val disk = this.diskRack[diskName]

                return disk
            }
            else {
                return this.diskRack[this.computerValue.getAsString("boot")]
            }
        }

        fun TerrarumComputer.getDirectoryEntries(path: VDPath) : Array<DiskEntry>? {
            val directory = this.getFile(path)

            if (directory == null) return null
            return VDUtil.getDirectoryEntries(this.getTargetDisk(path)!!, directory)
        }

        fun combinePath(base: String, local: String) : String {
            return "$base$local".replace("//", "/")
        }

        private fun tryBool(action: (Unit) -> Unit): LuaValue {
            try {
                action(Unit)
                return LuaValue.valueOf(true)
            }
            catch (gottaCatchemAll: Exception) {
                return LuaValue.valueOf(false)
            }
        }
    } // end of Companion Object

    /**
     * @param cname == UUID of the drive
     *
     * actual directory: <appdata>/Saves/<savename>/computers/<drivename>/
     */
    class ListFiles(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            val path = VDPath(path.checkPath(), sysCharset)

            val table = LuaTable()
            try {
                val directoryContents = computer.getDirectoryEntries(path)!!
                directoryContents.forEachIndexed { index, diskEntry ->
                    table.insert(index + 1, LuaValue.valueOf(diskEntry.filename.toCanonicalString()))
                }
            }
            catch (e: KotlinNullPointerException) {}
            return table
        }
    }

    class FileExists(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            val path = VDPath(path.checkPath(), sysCharset)
            val disk = computer.getTargetDisk(path)

            if (disk == null) return LuaValue.valueOf(false)

            return LuaValue.valueOf(
                    VDUtil.getFile(disk, path.dropMount()) != null
            )
        }
    }

    class IsDirectory(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            val path = VDPath(path.checkPath(), sysCharset)
            return LuaValue.valueOf(computer.getFile(path)?.contents is EntryDirectory)
        }
    }

    class IsFile(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            val path = VDPath(path.checkPath(), sysCharset)
            return LuaValue.valueOf(computer.getFile(path)?.contents is EntryFile)
        }
    }

    class IsReadOnly(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            return LuaValue.valueOf(false)
        }
    }

    /** we have 2 GB file size limit */
    class GetSize(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            val path = VDPath(path.checkPath(), sysCharset)
            val file = computer.getFile(path)
            try {
                if (file!!.contents is EntryFile)
                    return LuaValue.valueOf(file.contents.getSizePure())
                else if (file.contents is EntryDirectory)
                    return LuaValue.valueOf(file.contents.entries.size)
            }
            catch (e: KotlinNullPointerException) {
            }

            return LuaValue.NONE
        }
    }

    // TODO class GetFreeSpace

    /**
     * returns true on success
     */
    class Mkdir(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            return tryBool {
                val path = VDPath(path.checkPath(), sysCharset)
                val disk = computer.getTargetDisk(path)!!

                VDUtil.addDir(disk, path.getParent(), path.last())
            }
        }
    }

    /**
     * moves a directory, overwrites the target
     */
    class Mv(val computer: TerrarumComputer) : TwoArgFunction() {
        override fun call(from: LuaValue, to: LuaValue) : LuaValue {
            return tryBool {
                val pathFrom = VDPath(from.checkPath(), sysCharset)
                val disk1 = computer.getTargetDisk(pathFrom)
                val pathTo = VDPath(to.checkPath(), sysCharset)
                val disk2 = computer.getTargetDisk(pathTo)

                VDUtil.moveFile(disk1!!, pathFrom, disk2!!, pathTo)
            }
        }
    }

    /**
     * copies a directory, overwrites the target
     * difference with ComputerCraft: it returns boolean, true on successful.
     */
    class Cp(val computer: TerrarumComputer) : TwoArgFunction() {
        override fun call(from: LuaValue, to: LuaValue) : LuaValue {
            return tryBool {
                val pathFrom = VDPath(from.checkPath(), sysCharset)
                val disk1 = computer.getTargetDisk(pathFrom)!!
                val pathTo = VDPath(to.checkPath(), sysCharset)
                val disk2 = computer.getTargetDisk(pathTo)!!

                val oldFile = VDUtil.getFile(disk2, pathTo)

                try {
                    VDUtil.deleteFile(disk2, pathTo)
                }
                catch (e: FileNotFoundException) {
                    "Nothing to delete beforehand"
                }

                val file = VDUtil.getFile(disk1, pathFrom)!!
                try {
                    VDUtil.addFile(disk2, pathTo.getParent(), file.file)
                }
                catch (e: FileNotFoundException) {
                    // roll back delete on disk2
                    if (oldFile != null) {
                        VDUtil.addFile(disk2, oldFile.parent.entryID, oldFile.file)
                        throw FileNotFoundException("No such destination")
                    }
                }
            }
        }
    }

    /**
     * difference with ComputerCraft: it returns boolean, true on successful.
     */
    class Rm(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            return tryBool {
                val path = VDPath(path.checkPath(), sysCharset)
                val disk = computer.getTargetDisk(path)!!

                VDUtil.deleteFile(disk, path)
            }
        }
    }

    class ConcatPath(val computer: TerrarumComputer) : TwoArgFunction() {
        override fun call(base: LuaValue, local: LuaValue) : LuaValue {
            TODO()
        }
    }

    /**
     * @param mode: r, rb, w, wb, a, ab
     *
     * Difference: TEXT MODE assumes CP437 instead of UTF-8!
     *
     * When you have opened a file you must always close the file handle, or else data may not be saved.
     *
     * FILE class in CC:
     * (when you look thru them using file = fs.open("./test", "w")
     *
     * file = {
     *      close = function()
     *      -- write mode
     *      write = function(string)
     *      flush = function() -- write, keep the handle
     *      writeLine = function(string) -- text mode
     *      -- read mode
     *      readLine = function() -- text mode
     *      readAll = function()
     *      -- binary read mode
     *      read = function() -- read single byte. return: number or nil
     *      -- binary write mode
     *      write = function(byte)
     *      writeBytes = function(string as bytearray)
     * }
     */
    class OpenFile(val computer: TerrarumComputer) : TwoArgFunction() {
        override fun call(path: LuaValue, mode: LuaValue) : LuaValue {
            val path = VDPath(path.checkPath(), sysCharset)
            val disk = computer.getTargetDisk(path)!!

            path.dropMount()

            val mode = mode.checkIBM437().toLowerCase()
            val luaClass = LuaTable()
            val fileEntry = computer.getFile(path)!!

            if (fileEntry.contents is EntryDirectory) {
                throw LuaError("File '${fileEntry.getFilenameString(sysCharset)}' is directory.")
            }

            val file = fileEntry.contents as EntryFile

            if (mode.contains(Regex("""[aw]""")))
                throw LuaError("Cannot open file for " +
                               "${if (mode.startsWith('w')) "read" else "append"} mode" +
                               ": is readonly.")


            when (mode) {
                "r"  -> {
                    try {
                        val fr = StringReader(String(file.bytes, sysCharset))//FileReader(file)
                        luaClass["close"] = FileClassClose(fr)
                        luaClass["readLine"] = FileClassReadLine(fr)
                        luaClass["readAll"] = FileClassReadAll(file)
                    }
                    catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        throw LuaError(
                                if (e.message != null && e.message!!.contains(Regex("""[Aa]ccess (is )?denied""")))
                                    "$path: access denied."
                                else
                                    "$path: no such file."
                        )
                    }
                }
                "rb" -> {
                    try {
                        val fis = ByteArrayInputStream(file.bytes)
                        luaClass["close"] = FileClassClose(fis)
                        luaClass["read"] = FileClassReadByte(fis)
                        luaClass["readAll"] = FileClassReadAll(file)
                    }
                    catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        throw LuaError("$path: no such file.")
                    }
                }
                "w", "a"  -> {
                    try {
                        val fw = VDFileWriter(fileEntry, mode.startsWith('a'), sysCharset)
                        luaClass["close"] = FileClassClose(fw)
                        luaClass["write"] = FileClassPrintText(fw)
                        luaClass["writeLine"] = FileClassPrintlnText(fw)
                        luaClass["flush"] = FileClassFlush(fw)
                    }
                    catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        throw LuaError("$path: is a directory.")
                    }
                }
                "wb", "ab" -> {
                    try {
                        val fos = VDFileOutputStream(fileEntry, mode.startsWith('a'), sysCharset)
                        luaClass["close"] = FileClassClose(fos)
                        luaClass["write"] = FileClassWriteByte(fos)
                        luaClass["writeBytes"] = FileClassWriteBytes(fos)
                        luaClass["flush"] = FileClassFlush(fos)
                    }
                    catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        throw LuaError("$path: is a directory.")
                    }
                }
            }

            return luaClass
        }
    }

    class GetParentDir(val computer: TerrarumComputer) : OneArgFunction() {
        override fun call(path: LuaValue) : LuaValue {
            val path = VDPath(path.checkPath(), sysCharset).getParent()
            return LuaValue.valueOf(path.toString())
        }
    }


    //////////////////////////////
    // OpenFile implementations //
    //////////////////////////////

    private class FileClassClose(val fo: Closeable) : ZeroArgFunction() {
        override fun call() : LuaValue {
            fo.close()
            return LuaValue.NONE
        }
    }

    private class FileClassWriteByte(val fos: VDFileOutputStream) : OneArgFunction() {
        override fun call(byte: LuaValue) : LuaValue {
            fos.write(byte.checkint())

            return LuaValue.NONE
        }
    }

    private class FileClassWriteBytes(val fos: VDFileOutputStream) : OneArgFunction() {
        override fun call(byteString: LuaValue) : LuaValue {
            val byteString = byteString.checkIBM437()
            val bytearr = ByteArray(byteString.length, { byteString[it].toByte() })
            fos.write(bytearr)

            return LuaValue.NONE
        }
    }

    private class FileClassPrintText(val fw: VDFileWriter) : OneArgFunction() {
        override fun call(string: LuaValue) : LuaValue {
            val text = string.checkIBM437()
            fw.write(text)
            return LuaValue.NONE
        }
    }

    private class FileClassPrintlnText(val fw: VDFileWriter) : OneArgFunction() {
        override fun call(string: LuaValue) : LuaValue {
            val text = string.checkIBM437() + "\n"
            fw.write(text)
            return LuaValue.NONE
        }
    }

    private class FileClassFlush(val fo: Flushable) : ZeroArgFunction() {
        override fun call() : LuaValue {
            fo.flush()
            return LuaValue.NONE
        }
    }

    private class FileClassReadByte(val fis: ByteArrayInputStream) : ZeroArgFunction() {
        override fun call() : LuaValue {
            val readByte = fis.read()
            return if (readByte == -1) LuaValue.NIL else LuaValue.valueOf(readByte)
        }
    }

    private class FileClassReadAllBytes(val file: EntryFile) : ZeroArgFunction() {
        override fun call() : LuaValue {
            return LuaValue.valueOf(String(file.bytes, sysCharset))
        }
    }

    private class FileClassReadAll(val file: EntryFile) : ZeroArgFunction() {
        override fun call() : LuaValue {
            return LuaValue.valueOf(String(file.bytes, sysCharset))
        }
    }

    /** returns NO line separator! */
    private class FileClassReadLine(fr: Reader) : ZeroArgFunction() {
        val scanner = Scanner(fr.readText()) // no closing; keep the scanner status persistent

        override fun call() : LuaValue {
            return if (scanner.hasNextLine()) LuaValue.valueOf(scanner.nextLine())
                   else LuaValue.NIL
        }
    }
}

/**
 * drops appended NULs and return resulting ByteArray as String
 */
private fun ByteArray.toCanonicalString(): String {
    var lastIndexOfRealStr = 0
    for (i in this.lastIndex downTo 0) {
        if (this[i] != 0.toByte()) {
            lastIndexOfRealStr = i
            break
        }
    }
    return String(this.sliceArray(0..lastIndexOfRealStr))
}
