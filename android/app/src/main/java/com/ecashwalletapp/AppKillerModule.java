package com.ecashwalletapp;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import android.app.Activity;

public class AppKillerModule extends ReactContextBaseJavaModule {
    
    public AppKillerModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "AppKiller";
    }

    @ReactMethod
    public void killApp() {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            // Finish all activities in the task and exit
            activity.finishAffinity();
        }
    }
}
