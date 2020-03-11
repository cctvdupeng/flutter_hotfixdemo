package io.tick.flutter_hotfixdemo;

import android.os.Bundle;
import android.util.Log;

import com.tencent.bugly.beta.Beta;

import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeneratedPluginRegistrant.registerWith(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("MainActivity", "onBackPressed");

        Beta.unInit();
    }
}
