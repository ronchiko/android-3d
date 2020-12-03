package com.roncho.game;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.roncho.engine.android.AssetHandler;
import com.roncho.engine.android.GameRendererView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AssetHandler.init(getAssets(), getBaseContext());
        setContentView(new GameRendererView(getBaseContext()));
    }

}