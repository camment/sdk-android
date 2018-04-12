package com.yourapp.yourapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import tv.camment.cammentsdk.EurovisionShowsActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnShow = findViewById(R.id.btn_show);
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityWithShow();
            }
        });
    }

    private void openActivityWithShow() {
        EurovisionShowsActivity.start(this);
    }

}
