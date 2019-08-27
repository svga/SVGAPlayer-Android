package com.opensource.svgaplayer.threadpool

import com.opensource.svgaplayer.log.SLog

/**
 * Created by guojunjie on 2019-08-26.
 *  A strategy for handling unexpected and uncaught throwables thrown by futures run on the pool.
 */
enum class UncaughtThrowableStrategy {
    /**
     * Silently catches and ignores the uncaught throwables.
     */
    IGNORE,
    /**
     * Logs the uncaught throwables using [.TAG] and [SLog].
     */
    LOG {
        override fun handle(t: Throwable) {
            SLog.e("UncaughtThrowableStrategy", "Request threw uncaught throwable", t)
        }
    },
    /**
     * Rethrows the uncaught throwables to crash the app.
     */
    THROW {
        override fun handle(t: Throwable) {
            super.handle(t)
            throw RuntimeException(t)
        }
    };

    open fun handle(t: Throwable) {
        // Ignore.
    }
}
