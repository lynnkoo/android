package com.bbq.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class DownloadActivity extends AppCompatActivity {
    public static final String JS_BUNDLE_REMOTE_URL = "https://public.smoex.com/index.android.bundle";
    public static final String JS_BUNDLE_LOCAL_FILE = "index.android.bundle";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
    }

    public void startDownload(View view) {
        DownloadUtils downloadUtils = new DownloadUtils(this);
        downloadUtils.setLifecycleOwner(this);
        downloadUtils.setDownloadListener(new DownloadUtils.DownloadListener() {
            @Override
            public void onSuccess(String path) {
                Toast.makeText(DownloadActivity.this, "下载成功！ path: " + path, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(DownloadActivity.this, "下载失败！", Toast.LENGTH_SHORT).show();
            }
        });

        downloadUtils.download(JS_BUNDLE_REMOTE_URL, JS_BUNDLE_LOCAL_FILE);
    }
}
