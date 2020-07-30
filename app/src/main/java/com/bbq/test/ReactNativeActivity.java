package com.bbq.test;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactInstanceManagerBuilder;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.shell.MainReactPackage;

import java.io.File;

public class ReactNativeActivity extends AppCompatActivity {
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 127;//这个值是自定义的一个int值，在申请多个权限时要保证这个值不重复，才能在回调时进行判断
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;
    public static final String JS_BUNDLE_REMOTE_URL = "https://public.smoex.com/index.android.bundle";
    public static final String JS_BUNDLE_LOCAL_FILE = "index.android.bundle";
    public static final String JS_BUNDLE_REACT_UPDATE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "Android/data/com.bbq.test/files/Download/index.android.bundle";
    public static final String JS_BUNDLE_LOCAL_PATH = JS_BUNDLE_REACT_UPDATE_PATH + File.separator + JS_BUNDLE_LOCAL_FILE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        File file = new File(DownloadUtils.DEFAULT_PATH_PARENT, JS_BUNDLE_LOCAL_FILE);
        Log.i("file path", file.getPath());

        if (file.exists()) {
            /** ReactNativeHost.java
             * Returns a custom path of the bundle file. This is used in cases the bundle should be loaded
             * from a custom path. By default it is loaded from Android assets, from a path specified by
             * {@link getBundleAssetName}. e.g. "file://sdcard/myapp_cache/index.android.bundle"
             */
            builder.setJSBundleFile(file.getAbsolutePath());
        } else {
            builder.setBundleAssetName(JS_BUNDLE_LOCAL_FILE);
        }

        // /storage/emulated/0/react_native_update/index.android.bundle
        // /storage/emulated/0/Android/data/com.bbq.test/files/Download/index.android.bundle
        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = builder.build();
        mReactRootView.startReactApplication(mReactInstanceManager, "AndroidTestRN", null);
        setContentView(mReactRootView);

        startDownload();
    }

    public void startDownload() {
        DownloadUtils downloadUtils = new DownloadUtils(this);
        downloadUtils.setLifecycleOwner(this);
        downloadUtils.setDownloadListener(new DownloadUtils.DownloadListener() {
            @Override
            public void onSuccess(String path) {
                Toast.makeText(ReactNativeActivity.this, "下载成功！ path: " + path, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(ReactNativeActivity.this, "下载失败！", Toast.LENGTH_SHORT).show();
            }
        });
        downloadUtils.download(JS_BUNDLE_REMOTE_URL, JS_BUNDLE_LOCAL_FILE);
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
