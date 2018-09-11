package com.yourapp.yourapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import tv.camment.cammentsdk.CammentSDK;

public class LoginActivity extends BaseActivity {

    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnShow1 = findViewById(R.id.btn_show1);
        btnShow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityWithShow();
            }
        });

        Button btnShow2 = findViewById(R.id.btn_show2);
        btnShow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityFragmentWithShow();
            }
        });

        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOnLoginButtonClick();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setLoginButtonText();
    }

    private void openActivityWithShow() {
        MainActivity.start(this, "111-aaa-222-bbb");
    }

    private void openActivityFragmentWithShow() {
        MainFragActivity.start(this, "111-aaa-222-bbb");
    }

    private void handleOnLoginButtonClick() {
        // IF YOU HAVE ALREADY FACEBOOK LOGIN/LOGOUT SOMEWHERE IN YOUR APP
        // YourFacebookLoginHelper here showcases how login code may look like in your app and to show how to connect to CammentSDK
        if (YourFacebookLoginHelper.getInstance().isLoggedIn()) {
            YourFacebookLoginHelper.getInstance().logOut();

            setLoginButtonText();
        } else {
            YourFacebookLoginHelper.getInstance().logIn(this);
        }
    }

    private void setLoginButtonText() {
        btnLogin.setText(YourFacebookLoginHelper.getInstance().isLoggedIn() ? "Log Out" : "Log In");
    }

}
