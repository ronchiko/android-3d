package com.roncho.engine.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Debug;
import android.view.MotionEvent;

import com.roncho.engine.structs.events.TouchEvent;
import com.roncho.engine.structs.primitive.Vector2;

public class GameRendererView extends GLSurfaceView {

    public static final TouchEvent touchEvent = new TouchEvent();

    public GameRendererView(Context context) {
        super(context);

        init();

        setEGLContextClientVersion(2);
        setRenderer(new WorldRenderer());

        touchEvent.add((v) -> Logger.Log("Position:" + v));
    }

    public static native void init();

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchEvent.invoke(new Vector2(event.getXPrecision(), event.getYPrecision()));
        performClick();
        return super.onTouchEvent(event);
    }
}
