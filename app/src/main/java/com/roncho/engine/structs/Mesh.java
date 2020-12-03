package com.roncho.engine.structs;

import com.roncho.engine.Failable;
import com.roncho.engine.android.AssetHandler;
import com.roncho.engine.android.Logger;
import com.roncho.engine.helpers.Builder;

import java.nio.FloatBuffer;
import java.util.HashMap;

public class Mesh implements Failable {

    private class WavefrontMeshData {
        public float[] verts, uv, norm;

        public WavefrontMeshData(){
            uvs = null;
            vertecies = null;
            normals = null;
        }

        private native void parse(String data);

        private void constructBuffers(){
            vertecies = Builder.newFloatBuffer(this.verts.length);
            vertecies.put(verts);
            vertecies.position(0);
            uvs = Builder.newFloatBuffer(this.uv.length);
            uvs.put(uv);
            uvs.position(0);
            normals = Builder.newFloatBuffer(this.norm.length);
            normals.put(norm);
            normals.position(0);
        }
    }

    private final static HashMap<String, Mesh> Cache = new HashMap<>();

    private String path;
    private boolean failed;
    private int size;
    public FloatBuffer vertecies, uvs, normals;

    /** Creates an empty mesh with a pre allocated vertecies*/
    private Mesh(int size){
        this.size = size;
        failed = false;
        path = "Custom Mesh";
        vertecies = Builder.newFloatBuffer(size * 3);
        uvs = Builder.newFloatBuffer(size * 2);
        normals = Builder.newFloatBuffer(size * 3);
    }
    /** Creates a new mesh from a path */
    private Mesh(String path){
        String text = AssetHandler.loadText(AssetHandler.MODELS_PATH + "/" + path);
        if(text == null) {
            return;
        }
        Logger.Log("Loading model: " + path);
        WavefrontMeshData md;
        (md = makeMeshData()).parse(text); md.constructBuffers();
        size = md.verts.length / 3;
        this.path = path;
        Cache.put(this.path, this);
    }

    /** Loads a mesh from a file*/
    public static Mesh load(String path){
        if(Cache.containsKey(path)) return Cache.get(path);
        return new Mesh(path);
    }

    @Override
    public void failed() {
        failed = true;
    }
    @Override
    public boolean isFailure() {
        return failed;
    }
    public int size() {return size;}

    private WavefrontMeshData makeMeshData(){
        return new WavefrontMeshData();
    }

    public static Mesh allocateNew(int vertecies){
        return new Mesh(vertecies);
    }

    @Override
    public String toString(){
        return "mesh(" + path + ")";
    }
}
