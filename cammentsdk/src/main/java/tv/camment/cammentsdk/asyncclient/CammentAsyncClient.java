package tv.camment.cammentsdk.asyncclient;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * An async client executes actions in the background and returns the result on the UI thread.
 */

public abstract class CammentAsyncClient {

    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    private static final Thread UI_THREAD = Looper.getMainLooper().getThread();

    protected ExecutorService executorService;

    protected CammentAsyncClient(ExecutorService executorService) {
        this.executorService = executorService;
    }

    //deliver on UI thread
    public <T> Future<T> submitTask(@NonNull final Callable<T> callable, @Nullable final CammentCallback<T> callback) {
        return executorService.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    T result = callable.call();
                    onResult(result, callback, true);
                    return result;

                } catch (Exception e) {
                    onException(e, callback, true);
                    return null;
                }
            }
        });
    }

    //deliver on background thread
    public <T> Future<T> submitBgTask(@NonNull final Callable<T> callable, @Nullable final CammentCallback<T> callback) {
        return executorService.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    T result = callable.call();
                    onResult(result, callback, false);
                    return result;

                } catch (Exception e) {
                    onException(e, callback, false);
                    return null;
                }
            }
        });
    }

    private <T> void onResult(final T result, final CammentCallback<T> callback, final boolean deliverOnUI) {
        if (callback != null) {
            if (deliverOnUI) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(result);
                    }
                });
            } else {
                callback.onSuccess(result);
            }
        }
    }

    private <T> void onException(final Exception e, final CammentCallback<T> callback, final boolean deliverOnUI) {
        if (callback != null) {
            if (deliverOnUI) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onException(e);
                    }
                });
            } else {
                callback.onException(e);
            }
        }
    }

    protected final void runOnUiThread(@NonNull Runnable runnable) {
        if (Thread.currentThread() != UI_THREAD) {
            UI_HANDLER.post(runnable);
        } else {
            runnable.run();
        }
    }

}
