package net.torvald

import sun.misc.Unsafe

/**
 * Created by minjaesong on 2019-06-21.
 */

object UnsafeHelper {
    internal val unsafe: Unsafe

    init {
        val unsafeConstructor = Unsafe::class.java.getDeclaredConstructor()
        unsafeConstructor.isAccessible = true
        unsafe = unsafeConstructor.newInstance()
    }


    fun allocate(size: Long): UnsafePtr {
        val ptr = unsafe.allocateMemory(size)
        return UnsafePtr(ptr, size)
    }
}

/**
 * To allocate a memory, use UnsafeHelper.allocate(long)
 */
class UnsafePtr(val ptr: Long, val allocSize: Long) {
    var destroyed = false
        private set

    fun destroy() {
        if (!destroyed) {
            UnsafeHelper.unsafe.freeMemory(ptr)
            destroyed = true
        }
    }

    private inline fun checkNullPtr(index: Long) {
        if (destroyed) throw NullPointerException()

        // OOB Check: debugging purposes only -- comment out for the production
        //if (index !in 0 until allocSize) throw NullPointerException("Out of bounds: $index; alloc size: $allocSize")
    }

    operator fun get(index: Long): Byte {
        checkNullPtr(index)
        return UnsafeHelper.unsafe.getByte(ptr + index)
    }

    fun getFloat(index: Long): Float {
        checkNullPtr(index)
        return UnsafeHelper.unsafe.getFloat(ptr + index)
    }

    operator fun set(index: Long, value: Byte) {
        checkNullPtr(index)
        UnsafeHelper.unsafe.putByte(ptr + index, value)
    }

    fun setFloat(index: Long, value: Float) {
        checkNullPtr(index)
        UnsafeHelper.unsafe.putFloat(ptr + index, value)
    }

}