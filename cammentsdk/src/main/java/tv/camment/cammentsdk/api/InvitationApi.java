package tv.camment.cammentsdk.api;

import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.AcceptInvitationRequest;
import com.camment.clientsdk.model.FacebookFriend;
import com.camment.clientsdk.model.UserInAddToGroupRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.utils.NoSqlHelper;

/**
 * Created by petrushka on 03/08/2017.
 */

public class InvitationApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    public InvitationApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void sendInvitation(final List<FacebookFriend> fbFriends, final CammentCallback<Object> sendInvitationCallback) {
        submitTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final String showUuid = NoSqlHelper.getCurrentShow().getUuid();
                final String userGroupUuid = NoSqlHelper.getActiveGroup().getUuid();

                UserInAddToGroupRequest userInAddToGroupRequest = new UserInAddToGroupRequest();
                userInAddToGroupRequest.setShowUuid(showUuid);

                List<String> fbUserIdsStrings = new ArrayList<>();
                for (FacebookFriend fbFriend : fbFriends) {
                    fbUserIdsStrings.add(String.valueOf(fbFriend.getId().longValue()));
                }
                userInAddToGroupRequest.setUserFacebookIdList(fbUserIdsStrings);

                devcammentClient.usergroupsGroupUuidUsersPost(userGroupUuid, userInAddToGroupRequest);

                return new Object();
            }
        }, sendInvitationCallback);
    }

    public void acceptInvitation(final InvitationMessage invitationMessage) {
        submitTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                AcceptInvitationRequest acceptInvitationRequest = new AcceptInvitationRequest();
                acceptInvitationRequest.setInvitationKey(invitationMessage.body.key);
                devcammentClient.usergroupsGroupUuidInvitationsPut(invitationMessage.body.groupUuid, acceptInvitationRequest);

                return new Object();
            }
        }, acceptInvitationCallback());
    }

    private CammentCallback<Object> acceptInvitationCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "acceptInvitation");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "acceptInvitation", exception);
            }
        };
    }

}
