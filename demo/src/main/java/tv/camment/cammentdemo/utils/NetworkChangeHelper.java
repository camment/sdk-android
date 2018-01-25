package tv.camment.cammentdemo.utils;


public class NetworkChangeHelper {

    private static final NetworkChangeHelper ourInstance = new NetworkChangeHelper();

    private NetworkState networkState;

    public static NetworkChangeHelper getInstance() {
        return ourInstance;
    }

    private NetworkChangeHelper() {
        networkState = NetworkState.UNDEFINED;
    }

    public boolean shouldShowOfflineToast(NetworkState networkState) {
        NetworkState previousState = this.networkState;

        this.networkState = networkState;

        return networkState == NetworkState.OFFLINE
                && previousState != NetworkState.OFFLINE;
    }

    public enum NetworkState {
        ONLINE,
        OFFLINE,
        UNDEFINED
    }
}
