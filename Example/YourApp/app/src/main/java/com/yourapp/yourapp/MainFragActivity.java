package com.yourapp.yourapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import tv.camment.cammentsdk.CammentSDK;

public class MainFragActivity extends BaseActivity {

    private static final String EXTRA_SHOW_UUID = "extra_show_uuid";

    public static void start(Context context, String showUuid) {
        Intent intent = new Intent(context, MainFragActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_SHOW_UUID, showUuid);
        context.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);

        Intent oldIntent = getIntent();

        if (!TextUtils.equals(oldIntent.getStringExtra(EXTRA_SHOW_UUID), newIntent.getStringExtra(EXTRA_SHOW_UUID))) {
            setIntent(newIntent);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PlayerFragment.getInstance(getIntent().getStringExtra(EXTRA_SHOW_UUID))).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_frag);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PlayerFragment.getInstance(getIntent().getStringExtra(EXTRA_SHOW_UUID))).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //IMPORTANT to pass camera and microphone permission to CammentSDK
        CammentSDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
