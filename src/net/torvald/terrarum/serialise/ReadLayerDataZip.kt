package net.torvald.terrarum.serialise

import net.torvald.terrarum.AppLoader.printdbg
import net.torvald.terrarum.gameitem.ItemID
import net.torvald.terrarum.gameworld.BlockAddress
import net.torvald.terrarum.gameworld.BlockLayer
import net.torvald.terrarum.gameworld.FluidType
import net.torvald.terrarum.modulecomputers.virtualcomputer.tvd.DiskSkimmer.Companion.read
import net.torvald.terrarum.realestate.LandUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.Inflater
import kotlin.collections.HashMap

/**
 * Created by minjaesong on 2016-08-24.
 */
// internal for everything: prevent malicious module from messing up the savedata
internal object ReadLayerDataZip {

    // FIXME TERRAIN DAMAGE UNTESTED

    internal operator fun invoke(file: File): LayerData {
        val inputStream = MarkableFileInputStream(FileInputStream(file))


        val magicBytes = ByteArray(4)


        //////////////////
        // FILE READING //
        //////////////////


        // read header first
        inputStream.read(magicBytes)
        if (!Arrays.equals(magicBytes, WriteLayerDataZip.MAGIC)) {
            throw IllegalArgumentException("File not a Layer Data")
        }

        val versionNumber = inputStream.read(1)[0].toUint()
        val layerCount = inputStream.read(1)[0].toUint()
        val payloadCount = inputStream.read(1)[0].toUint()
        val compression = inputStream.read(1)[0].toUint()
        val generatorVer = inputStream.read(2).toULittleShort()
        val width = inputStream.read(4).toLittleInt()
        val height = inputStream.read(4).toLittleInt()
        val spawnAddress = inputStream.read(6).toLittleInt48()

        if (compression != 1) throw IllegalArgumentException("Input file is not compressed as DEFLATE; it's using algorithm $compression")

        printdbg(this, "Version number: $versionNumber")
        printdbg(this, "Layers count: $layerCount")
        printdbg(this, "Payloads count: $payloadCount")
        printdbg(this, "Compression: $compression")
        printdbg(this, "World generator version: $generatorVer")
        printdbg(this, "Dimension: ${width}x$height")

        // read payloads

        val payloads = PayloadUtil.readAll(inputStream)
        /*val pldBuffer4 = ByteArray(4)
        val pldBuffer6 = ByteArray(6)
        val pldBuffer8 = ByteArray(8)

        val payloads = HashMap<String, TEMzPayload>()


        // TODO please test the read; write has been fixed up

        for (pldCnt in 0 until payloadCount) {
            inputStream.read(pldBuffer4)

            // check payload header
            if (!pldBuffer4.contentEquals(WriteLayerDataZip.PAYLOAD_HEADER))
                throw InternalError("Payload $pldCnt not found -- expected ${WriteLayerDataZip.PAYLOAD_HEADER.toByteString()}, got ${pldBuffer4.toByteString()}")

            // get payload's name
            inputStream.read(pldBuffer4)
            val payloadName = pldBuffer4.toString(Charset.forName("US-ASCII"))

            printdbg(this, "Payload $pldCnt name: $payloadName") // maybe maybe related with buffer things?

            // get uncompressed size
            inputStream.read(pldBuffer6)
            val uncompressedSize = pldBuffer6.toLittleInt48()

            // get deflated size
            inputStream.mark(2147483647) // FIXME deflated stream cannot be larger than 2 GB
            // creep forward until we hit the PAYLOAD_FOOTER
            var deflatedSize: Int = 0 // FIXME deflated stream cannot be larger than 2 GB
            // loop init
            inputStream.read(pldBuffer8)
            // loop main
            while (!pldBuffer8.contentEquals(WriteLayerDataZip.PAYLOAD_FOOTER)) {
                val aByte = inputStream.read(); deflatedSize += 1
                if (aByte == -1) throw InternalError("Unexpected end-of-file at payload $pldCnt")
                pldBuffer8.shiftLeftBy(1, aByte.toByte())
            }

            // at this point, we should have correct size of deflated bytestream

            printdbg(this, "Payload $pldCnt compressed size: $deflatedSize")

            val deflatedBytes = ByteArray(deflatedSize) // FIXME deflated stream cannot be larger than 2 GB
            inputStream.reset() // go back to marked spot
            inputStream.read(deflatedBytes)

            // PRO Debug tip: every deflated bytes must begin with 0x789C or 0x78DA
            // Thus, \0pLd + [10] must be either of these.

            // put constructed payload into a container
            payloads.put(payloadName, TEMzPayload(uncompressedSize, deflatedBytes))

            // skip over to be aligned with the next payload
            inputStream.skip(8)
        }


        // test for EOF
        inputStream.read(pldBuffer8)
        if (!pldBuffer8.contentEquals(WriteLayerDataZip.FILE_FOOTER))
            throw InternalError("Expected end-of-file, got not-so-end-of-file")*/


        //////////////////////
        // END OF FILE READ //
        //////////////////////

        val worldSize = width.toLong() * height

        val payloadBytes = HashMap<String, ByteArray>()

        payloads.forEach { t, u ->
            val inflatedFile = ByteArray(u.uncompressedSize.toInt()) // FIXME deflated stream cannot be larger than 2 GB
            val inflater = Inflater()
            inflater.setInput(u.bytes, 0, u.bytes.size)
            val uncompLen = inflater.inflate(inflatedFile)

            // just in case
            if (uncompLen.toLong() != u.uncompressedSize)
                throw InternalError("Payload $t DEFLATE size mismatch -- expected ${u.uncompressedSize}, got $uncompLen")

            payloadBytes[t] = inflatedFile
        }

        val spawnPoint = LandUtil.resolveBlockAddr(width, spawnAddress)

        val terrainDamages = HashMap<BlockAddress, Float>()
        val wallDamages = HashMap<BlockAddress, Float>()
        val fluidTypes = HashMap<BlockAddress, FluidType>()
        val fluidFills = HashMap<BlockAddress, Float>()
        val tileNumberToNameMap = HashMap<Int, ItemID>()

        // parse terrain damages
        for (c in payloadBytes["TdMG"]!!.indices step 10) {
            val bytes = payloadBytes["TdMG"]!!

            val tileAddr = bytes.sliceArray(c..c+5)
            val value = bytes.sliceArray(c+6..c+9)

            terrainDamages[tileAddr.toLittleInt48()] = value.toLittleFloat()
        }


        // parse wall damages
        for (c in payloadBytes["WdMG"]!!.indices step 10) {
            val bytes = payloadBytes["WdMG"]!!

            val tileAddr = bytes.sliceArray(c..c+5)
            val value = bytes.sliceArray(c+6..c+9)

            wallDamages[tileAddr.toLittleInt48()] = value.toLittleFloat()
        }


        // TODO parse fluid(Types|Fills)

        
        return LayerData(
                BlockLayer(width, height, payloadBytes["WALL"]!!),
                BlockLayer(width, height, payloadBytes["TERR"]!!),

                spawnPoint.first, spawnPoint.second,

                wallDamages, terrainDamages, fluidTypes, fluidFills, tileNumberToNameMap
        )
    }

