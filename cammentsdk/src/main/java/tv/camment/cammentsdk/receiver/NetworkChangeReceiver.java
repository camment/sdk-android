package tv.camment.cammentsdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.utils.NetworkUtils;


public final class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isConnected = NetworkUtils.getInstance().isInternetConnectionAvailable(context);

            Log.d("INTERNET", "SDK " + isConnected);

            if (isConnected) {
                ApiManager.getInstance().getCammentApi().getUserGroupCamments();
            }
        }
    }

}
