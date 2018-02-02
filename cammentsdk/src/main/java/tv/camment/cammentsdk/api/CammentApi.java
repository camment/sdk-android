package tv.camment.cammentsdk.api;

import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.mobileconnectors.apigateway.ApiClientException;
import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.CammentInRequest;
import com.camment.clientsdk.model.CammentList;
import com.camment.clientsdk.model.Usergroup;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.utils.FileUtils;
import tv.camment.cammentsdk.utils.LogUtils;


public final class CammentApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    CammentApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void createUserGroupCamment(final Camment camment) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                CammentInRequest cammentInRequest = new CammentInRequest();
                cammentInRequest.setUuid(camment.getUuid());
                devcammentClient.usergroupsGroupUuidCammentsPost(camment.getUserGroupUuid(), cammentInRequest);
                return new Object();
            }
        }, createUserGroupCammentCallback(camment));
    }

    private CammentCallback<Object> createUserGroupCammentCallback(final Camment camment) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "createUserGroupCamment");

                CammentProvider.setCammentSent(camment.getUuid());
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createUserGroupCamment", exception);
            }
        };
    }

    public void getUserGroupCamments() {
        final Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        if (usergroup == null
                || TextUtils.isEmpty(usergroup.getUuid())) {
            return;
        }

        if (ApiCallManager.getInstance().canCall(ApiCallType.GET_CAMMENTS, usergroup.getUuid().hashCode())) {
            submitTask(new Callable<CammentList>() {
                @Override
                public CammentList call() throws Exception {
                    return devcammentClient.usergroupsGroupUuidCammentsGet(usergroup.getUuid());
                }
            }, getUserGroupCammentsCallback(usergroup.getUuid()));
        }
    }

    private CammentCallback<CammentList> getUserGroupCammentsCallback(final String groupUuid) {
        return new CammentCallback<CammentList>() {
            @Override
            public void onSuccess(CammentList cammentList) {
                LogUtils.debug("onSuccess", "getUserGroupCamments");
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_CAMMENTS, groupUuid.hashCode());

                if (cammentList != null
                        && cammentList.getItems() != null) {
                    for (Camment camment : cammentList.getItems()) {
                        if (!FileUtils.getInstance().isLocalVideoAvailable(camment.getUuid())) {
                            AWSManager.getInstance().getS3UploadHelper().preCacheFile(new CCamment(camment), false);
                        }
                    }
                    CammentProvider.insertCamments(cammentList.getItems());
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserGroupCamments", exception);
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_CAMMENTS, groupUuid.hashCode());
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
                LogUtils.debug("onSuccess", "deleteUserGroupCamment");

                CammentProvider.setCammentDeleted(camment.getUuid());
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "deleteUserGroupCamment", exception);

                if (exception instanceof ApiClientException
                        && (((ApiClientException) exception).getStatusCode() == 404 || ((ApiClientException) exception).getStatusCode() == 502)) {
                    CammentProvider.setCammentDeleted(camment.getUuid());
                }
            }
        };
    }

    public void markCammentAsReceived(final Camment camment) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                devcammentClient.cammentsCammentUuidPost(camment.getUuid());
                return new Object();
            }
        }, markCammentAsReceivedCallback());
    }

    private CammentCallback<Object> markCammentAsReceivedCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "markCammentAsReceived");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "markCammentAsReceived", exception);
            }
        };
    }

}
