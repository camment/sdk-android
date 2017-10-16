package tv.camment.cammentsdk.api;

import android.util.Log;
import android.widget.Toast;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.Show;
import com.camment.clientsdk.model.ShowList;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.ShowProvider;


public final class ShowApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    ShowApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void getShows(final String passcode) {
        submitTask(new Callable<ShowList>() {
            @Override
            public ShowList call() throws Exception {
                return devcammentClient.showsGet(passcode);
            }
        }, getShowsCallback());
    }

    private CammentCallback<ShowList> getShowsCallback() {
        return new CammentCallback<ShowList>() {
            @Override
            public void onSuccess(ShowList result) {
                if (result != null
                        && result.getItems() != null) {
                    ShowProvider.deleteShows();
                    ShowProvider.insertShows(result.getItems());
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getShows", exception);
            }
        };
    }

    public void getShowByUuid(final String uuid, final CammentCallback<Show> getShowByUuidCallback) {
        submitTask(new Callable<Show>() {
            @Override
            public Show call() throws Exception {
                return devcammentClient.showsUuidGet(uuid);
            }
        }, getShowByUuidCallback);
    }

}
