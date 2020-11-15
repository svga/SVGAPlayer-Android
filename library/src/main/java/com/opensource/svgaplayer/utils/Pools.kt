package com.opensource.svgaplayer.utils

/**
 * Helper class for creating pools of objects. An example use looks like this:
 * <pre>
 * public class MyPooledClass {
 *
 *     private static final SynchronizedPool<MyPooledClass> sPool =
 *             new SynchronizedPool<MyPooledClass>(10);
 *
 *     public static MyPooledClass obtain() {
 *         MyPooledClass instance = sPool.acquire();
 *         return (instance != null) ? instance : new MyPooledClass();
 *     }
 *
 *     public void recycle() {
 *          // Clear state if needed.
 *          sPool.release(this);
 *     }
 *
 *     . . .
 * }
 * </pre>
 *
 */
class Pools private constructor() {

    /**
     * Interface for managing a pool of objects.
     *
     * @param <T> The pooled type.
     */
    interface Pool<T> {
        /**
         * @return An instance from the pool if such, null otherwise.
         */
        fun acquire(): T?

        /**
         * Release an instance to the pool.
         *
         * @param instance The instance to release.
         * @return Whether the instance was put in the pool.
         *
         * @throws IllegalStateException If the instance is already in the pool.
         */
        fun release(instance: T): Boolean
    }

    /**
     * Simple (non-synchronized) pool of objects.
     *
     * @param maxPoolSize The max pool size.
     *
     * @throws IllegalArgumentException If the max pool size is less than zero.
     *
     * @param <T> The pooled type.
     */
    open class SimplePool<T>(maxPoolSize: Int) : Pool<T> {
        private val mPool: Array<Any?>
        private var mPoolSize = 0

        init {
            require(maxPoolSize > 0) { "The max pool size must be > 0" }
            mPool = arrayOfNulls(maxPoolSize)
        }

        @Suppress("UNCHECKED_CAST")
        override fun acquire(): T? {
            if (mPoolSize > 0) {
                val lastPooledIndex = mPoolSize - 1
                val instance = mPool[lastPooledIndex] as T?
                mPool[lastPooledIndex] = null
                mPoolSize--
                return instance
            }
            return null
        }

        override fun release(instance: T): Boolean {
            check(!isInPool(instance)) { "Already in the pool!" }
            if (mPoolSize < mPool.size) {
                mPool[mPoolSize] = instance
                mPoolSize++
                return true
            }
            return false
        }

        private fun isInPool(instance: T): Boolean {
            for (i in 0 until mPoolSize) {
                if (mPool[i] === instance) {
                    return true
                }
            }
            return false
        }

    }


}