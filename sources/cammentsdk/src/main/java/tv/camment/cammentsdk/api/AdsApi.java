package tv.camment.cammentsdk.api;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.Sofa;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;


public final class AdsApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    AdsApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void getSofaData(final String showUuid, final CammentCallback<Sofa> getSofaDataCallback) {
        submitTask(new Callable<Sofa>() {
            @Override
            public Sofa call() throws Exception {
                return devcammentClient.sofaShowUuidGet(showUuid);
            }
        }, getSofaDataCallback);
    }

}
