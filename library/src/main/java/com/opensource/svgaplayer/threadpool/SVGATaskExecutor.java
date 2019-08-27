package com.opensource.svgaplayer.threadpool;

import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.opensource.svgaplayer.log.SLog;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class SVGATaskExecutor {
    private static final String TAG = "SVGATaskExecutor";
    public static final int THREAD_PRIORITY_BACKGROUND = FifoPriorityThreadPoolExecutor.THREAD_PRIORITY_BACKGROUND;
    public static final int THREAD_PRIORITY_HIGH = FifoPriorityThreadPoolExecutor.THREAD_PRIORITY_HIGH;

    private static final int CORE_SIZE = RuntimeCompat.INSTANCE.calculateBestThreadCount();
    /**
     * 普通线程池
     */
    private static ThreadPoolExecutor sThreadPool =
            new FifoPriorityThreadPoolExecutor(
                    CORE_SIZE + CORE_SIZE / 2,
                    CORE_SIZE + CORE_SIZE / 2,
                    UncaughtThrowableStrategy.LOG,
                    "SVGATask-normal-"
            );

    public static void setThreadPoolExecutor(ThreadPoolExecutor executor) {
        sThreadPool = executor;
    }

    /**
     *
     */
    private static volatile HandlEx sMainThreadHandler;

    private final static HashMap<Runnable, Runnable> sDelayExecutorRunnableCache = new HashMap<Runnable, Runnable>();
    private final static HashMap<Runnable, Runnable> sExecutorRunnableCache = new HashMap<Runnable, Runnable>();


    /**
     * 将task放入线程池执行
     *
     * @param task
     */
    public static void execute(final Runnable task) {
        execute(task, 0, THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * 将task放入线程池执行
     *
     * @param task
     * @param delayMillis 延迟执行时间 单位毫秒；0，不延迟
     */
    public static void execute(final Runnable task, long delayMillis) {
        execute(task, null, delayMillis, THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * 将task放入线程池执行
     *
     * @param task
     * @param delayMillis 延迟执行时间 单位毫秒；0，不延迟
     * @param priority    线程优先级, 见常量：
     *                    THREAD_PRIORITY_LOW（低）、
     *                    THREAD_PRIORITY_BACKGROUND（默认）、
     *                    THREAD_PRIORITY_NORMAL（介于BACKGROUND和HIGH之间）、
     *                    THREAD_PRIORITY_HIGH
     */
    public static void execute(final Runnable task, long delayMillis, int priority) {
        execute(task, null, delayMillis, priority);
    }

    /**
     * 将task放入线程池执行
     *
     * @param task
     * @param callbackInMainThread 执行完task后在主线程回调Runnable；
     * @param delayMillis          延迟执行时间 单位毫秒；0，不延迟
     */
    public static void execute(final Runnable task, final Runnable callbackInMainThread, long delayMillis) {
        execute(task, callbackInMainThread, delayMillis, THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * 将task放入线程池执行
     *
     * @param task
     * @param callbackInMainThread 执行完task后在主线程回调Runnable；
     */
    public static void execute(final Runnable task, final Runnable callbackInMainThread) {
        execute(task, callbackInMainThread, 0);
    }

    /**
     * 将task放入线程池执行
     *
     * @param task
     * @param callbackInMainThread 执行完task后在主线程回调Runnable；
     * @param delayMillis          延迟执行时间 单位毫秒；0，不延迟
     * @param thePriority          线程优先级,
     *                            见常量：THREAD_PRIORITY_LOW（低）、
     *                             THREAD_PRIORITY_BACKGROUND（默认）、
     *                             THREAD_PRIORITY_NORMAL（介于BACKGROUND和HIGH之间）、
     *                             THREAD_PRIORITY_HIGH
     */

    public static void execute(
            final Runnable task, final Runnable callbackInMainThread, long delayMillis, int thePriority
    ) {
        if (task == null) {
            return;
        }

        if (delayMillis < 0) {
            delayMillis = 0;
        }


        if (thePriority < 0) {
            thePriority = THREAD_PRIORITY_BACKGROUND;
        } else if (thePriority > THREAD_PRIORITY_HIGH) {
            thePriority = THREAD_PRIORITY_HIGH;
        }

        final int priority = thePriority;
        ExecutorRunnable theExecutorRunnable = ExecutorRunnable.obtain();
        if (theExecutorRunnable == null) {
            theExecutorRunnable = new ExecutorRunnable() {
                @Override
                public void run() {
                    try {
                        Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);

                        synchronized (sExecutorRunnableCache) {
                            sExecutorRunnableCache.remove(this.task);
                        }

                        this.task.run();

                        if (callBack != null) {
                            getMainThreadHandler().post(callBack);
                        }
                    } catch (final Throwable t) {
                        synchronized (sExecutorRunnableCache) {
                            sExecutorRunnableCache.remove(this.task);
                        }

                        SLog.INSTANCE.e("SVGATaskExecutor execute error one:", "", t);
                    } finally {
                        if (priority != THREAD_PRIORITY_BACKGROUND) {
                            try {
                                Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
                            } catch (final Throwable t) {
                                Log.e("SVGATaskExecutor", " error ignore: " + t.getMessage());
                            }
                        }

                        this.recycle();
                    }
                }

            };
        }

        final ExecutorRunnable executorRunnable = theExecutorRunnable;
        executorRunnable.task = task;
        executorRunnable.callBack = callbackInMainThread;
        executorRunnable.priority = priority;

        if (delayMillis > 0) {
            Runnable delayDelayExecutorRunnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (sDelayExecutorRunnableCache) {
                        sDelayExecutorRunnableCache.remove(task);
                    }

                    execute(executorRunnable);
                }
            };

            synchronized (sDelayExecutorRunnableCache) {
                sDelayExecutorRunnableCache.put(task, delayDelayExecutorRunnable);
            }

            postToMainThread(delayDelayExecutorRunnable, delayMillis);
        } else {
            execute(executorRunnable);
        }
    }

    private static void execute(ExecutorRunnable task) {
        if (task == null || task.task == null) {
            return;
        }

        try {
            if (!sThreadPool.isShutdown()) {
                synchronized (sExecutorRunnableCache) {
                    sExecutorRunnableCache.put(task.task, task);
                }
                sThreadPool.execute(task);
            }
        } catch (final Throwable e) {
            SLog.INSTANCE.e("SVGATaskExecutor execute error two:", "", e);
        }
    }

    /**
     * 将task移除，包括子线程和主线程
     *
     * @param task
     * @param
     */
    public static void removeTask(final Runnable task) {
        if (task == null) {
            return;
        }

        Runnable delayExecutorRunnable;
        synchronized (sDelayExecutorRunnableCache) {
            delayExecutorRunnable = sDelayExecutorRunnableCache.remove(task);
        }

        if (delayExecutorRunnable != null) {
            getMainThreadHandler().removeCallbacks(delayExecutorRunnable);
        }

        Runnable executorRunnable;
        synchronized (sExecutorRunnableCache) {
            executorRunnable = sExecutorRunnableCache.remove(task);
        }

        removeRunnableFromMainThread(task);

        if (executorRunnable != null) {
            try {
                if (sThreadPool != null) {
                    //remove task from mThreadPool
                    sThreadPool.remove(executorRunnable);
                }
            } catch (final Throwable e) {
                Log.e("SVGATaskExecutor", " error ignore: " + e.getMessage());
            }
        }

    }

    /**
     * @param task
     * @note post到主线程执行
     */
    public static void postToMainThread(final Runnable task) {
        postToMainThread(task, 0);
    }

    /**
     * @param task
     * @param delayMillis 延迟执行时间 单位毫秒；0，不延迟
     * @note post到主线程执行
     */
    public static void postToMainThread(final Runnable task, long delayMillis) {
        if (task == null) {
            return;
        }

        getMainThreadHandler().postDelayed(task, delayMillis);
    }


    private static Thread sMainThread = null;

    public static boolean isMainThread() {
        Thread current = Thread.currentThread();
        if (sMainThread == null) {
            Looper looper = Looper.getMainLooper();
            if (looper != null) {
                sMainThread = looper.getThread();
            }
        }

        return sMainThread == current;
    }

    public static synchronized void destroy() {
        if (sThreadPool != null) {
            try {
                sThreadPool.shutdown();
            } catch (Throwable t) {
                Log.e("SVGATaskExecutor", "Empty Catch on destroy", t);
            }

            sThreadPool = null;
        }
    }

    public static abstract class RunnableEx implements Runnable {
        private Object mArg;

        public void setArg(Object arg) {
            mArg = arg;
        }

        public Object getArg() {
            return mArg;
        }
    }

    private static HandlEx getMainThreadHandler() {
        if (sMainThreadHandler == null) {
            sMainThreadHandler = new HandlEx("MainThreadHandler + 8", Looper.getMainLooper(), null);
        }

        return sMainThreadHandler;
    }

    private static abstract class ExecutorRunnable implements Runnable, Prioritized, Comparable<Prioritized> {
        Runnable task;
        Runnable callBack;
        int priority;
        //io normal
        private ExecutorRunnable mNext;

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public int hashCode() {
            return priority;
        }

        @Override
        public int compareTo(Prioritized loadTask) {
            return loadTask.getPriority() - priority;
        }

        private void reset() {
            task = null;
            callBack = null;
            priority = THREAD_PRIORITY_BACKGROUND;
        }

        private static int sPoolSize = 0;
        private static final int MAX_POOL_SIZE = 100;
        private static final Object sPoolSync = new Object();
        private static ExecutorRunnable sPool;

        public static ExecutorRunnable obtain() {
            synchronized (sPoolSync) {
                if (sPool != null) {
                    ExecutorRunnable m = sPool;
                    sPool = m.mNext;
                    m.mNext = null;
                    sPoolSize--;
                    return m;
                }
            }
            return null;
        }

        void recycle() {
            reset();
            synchronized (sPoolSync) {
                if (sPoolSize < MAX_POOL_SIZE) {
                    mNext = sPool;
                    sPool = this;
                    sPoolSize++;
                }
            }
        }
    }

    /**
     * 移除主线程task
     * @param task
     */
    public static void removeRunnableFromMainThread(final Runnable task) {
        if (task == null) {
            return;
        }

        getMainThreadHandler().removeCallbacks(task);
    }
}
