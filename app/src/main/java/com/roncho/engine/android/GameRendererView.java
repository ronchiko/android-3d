package com.roncho.engine.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.roncho.engine.Engine;
import com.roncho.engine.helpers.Input;
import com.roncho.engine.structs.events.TouchEvent;

public class GameRendererView extends GLSurfaceView {

    private final WorldRenderer renderer;

    public GameRendererView(Context context, Engine creator) {
        super(context);

        init();

        setEGLContextClientVersion(2);
        renderer = new WorldRenderer(creator);
        setRenderer(renderer);
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

    public WorldRenderer getRenderer() { return renderer; }
}
