package tv.camment.cammentsdk.asyncclient;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.helpers.AuthHelper;

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
    protected <T> Future<T> submitTask(@NonNull final Callable<T> callable, @Nullable final CammentCallback<T> callback) {
        final String uuid = UUID.randomUUID().toString();
        Callable<T> call = new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    T result = callable.call();
                    onResult(result, callback, true, uuid);
                    return result;

                } catch (Exception e) {
                    onException(e, callback, true, uuid);
                    return null;
                }
            }
        };
        ApiManager.getInstance().putCallable(uuid, call);
        return executorService.submit(call);
    }

    //deliver on background thread
    protected <T> Future<T> submitBgTask(@NonNull final Callable<T> callable, @Nullable final CammentCallback<T> callback) {
        final String uuid = UUID.randomUUID().toString();
        Callable<T> call = new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    T result = callable.call();
                    onResult(result, callback, false, uuid);
                    return result;

                } catch (Exception e) {
                    onException(e, callback, false, uuid);
                    return null;
                }
            }
        };
        ApiManager.getInstance().putCallable(uuid, call);
        return executorService.submit(call);
    }

    private <T> void onResult(final T result, final CammentCallback<T> callback, final boolean deliverOnUI, String uuid) {
        ApiManager.getInstance().removeCallable(uuid);

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

    private <T> void onException(final Exception e, final CammentCallback<T> callback, final boolean deliverOnUI, String uuid) {
        if (e != null
                && (e instanceof NotAuthorizedException
                || e.getCause() instanceof NotAuthorizedException)) {
            ApiManager.getInstance().putRetryCallable(uuid);

            ApiManager.getInstance().removeCallable(uuid);

            AuthHelper.getInstance().checkLogin();
            return;
        }

        ApiManager.getInstance().removeCallable(uuid);

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
