package com.roncho.engine.structs;

import android.opengl.GLES20;

public class Sprite {

    public static final int RECT_SIZE = 4;

    private final float[] rect;
    private Texture2D sheet;

    public Sprite(Texture2D sheet, float sx, float sy, float ex, float ey){
        rect = new float[]{
                sx, sy, ex, ey
        };
        this.sheet = sheet;
    }

    public void passToShader(int sampleHandle, int uvHandle){
    }
}
