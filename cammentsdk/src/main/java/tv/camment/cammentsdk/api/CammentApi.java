package tv.camment.cammentsdk.api;

import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.CammentInRequest;
import com.camment.clientsdk.model.CammentList;
import com.camment.clientsdk.model.Usergroup;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.utils.NoSqlHelper;

/**
 * Created by petrushka on 03/08/2017.
 */

public class CammentApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    public CammentApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void createUserGroupCamment(final Camment camment) {
        submitTask(new Callable<Camment>() {
            @Override
            public Camment call() throws Exception {
                CammentInRequest cammentInRequest = new CammentInRequest();
                cammentInRequest.setUuid(camment.getUuid());
                return devcammentClient.usergroupsGroupUuidCammentsPost(camment.getUserGroupUuid(), cammentInRequest);
            }
        }, createUserGroupCammentCallback());
    }

    private CammentCallback<Camment> createUserGroupCammentCallback() {
        return new CammentCallback<Camment>() {
            @Override
            public void onSuccess(Camment camment) {
                Log.d("onSuccess", "createUserGroupCamment");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createUserGroupCamment", exception);
            }
        };
    }

    public void getUserGroupCamments() {
        submitTask(new Callable<CammentList>() {
            @Override
            public CammentList call() throws Exception {
                final Usergroup usergroup = NoSqlHelper.getActiveGroup();

                return devcammentClient.usergroupsGroupUuidCammentsGet(usergroup.getUuid());
            }
        }, getUserGroupCammentsCallback());
    }

    private CammentCallback<CammentList> getUserGroupCammentsCallback() {
        return new CammentCallback<CammentList>() {
            @Override
            public void onSuccess(CammentList cammentList) {
                Log.d("onSuccess", "getUserGroupCamments");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserGroupCamments", exception);
            }
        };
    }

}
