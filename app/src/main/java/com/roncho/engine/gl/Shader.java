package com.roncho.engine.gl;

import android.opengl.GLES20;

import com.roncho.engine.Failable;
import com.roncho.engine.android.AssetHandler;
import com.roncho.engine.android.Logger;

import java.util.HashMap;

public class Shader implements Failable {

    private static final HashMap<String, Shader> Cache = new HashMap<>();


    public enum Type {
        VERTEX(GLES20.GL_VERTEX_SHADER),
        FRAGMENT(GLES20.GL_FRAGMENT_SHADER);

        private final int gl;

        Type(int gl){
            this.gl = gl;
        }
    }

    private boolean failed;
    private int glId;

    private Shader(String path, Type type){
        String source = AssetHandler.loadText(AssetHandler.SHADERS_PATH + "/" + path);

        glId = GLES20.glCreateShader(type.gl);

        GLES20.glShaderSource(glId, source);
        GLES20.glCompileShader(glId);

        // Logger.Log(source);

        int[] p = new int[1];
        GLES20.glGetShaderiv(glId, GLES20.GL_COMPILE_STATUS, p, 0);
        if(p[0] == GLES20.GL_FALSE){
            String error = GLES20.glGetShaderInfoLog(glId);
            Logger.Error("Shader compile failed: " + error);
            GLES20.glDeleteShader(glId);
            glId = 0;
            failed();
            return;
        }

        Cache.put(path, this);
    }

    @Override
    public void failed() {
        failed = true;
    }
    @Override
    public boolean isFailure() {
        return failed;
    }

    /**
     * Loads a shader from the cache or creates a new one
     * @param path path to shader (Under assets/shaders)
     * @return  The loaded shader
     */
    public static Shader load(String path){
        if(Cache.containsKey(path)) return Cache.get(path);
        int index = path.lastIndexOf('.');
        String ext = path.substring(index + 1);
        return new Shader(path, ext.equals("frag") ? Type.FRAGMENT : Type.VERTEX);
    }

    public int getGlId() {return glId;}
}
