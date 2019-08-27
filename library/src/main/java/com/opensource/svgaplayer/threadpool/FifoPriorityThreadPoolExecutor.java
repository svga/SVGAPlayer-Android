package com.opensource.svgaplayer.threadpool;

import android.os.Process;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lulong on 2017/7/28.
 * Email:lulong@yy.com
 */
public class FifoPriorityThreadPoolExecutor extends ThreadPoolExecutor {
    static final int THREAD_PRIORITY_LOW = (Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LOWEST) / 2;
    static final int THREAD_PRIORITY_BACKGROUND = Process.THREAD_PRIORITY_BACKGROUND;
    static final int THREAD_WORK_PRIORITY = (Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_DEFAULT) / 2;
    static final int THREAD_PRIORITY_NORMAL =
            (Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_DEFAULT) / 2;
    static final int THREAD_PRIORITY_HIGH = Process.THREAD_PRIORITY_DEFAULT;

    private static final String TAG = "YYThreadPoolExecutor";
    private final AtomicInteger ordering = new AtomicInteger();
    private final UncaughtThrowableStrategy uncaughtThrowableStrategy;

    /**
     * Constructor to build a fixed thread pool with the given pool size using
     * {@link DefaultThreadFactory}.
     *
     * @param poolSize The number of threads.
     */
    public FifoPriorityThreadPoolExecutor(int poolSize, String mThreadPrefix) {
        this(poolSize, UncaughtThrowableStrategy.LOG, mThreadPrefix);
    }

    /**
     * Constructor to build a fixed thread pool with the given pool size using
     * {@link DefaultThreadFactory}.
     *
     * @param poolSize                  The number of threads.
     * @param uncaughtThrowableStrategy Dictates how the pool should handle uncaught and unexpected throwables
     *                                  thrown by Futures run by the pool.
     */
    public FifoPriorityThreadPoolExecutor(
            int poolSize, UncaughtThrowableStrategy uncaughtThrowableStrategy, String mThreadPrefix
    ) {
        this(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, new DefaultThreadFactory(mThreadPrefix),
                uncaughtThrowableStrategy);
    }

    public FifoPriorityThreadPoolExecutor(
            int poolSize, int maxPoolSize, UncaughtThrowableStrategy uncaughtThrowableStrategy, String mThreadPrefix
    ) {
        this(poolSize, poolSize, 30, TimeUnit.SECONDS, new DefaultThreadFactory(mThreadPrefix),
                uncaughtThrowableStrategy);
    }

    public FifoPriorityThreadPoolExecutor(
            int corePoolSize, int maximumPoolSize, long keepAlive, TimeUnit timeUnit,
            ThreadFactory threadFactory, UncaughtThrowableStrategy uncaughtThrowableStrategy
    ) {
        super(corePoolSize, maximumPoolSize, keepAlive, timeUnit, new PriorityBlockingQueue<Runnable>(), threadFactory);
        this.uncaughtThrowableStrategy = uncaughtThrowableStrategy;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new LoadTask<T>(runnable, value, ordering.getAndIncrement());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            Future<?> future = (Future<?>) r;
            if (future.isDone() && !future.isCancelled()) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    uncaughtThrowableStrategy.handle(e);
                } catch (ExecutionException e) {
                    uncaughtThrowableStrategy.handle(e);
                }
            }
        }
    }

    /**
     * A {@link ThreadFactory} that builds threads with priority
     * {@link Process#THREAD_PRIORITY_BACKGROUND}.
     */
    public static class DefaultThreadFactory implements ThreadFactory {

        private String mThreadPrefix;

        public DefaultThreadFactory(String mThreadPrefix) {
            this.mThreadPrefix = mThreadPrefix;
        }

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            final Thread result = new Thread(runnable, mThreadPrefix + threadNumber.getAndIncrement()) {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    super.run();
                }
            };
            if (result.isDaemon()) {
                result.setDaemon(false);
            }
            return result;
        }
    }

    // Visible for testing.
    static class LoadTask<T> extends FutureTask<T> implements Prioritized, Comparable<Prioritized> {
        private final int priority;
        private final int order;

        LoadTask(Runnable runnable, T result, int order) {
            super(runnable, result);
            if (runnable instanceof Prioritized) {
                priority = ((Prioritized) runnable).getPriority();
            } else {
                priority = THREAD_PRIORITY_BACKGROUND;
            }

            this.order = order;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {
            if (o instanceof LoadTask) {
                LoadTask<Object> other = (LoadTask<Object>) o;
                return order == other.order && priority == other.priority;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int result = priority;
            result = 31 * result + order;
            return result;
        }

        @Override
        public int compareTo(Prioritized loadTask) {
            int result = loadTask.getPriority() - priority;
            if (result == 0 && loadTask instanceof LoadTask) {
                result = order - ((LoadTask) loadTask).order;
            }

            return result;
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }
}