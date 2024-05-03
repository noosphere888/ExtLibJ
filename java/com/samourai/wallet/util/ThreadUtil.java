package com.samourai.wallet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ThreadUtil {
    private static final Logger log = LoggerFactory.getLogger(ThreadUtil.class);
    private static ThreadUtil instance;

    private ExecutorService executorService;

    public static ThreadUtil getInstance() {
        if (instance == null) {
            instance = new ThreadUtil();
        }
        return instance;
    }

    protected ThreadUtil() {
        this.executorService = computeExecutorService();
    }

    protected ExecutorService computeExecutorService() {
        return Executors.newFixedThreadPool(5,
                r -> {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                });
    }

    // used by Sparrow
    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public <T> Future<T> runAsync(Callable<T> callable) {
        return executorService.submit(callable);
    }
}
