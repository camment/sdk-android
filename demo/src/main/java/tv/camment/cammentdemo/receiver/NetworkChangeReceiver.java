package tv.camment.cammentdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import tv.camment.cammentdemo.R;
import tv.camment.cammentdemo.utils.NetworkChangeHelper;
import tv.camment.cammentsdk.utils.NetworkUtils;


public final class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isConnected = NetworkUtils.getInstance().isInternetConnectionAvailable(context);
            boolean shouldShowOfflineToast = NetworkChangeHelper.getInstance()
                    .shouldShowOfflineToast(isConnected ? NetworkChangeHelper.NetworkState.ONLINE : NetworkChangeHelper.NetworkState.OFFLINE);

            if (shouldShowOfflineToast) {
                Toast.makeText(context, R.string.no_network, Toast.LENGTH_LONG).show();
            }
        }
    }

}
