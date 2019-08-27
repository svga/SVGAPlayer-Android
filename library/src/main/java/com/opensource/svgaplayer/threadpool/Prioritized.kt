package com.opensource.svgaplayer.threadpool

/**
 * Created by lulong on 2017/7/28.
 * Email:lulong@yy.com
 */

internal interface Prioritized {
    /**
     * Returns the priority of this task.
     */
    val priority: Int
}
