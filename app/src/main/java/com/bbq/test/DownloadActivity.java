package com.bbq.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class DownloadActivity extends AppCompatActivity {
    public static final String JS_BUNDLE_REMOTE_URL = "https://public.smoex.com/index.android.bundle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        new DownloadUtils(this, JS_BUNDLE_REMOTE_URL, "index.android.bundle");
    }
}
