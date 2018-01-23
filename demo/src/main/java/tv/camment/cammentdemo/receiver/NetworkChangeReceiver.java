package tv.camment.cammentdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import tv.camment.cammentdemo.R;
import tv.camment.cammentsdk.utils.NetworkUtils;


public final class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isConnected = NetworkUtils.getInstance().isInternetConnectionAvailable(context);

            Log.d("INTERNET", "APP " + isConnected);

            if (!isConnected) {
                Toast.makeText(context, R.string.no_network, Toast.LENGTH_LONG).show();
            }
        }
    }

}
