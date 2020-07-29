package com.bbq.test;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactInstanceManagerBuilder;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.shell.MainReactPackage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReactNativeActivity extends AppCompatActivity {
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 127;//这个值是自定义的一个int值，在申请多个权限时要保证这个值不重复，才能在回调时进行判断
    private ReactRootView mReactRootView;
    private CompleteReceiver mDownloadCompleteReceiver;
    private long mDownloadId;
    private DownloadManager mDownloadManager;
    private ReactInstanceManager mReactInstanceManager;
    public static final String JS_BUNDLE_REMOTE_URL = "https://public.smoex.com/index.android.bundle";
    public static final String JS_BUNDLE_LOCAL_FILE = "index.android.bundle";
    public static final String JS_BUNDLE_REACT_UPDATE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "react_native_update";
    public static final String JS_BUNDLE_LOCAL_PATH = JS_BUNDLE_REACT_UPDATE_PATH + File.separator + JS_BUNDLE_LOCAL_FILE;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            //申请WRITE_EXTERNAL_STORAGE权限
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
//        }

        ReactInstanceManagerBuilder builder = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setCurrentActivity(this)
                // assets 目录下文件
//                .setBundleAssetName("index.android.bundle")
                // hot load https://10.0.2.2/index.js 文件
                .setJSMainModulePath("index")
                .addPackage(new MainReactPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED);

        File file = new File(JS_BUNDLE_LOCAL_PATH);
        if (file.exists()) {
            Log.i("xxxxxxxx", "xxxxxxxxxxxx");
            builder.setJSBundleFile(JS_BUNDLE_LOCAL_PATH);
        } else {
            Log.i("xxxxxxxx", "ooooooooooooo");
            builder.setBundleAssetName(JS_BUNDLE_LOCAL_FILE);
        }

        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = builder.build();
        mReactRootView.startReactApplication(mReactInstanceManager, "AndroidTestRN", null);
        setContentView(mReactRootView);

        initDownloadManager();
        updateJSBundle();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateJSBundle();
            } else {
                // Permission Denied 用户拒绝
            }
        }
    }

    private void updateJSBundle() {

        final File file = new File(JS_BUNDLE_LOCAL_PATH);
        if (file.exists()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    file.delete();
                }
            }, 5000);
            return;
        }


        File rootDir = new File(JS_BUNDLE_REACT_UPDATE_PATH);
        if (!rootDir.exists()) {
            rootDir.mkdir();
        }

        File res = new File(JS_BUNDLE_REACT_UPDATE_PATH + File.separator + "drawable-mdpi");
        if (!res.exists()) {
            res.mkdir();
        }

//        FileAssetUtils.copyAssets(this, "drawable-mdpi", JS_BUNDLE_REACT_UPDATE_PATH);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(JS_BUNDLE_REMOTE_URL));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
//        Uri downloadUri = FileProvider.getUriForFile(this, "com.bbq.test.fileProvider", file);
//        request.setDestinationUri(downloadUri);
        request.setDestinationUri(Uri.fromFile(file));
//        request.setDestinationInExternalPublicDir(JS_BUNDLE_REACT_UPDATE_PATH, JS_BUNDLE_LOCAL_FILE);
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Toast.makeText(this, "准备下载", Toast.LENGTH_SHORT).show();
        mDownloadId = mDownloadManager.enqueue(request);
        Log.i("xxxxx", "" + mDownloadId);

    }

    private void initDownloadManager() {
        mDownloadCompleteReceiver = new CompleteReceiver();
        registerReceiver(mDownloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDownloadCompleteReceiver);
    }

    private class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("xxxxxxx", "33333333333");
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            checkStatus();
        }
    }
    //检查下载状态
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(mDownloadId);
        Cursor cursor = mDownloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    Toast.makeText(this, "正在下载", Toast.LENGTH_SHORT).show();
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    //下载完成安装APK
//                    installAPK();
                    Toast.makeText(this, "下载成功", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    this.unregisterReceiver(mDownloadCompleteReceiver);
                    break;
            }
        }
    }
//    private void onJSBundleLoadedFromServer() {
//        final File file = new File(JS_BUNDLE_LOCAL_PATH);
//        if (file == null || !file.exists()) {
////            Log.i(TAG, "js bundle file download error, check URL or network state");
//            return;
//        }
//
//        Log.i(TAG, "js bundle file file success, reload js bundle");
//
////        Toast.makeText(UpdateReactActivity.this, "download bundle complete", Toast.LENGTH_SHORT).show();
//        try {
//
//            Class<?> RIManagerClazz = mReactInstanceManager.getClass();
//
//            Field f = RIManagerClazz.getDeclaredField("mJSCConfig");
//            f.setAccessible(true);
//            JSCConfig jscConfig = (JSCConfig)f.get(mReactInstanceManager);
//
//            Method method = RIManagerClazz.getDeclaredMethod("recreateReactContextInBackground",
//                    com.facebook.react.cxxbridge.JavaScriptExecutor.Factory.class,
//                    com.facebook.react.cxxbridge.JSBundleLoader.class);
//            method.setAccessible(true);
//            method.invoke(mReactInstanceManager,
//                    new com.facebook.react.cxxbridge.JSCJavaScriptExecutor.Factory(jscConfig.getConfigMap()),
//                    com.facebook.react.cxxbridge.JSBundleLoader.createFileLoader(getApplicationContext(), JS_BUNDLE_LOCAL_PATH));
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e){
//            e.printStackTrace();
//        }
//    }
}
