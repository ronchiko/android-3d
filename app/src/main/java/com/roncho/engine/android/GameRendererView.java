package com.roncho.engine.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.roncho.engine.helpers.Input;
import com.roncho.engine.structs.events.TouchEvent;

public class GameRendererView extends GLSurfaceView {

    public GameRendererView(Context context) {
        super(context);

        init();

        setEGLContextClientVersion(2);
        setRenderer(new WorldRenderer());
    }

    public static native void init();

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Input.onTouch.invoke(new TouchEvent.TouchEventInfo(event));
        performClick();
        return super.onTouchEvent(event);
    }
}
