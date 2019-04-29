package net.torvald.util

import net.torvald.terrarum.lock
import java.util.concurrent.locks.ReentrantLock

/**
 * The modification of the arraylist that its element is always sorted.
 *
 * Created by minjaesong on 2019-03-12.
 */
class SortedArrayList<T: Comparable<T>>(initialSize: Int = 10) {

    private val arrayList = ArrayList<T>(initialSize)

    /**
     */
    fun add(elem: T) {
        // don't append-at-tail-and-sort; just insert at right index
        // this is a modified binary search to search the right "spot" where the insert elem fits
        ReentrantLock().lock {
            var low = 0
            var high = arrayList.size

            while (low < high) {
                val mid = (low + high).ushr(1)

                if (arrayList[mid] > elem)
                    high = mid
                else
                    low = mid + 1
            }

            arrayList.add(low, elem)
        }
    }

    val size: Int
        get() = arrayList.size

    fun removeAt(index: Int) = arrayList.removeAt(index)
    fun remove(element: T) = arrayList.remove(element)
    fun removeLast() = arrayList.removeAt(arrayList.size - 1)

    operator fun get(index: Int) = arrayList[index]
    fun getOrNull(index: Int?) = if (index == null) null else get(index)
    /**
     * Searches for the element. Null if the element was not found
     */
    fun contains(element: T): Boolean {
        // code from collections/Collections.kt
        var low = 0
        var high = this.size - 1

        while (low <= high) {
            val mid = (low + high).ushr(1) // safe from overflows

            val midVal = get(mid)

            if (element > midVal)
                low = mid + 1
            else if (element < midVal)
                high = mid - 1
            else
                return true // key found
        }
        return false // key not found
    }

    /** Searches the element using given predicate instead of the element itself. Returns index in the array where desired
     * element is stored.
     * (e.g. search the Actor by its ID rather than the actor instance) */
    fun <R: Comparable<R>> searchForIndex(key: R, predicate: (T) -> R): Int? {
        var low = 0
        var high = this.size - 1

        while (low <= high) {
            val mid = (low + high).ushr(1) // safe from overflows

            val midVal = predicate(get(mid))

            if (key > midVal)
                low = mid + 1
            else if (key < midVal)
                high = mid - 1
            else
                return mid // key found
        }
        return null // key not found
    }

    /** Searches the element using given predicate instead of the element itself. Returns the element desired.
     * (e.g. search the Actor by its ID rather than the actor instance) */
    fun <R: Comparable<R>> searchFor(key: R, predicate: (T) -> R): T? = getOrNull(searchForIndex(key, predicate))

    fun iterator() = arrayList.iterator()
    fun forEach(action: (T) -> Unit) = arrayList.forEach(action)
    fun forEachIndexed(action: (Int, T) -> Unit) = arrayList.forEachIndexed(action)


    fun <R> map(transformation: (T) -> R) = arrayList.map(transformation)

    fun <R> filter(function: (T) -> Boolean): List<R> {
        val retList = ArrayList<R>() // sorted-ness is preserved
        this.arrayList.forEach { if (function(it)) retList.add(it as R) }
        return retList
    }

    /**
     * Select one unsorted element from the array and put it onto the sorted spot.
     *
     * The list must be fully sorted except for that one "renegade", otherwise the operation is undefined behaviour.
     */
    private fun sortThisRenegade(index: Int) {
        if (
                (index == arrayList.lastIndex && arrayList[index - 1] <= arrayList[index]) ||
                (index == 0 && arrayList[index] <= arrayList[index + 1]) ||
                (arrayList[index - 1] <= arrayList[index] && arrayList[index] <= arrayList[index + 1])
        ) return

        // modified binary search
        ReentrantLock().lock {
            val renegade = arrayList.removeAt(index)

            var low = 0
            var high = arrayList.size

            while (low < high) {
                val mid = (low + high).ushr(1)

                if (arrayList[mid] > renegade)
                    high = mid
                else
                    low = mid + 1
            }

            arrayList.add(low, renegade)
        }
    }

    /**
     * Does NOT create copies!
     */
    fun toArrayList() = arrayList
}