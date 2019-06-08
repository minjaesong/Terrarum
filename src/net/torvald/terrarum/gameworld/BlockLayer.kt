package net.torvald.terrarum.gameworld

import com.badlogic.gdx.utils.Disposable
import net.torvald.terrarum.AppLoader.printdbg
import sun.misc.Unsafe

/**
 * Original version Created by minjaesong on 2016-01-17.
 * Unsafe version Created by minjaesong on 2019-06-08.
 *
 * Note to self: refrain from using shorts--just do away with two bytes: different system have different endianness
 */
open class BlockLayer(val width: Int, val height: Int) : Disposable {

    private val unsafe: Unsafe
    init {
        val unsafeConstructor = Unsafe::class.java.getDeclaredConstructor()
        unsafeConstructor.isAccessible = true
        unsafe = unsafeConstructor.newInstance()
    }
    private var unsafeArrayInitialised = false
    private var unsafeArrayDestroyed = false

    private var layerPtr = unsafe.allocateMemory(width * height * BYTES_PER_BLOCK.toLong())

    /**
     * @param data Byte array representation of the layer, where:
     * - every 2n-th byte is lowermost 8 bits of the tile number
     * - every (2n+1)th byte is uppermost 4 (4096 blocks) or 8 (65536 blocks) bits of the tile number.
     *
     * When 4096-block mode is being used, every (2n+1)th byte is filled in this format:
     * ```
     * (MSB) 0 0 0 0 a b c d (LSB)
     * ```
     *
     * In other words, the valid range for the every (2n+1)th byte is 0..15.
     *
     * TL;DR: LITTLE ENDIAN PLEASE
     */
    constructor(width: Int, height: Int, data: ByteArray) : this(width, height) {
        data.forEachIndexed { index, byte -> unsafe.putByte(layerPtr + index, byte) }
        unsafeArrayInitialised = true
    }

    init {
        if (!unsafeArrayInitialised) {
            unsafe.setMemory(layerPtr, width * height * BYTES_PER_BLOCK.toLong(), 0)
            unsafeArrayInitialised = true
        }
    }

    /**
     * Returns an iterator over blocks of type `Int`.
     *
     * @return an Iterator.
     */
    fun blocksIterator(): Iterator<Int> {
        return object : Iterator<Int> {

            private var iteratorCount = 0

            override fun hasNext(): Boolean {
                return iteratorCount < width * height
            }

            override fun next(): Int {
                val y = iteratorCount / width
                val x = iteratorCount % width
                // advance counter
                iteratorCount += 2

                val offset = 2 * (y * width + x)
                val lsb = unsafe.getByte(layerPtr + offset)
                val msb = unsafe.getByte(layerPtr + offset + 1)

                //return data[y * width + x]
                return lsb.toUint() + msb.toUint().shl(8)
            }
        }
    }

    /**
     * Returns an iterator over stored bytes.
     *
     * @return an Iterator.
     */
    fun bytesIterator(): Iterator<Byte> {
        return object : Iterator<Byte> {

            private var iteratorCount = 0

            override fun hasNext(): Boolean {
                return iteratorCount < width * height
            }

            override fun next(): Byte {
                val y = iteratorCount / width
                val x = iteratorCount % width
                // advance counter
                iteratorCount += 1

                return unsafe.getByte(layerPtr + 1)
            }
        }
    }

    internal fun unsafeGetTile(x: Int, y: Int): Int {
        val offset = BYTES_PER_BLOCK * (y * width + x)
        val lsb = unsafe.getByte(layerPtr + offset)
        val msb = unsafe.getByte(layerPtr + offset + 1)

        return lsb.toUint() + msb.toUint().shl(8)
    }

    internal fun unsafeSetTile(x: Int, y: Int, tile: Int) {
        val offset = BYTES_PER_BLOCK * (y * width + x)

        val lsb = tile.and(0xff).toByte()
        val msb = tile.ushr(8).and(0xff).toByte()

        unsafe.putByte(layerPtr + offset, lsb)
        unsafe.putByte(layerPtr + offset + 1, msb)
    }

    /**
     * @param blockOffset Offset in blocks. BlockOffset of 0x100 is equal to ```layerPtr + 0x200```
     */
    internal fun unsafeSetTile(blockOffset: Long, tile: Int) {
        val offset = 2 * blockOffset

        val lsb = tile.and(0xff).toByte()
        val msb = tile.ushr(8).and(0xff).toByte()

        unsafe.putByte(layerPtr + offset, lsb)
        unsafe.putByte(layerPtr + offset + 1, msb)
    }

    fun isInBound(x: Int, y: Int) = (x >= 0 && y >= 0 && x < width && y < height)

    override fun dispose() {
        if (!unsafeArrayDestroyed) {
            unsafe.freeMemory(layerPtr)
            unsafeArrayDestroyed = true
            printdbg(this, "BlockLayer successfully freed")
        }
    }

    companion object {
        @Transient val BYTES_PER_BLOCK = 2
    }
}

fun Byte.toUint() = java.lang.Byte.toUnsignedInt(this)