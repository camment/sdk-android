package tv.camment.cammentdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import tv.camment.cammentsdk.DeeplinkIgnore;

public class CammentSplashActivity extends AppCompatActivity
        implements DeeplinkIgnore {

    private static final int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camment_activity_camment_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openShowsActivity();
            }
        }, SPLASH_TIME_OUT);
    }

    private void openShowsActivity() {
        CammentShowsActivity.startClearHistory(this);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
