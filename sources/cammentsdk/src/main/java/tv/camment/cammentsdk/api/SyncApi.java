package tv.camment.cammentsdk.api;

import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.IotInRequest;
import com.camment.clientsdk.model.Usergroup;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.utils.LogUtils;

public final class SyncApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    SyncApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void sendIotMessage(final String type, final String message) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
                if (activeUserGroup != null
                        && !TextUtils.isEmpty(activeUserGroup.getUuid())) {
                    IotInRequest iotInRequest = new IotInRequest();
                    iotInRequest.setType(type);
                    iotInRequest.setMessage(message);

                    devcammentClient.usergroupsGroupUuidIotPost(activeUserGroup.getUuid(), iotInRequest);
                }
                return new Object();
            }
        }, getSendIotMessageCallback());
    }

    private CammentCallback<Object> getSendIotMessageCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "sendIotMessage");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "sendIotMessage", exception);
            }
        };
    }

}
