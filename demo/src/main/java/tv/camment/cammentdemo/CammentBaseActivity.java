package tv.camment.cammentdemo;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import tv.camment.cammentauth.FbHelper;
import tv.camment.cammentdemo.receiver.NetworkChangeReceiver;
import tv.camment.cammentsdk.CammentSDK;

abstract class CammentBaseActivity extends AppCompatActivity {

    private BroadcastReceiver networkReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FbHelper.getInstance().onActivityResult(requestCode, resultCode, data);

        CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkReceiver = new NetworkChangeReceiver();
        this.registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onPause() {
        if (networkReceiver != null) {
            this.unregisterReceiver(networkReceiver);
        }
        super.onPause();
    }

}