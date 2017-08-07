package tv.camment.cammentdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.CallbackManager;

import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.helpers.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("HAHA", "onActivityResult");
        if (callbackManager == null) {
            callbackManager = FacebookHelper.getInstance().getCallbackManager();
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);

        PermissionHelper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionHelper.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
