package com.qiniu.shortvideo.app.utils;

import android.os.AsyncTask;

import java.util.concurrent.Executor;

/**
 * 线程池工具类
 */
public class ThreadPoolUtils {

    private static Executor threadPool;

    static {
        threadPool = AsyncTask.THREAD_POOL_EXECUTOR;
    }

    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    public static Executor getExecutor(){
        return threadPool;
    }
}
