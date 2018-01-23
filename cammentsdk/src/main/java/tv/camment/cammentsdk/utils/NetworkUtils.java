package tv.camment.cammentsdk.utils;

import android.content.Context;
import android.net.ConnectivityManager;


public class NetworkUtils {

    private static final NetworkUtils instance = new NetworkUtils();

    public static NetworkUtils getInstance() {
        return instance;
    }

    private NetworkUtils() {
    }

    public boolean isInternetConnectionAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected();
    }

}
