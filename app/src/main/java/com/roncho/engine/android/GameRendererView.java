package com.roncho.engine.android;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GameRendererView extends GLSurfaceView {

    public GameRendererView(Context context) {
        super(context);

        init();

        setEGLContextClientVersion(2);
        setRenderer(new WorldRenderer());
    }

    public static native void init();
}
