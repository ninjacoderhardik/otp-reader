package com.otpreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class SmsListenerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private SmsReceiver smsReceiver;

    public SmsListenerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "SmsListener";
    }

    @Override
    public void initialize() {
        super.initialize();
        smsReceiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        reactContext.registerReceiver(smsReceiver, filter);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        reactContext.unregisterReceiver(smsReceiver);
    }

    private class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? bundle.getString("format") : null);
                    String messageBody = smsMessage.getMessageBody();
                    String senderNumber = smsMessage.getOriginatingAddress();

                    WritableMap params = Arguments.createMap();
                    params.putString("body", messageBody);
                    params.putString("sender", senderNumber);

                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onSMSReceived", params);
                }
            }
        }
    }
}
