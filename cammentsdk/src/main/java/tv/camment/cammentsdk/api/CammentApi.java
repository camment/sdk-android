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
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CCamment;

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
        submitBgTask(new Callable<Camment>() {
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
                CammentProvider.insertCamment(new CCamment(camment));
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
                final Usergroup usergroup = UserGroupProvider.getUserGroup();

                return devcammentClient.usergroupsGroupUuidCammentsGet(usergroup.getUuid());
            }
        }, getUserGroupCammentsCallback());
    }

    private CammentCallback<CammentList> getUserGroupCammentsCallback() {
        return new CammentCallback<CammentList>() {
            @Override
            public void onSuccess(CammentList cammentList) {
                Log.d("onSuccess", "getUserGroupCamments");

                if (cammentList != null) {
                    CammentProvider.insertCamments(cammentList.getItems());
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserGroupCamments", exception);
            }
        };
    }

    public void deleteUserGroupCamment(final Camment camment) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                devcammentClient.usergroupsGroupUuidCammentsCammentUuidDelete(camment.getUuid(), camment.getUserGroupUuid());
                return new Object();
            }
        }, deleteUserGroupCammentCallback(camment));
    }

    private CammentCallback<Object> deleteUserGroupCammentCallback(final Camment camment) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "deleteUserGroupCamment");
                CammentProvider.deleteCammentByUuid(camment.getUuid());
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "deleteUserGroupCamment", exception);
            }
        };
    }
}
