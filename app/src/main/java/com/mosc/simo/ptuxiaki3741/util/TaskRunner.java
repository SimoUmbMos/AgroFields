package com.mosc.simo.ptuxiaki3741.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRunner {
    private static final String TAG = "TaskRunner Class";
    private final Executor executor;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R result);
    }

    public TaskRunner(){
        this(true);
    }
    public TaskRunner(boolean singleThread){
        if(singleThread){
            executor = Executors.newSingleThreadExecutor();
        }else{
            executor = Executors.newCachedThreadPool();
        }
    }
    public TaskRunner(int threadNumber){
        executor = Executors.newFixedThreadPool(threadNumber);
    }

    public <R> void executeAsync(Callable<R> callable, Callback<R> callback) {
        executor.execute(() -> {
            try{
                final R result = callable.call();
                handler.post(() -> callback.onComplete(result));
            }catch (Exception e){
                Log.e(TAG, "executeAsync: ", e );
            }
        });
    }
}