    /**
     * Immediately deployable, a part of the gameworld
     */
    internal data class LayerData(
            val layerWall: BlockLayer,
            val layerTerrain: BlockLayer,
            //val layerThermal: MapLayerHalfFloat, // in Kelvins
            //val layerAirPressure: MapLayerHalfFloat, // (milibar - 1000)

            val spawnX: Int,
            val spawnY: Int,

            //val wirings: HashMap<BlockAddress, SortedArrayList<GameWorld.WiringNode>>,
            val wallDamages: HashMap<BlockAddress, Float>,
            val terrainDamages: HashMap<BlockAddress, Float>,
            val fluidTypes: HashMap<BlockAddress, FluidType>,
            val fluidFills: HashMap<BlockAddress, Float>,
            val tileNumberToNameMap: HashMap<Int, ItemID>
    )

	internal fun InputStream.readRelative(b: ByteArray, off: Int, len: Int): Int {
        if (b == null) {
            throw NullPointerException()
        } else if (off < 0 || len < 0 || len > b.size) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return 0
        }

        var c = read()
        if (c == -1) {
            return -1
        }
        b[0] = c.toByte()

        var i = 1
        try {
            while (i < len) {
                c = read()
                if (c == -1) {
                    break
                }
                b[i] = c.toByte()
                i++
            }
        } catch (ee: IOException) {
        }

        return i
    }
}
