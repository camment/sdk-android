package com.yourapp.yourapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import tv.camment.cammentsdk.CammentSDK;


abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // IMPORTANT: Notify CammentSDK about activity result (e.g. facebook login result is read in here)
        CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data);
    }

}
