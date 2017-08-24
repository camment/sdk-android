package tv.camment.cammentsdk.api;

import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.ShowList;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;


public final class ShowApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    ShowApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void getShows() {
        submitTask(new Callable<ShowList>() {
            @Override
            public ShowList call() throws Exception {
                return devcammentClient.showsGet();
            }
        }, getShowsCallback());
    }

    private CammentCallback<ShowList> getShowsCallback() {
        return new CammentCallback<ShowList>() {
            @Override
            public void onSuccess(ShowList result) {

            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "shows", exception);
            }
        };
    }

}
