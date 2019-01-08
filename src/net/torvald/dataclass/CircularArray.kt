package net.torvald.dataclass


/**
 * buffer[head] contains the most recent item, whereas buffer[tail] contains the oldest one.
 *
 * Notes for particle storage:
 *      Particles does not need to be removed, just let it overwrite as their operation is rather
 *      lightweight. So, just flagDespawn = true if it need to be "deleted" so that it won't update
 *      anymore.
 *
 * Created by minjaesong on 2017-01-22.
 */
class CircularArray<T>(val size: Int) {

    val buffer: Array<T> = arrayOfNulls<Any>(size) as Array<T>
    var tail: Int = 0; private set
    var head: Int = -1; private set

    private var unreliableAddCount = 0

    val lastIndex = size - 1

    /** elemCount == size means it has the exact elements and no more.
     * If elemCount is greater by 1 against the size, it means it started to circle, elemCount won't increment at this point. */
    val elemCount: Int
        get() = unreliableAddCount

    fun add(item: T) {
        if (unreliableAddCount <= size) unreliableAddCount += 1

        head = (head + 1) % size
        if (unreliableAddCount > size) {
            tail = (tail + 1) % size
        }

        buffer[head] = item // overwrites oldest item when eligible


        //println("$this $unreliableAddCount")
    }

    /**
     * Iterates the array with oldest element first.
     */
    fun forEach(action: (T) -> Unit) {
        // has slightly better iteration performance than lambda
        if (unreliableAddCount <= size) {
            for (i in 0..head)
                action(buffer[i])
        }
        else {
            for (i in 0..size - 1)
                action(buffer[(i + tail) % size])
        }
    }

    fun <R> fold(initial: R, operation: (R, T) -> R): R {
        var accumulator = initial
        //for (element in buffer) accumulator = operation(accumulator, element)
        if (unreliableAddCount <= size) {
            for (i in 0..head)
                accumulator = operation(accumulator, buffer[i])
        }
        else {
            for (i in 0..size - 1)
                accumulator = operation(accumulator, buffer[(i + tail) % size])
        }

        return accumulator
    }



    override fun toString(): String {
        return "CircularArray(size=" + buffer.size + ", head=" + head + ", tail=" + tail + ")"
    }
}