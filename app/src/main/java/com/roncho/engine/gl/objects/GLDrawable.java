package com.roncho.engine.gl.objects;

import android.opengl.GLES20;

import com.roncho.engine.Failable;
import com.roncho.engine.android.Logger;
import com.roncho.engine.gl.Shader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class GLDrawable implements Failable {
    protected int program;
    private boolean failed;

    /**
     * Makes a render program for this object
     * @param vertex the vertex shader to use
     * @param frag the fragment shader to use
     */
    public void makeProgram(Shader vertex, Shader frag){
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertex.getGlId());
        GLES20.glAttachShader(program, frag.getGlId());

        GLES20.glLinkProgram(program);

        int[] p = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, p, 0);
        if(p[0] == GLES20.GL_FALSE){
            String error = GLES20.glGetProgramInfoLog(program);
            Logger.Error("Failed to link program: " + error);
            failed();
            return;
        }

        setupGraphics();
    }

    /**
     * Prepares the pointers to the gl program attributes & uniforms
     */
    public abstract void setupGraphics();

    /**
     * Draws the objects
     * @param mvpMatrix float[16] projection matrix x view matrix
     */
    public abstract void draw(float[] mvpMatrix);

    @Override
    public void failed() {
        failed = true;
    }

    @Override
    public boolean isFailure() {
        return failed;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AutoInitiated {}

    public void onDestroyed() {}
}
