package tv.camment.cammentsdk.asyncclient;

/**
 * A callback which methods.
 */
public interface CammentCallback<T> {

    /**
     * Invoked when the async operation has completed successfully.
     *
     * @param result The result, which the async operation returned.
     */
    public void onSuccess(final T result);

    /**
     * Invoked when the async operation has completed with an exception.
     *
     * @param exception The error from the async operation.
     */
    public void onException(final Exception exception);

}
