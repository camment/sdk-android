package tv.camment.cammentsdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.camment.clientsdk.model.Usergroup;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.helpers.SyncHelper;
import tv.camment.cammentsdk.utils.NetworkUtils;


public final class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isConnected = NetworkUtils.getInstance().isInternetConnectionAvailable(context);

            if (isConnected) {
                AWSManager.getInstance().getIoTHelper().connect();

                ApiManager.getInstance().getUserApi().checkUserAfterConnectionRestoredAndGetData();

                String identityId = IdentityPreferences.getInstance().getIdentityId();
                Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
                if (activeUserGroup != null
                        && !TextUtils.equals(identityId, activeUserGroup.getHostId())) {
                    SyncHelper.getInstance().sendNeedPositionUpdate();
                }
            }
        }
    }

}
