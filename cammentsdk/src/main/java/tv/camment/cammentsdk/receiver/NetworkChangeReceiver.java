package tv.camment.cammentsdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.utils.NetworkUtils;


public final class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isConnected = NetworkUtils.getInstance().isInternetConnectionAvailable(context);

            if (isConnected) {
                AWSManager.getInstance().getIoTHelper().connect();

                ApiManager.getInstance().getUserApi().checkUserAfterConnectionRestoredAndGetData();
            }
        }
    }

}
