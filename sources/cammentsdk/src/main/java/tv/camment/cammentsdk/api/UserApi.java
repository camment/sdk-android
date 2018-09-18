package tv.camment.cammentsdk.api;

import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.Usergroup;
import com.camment.clientsdk.model.UsergroupList;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.events.ApiCalledEvent;
import tv.camment.cammentsdk.events.ApiResultEvent;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.utils.LogUtils;


public final class UserApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    UserApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void getMyUserGroups() {
        if (CammentSDK.getInstance().getShowMetadata() != null
                && !TextUtils.isEmpty(CammentSDK.getInstance().getShowMetadata().getUuid())) {
            String indentityId = IdentityPreferences.getInstance().getIdentityId();

            int apiHashCode = indentityId.hashCode() + CammentSDK.getInstance().getShowMetadata().getUuid().hashCode();

            if (ApiCallManager.getInstance().canCall(ApiCallType.GET_MY_USER_GROUPS, apiHashCode)) {
                submitBgTask(new Callable<UsergroupList>() {
                    @Override
                    public UsergroupList call() throws Exception {
                        EventBus.getDefault().post(new ApiCalledEvent(ApiCallType.GET_MY_USER_GROUPS));

                        return devcammentClient.meGroupsGet(CammentSDK.getInstance().getShowMetadata().getUuid());
                    }
                }, getMyUserGroupsCallback(apiHashCode, CammentSDK.getInstance().getShowMetadata().getUuid()));
            }
        }
    }

    private CammentCallback<UsergroupList> getMyUserGroupsCallback(final int apiHashCode, final String showUuid) {
        return new CammentCallback<UsergroupList>() {
            @Override
            public void onSuccess(UsergroupList result) {
                LogUtils.debug("onSuccess", "getMyUserGroups");
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_MY_USER_GROUPS, apiHashCode);

                EventBus.getDefault().post(new ApiResultEvent(ApiCallType.GET_MY_USER_GROUPS, true));

                if (result != null
                        && result.getItems() != null) {
                    UserGroupProvider.insertUserGroups(result.getItems(), showUuid);
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getMyUserGroups", exception);
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_MY_USER_GROUPS, apiHashCode);

                EventBus.getDefault().post(new ApiResultEvent(ApiCallType.GET_MY_USER_GROUPS, false));
            }
        };
    }

    public void checkUserAfterConnectionRestoredAndGetData() {
        final Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup == null || TextUtils.isEmpty(activeUserGroup.getUuid())) {
            return;
        }

        ApiManager.getInstance().getGroupApi().getUserGroupByUuid(activeUserGroup.getUuid()); //TODO get also camments?
    }

    public void sendCongnitoIdChanged() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                devcammentClient.meUuidPut();
                return new Object();
            }
        }, new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "sendCongnitoIdChanged");
            }

            @Override
            public void onException(Exception e) {
                Log.e("onException", "sendCongnitoIdChanged", e);
            }
        });
    }

    public void retrieveNewIdentityId() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                AWSManager.getInstance().getCammentAuthenticationProvider().refresh();
                return new Object();
            }
        }, new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "retrieveNewIdentityId");
            }

            @Override
            public void onException(Exception exception) {
                LogUtils.debug("onException", "retrieveNewIdentityId", exception);
            }
        });
    }


}
