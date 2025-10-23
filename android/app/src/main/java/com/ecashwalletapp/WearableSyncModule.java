package com.ecashwalletapp;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class WearableSyncModule extends ReactContextBaseJavaModule {
    
    public WearableSyncModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }
    
    @Override
    public String getName() {
        return "WearableSync";
    }
    
    @ReactMethod
    public void sendAddressToWatch(String address) {
        WearableListenerService.sendAddressToWatch(getReactApplicationContext(), address);
    }
}

