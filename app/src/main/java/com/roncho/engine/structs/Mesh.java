package com.roncho.engine.structs;

import com.roncho.engine.Failable;
import com.roncho.engine.android.AssetHandler;
import com.roncho.engine.android.Logger;
import com.roncho.engine.helpers.Builder;
import com.roncho.engine.structs.primitive.Quaternion;
import com.roncho.engine.structs.primitive.d3.Vector3;

import java.nio.FloatBuffer;
import java.util.HashMap;

public class Mesh implements Failable {

    private class WavefrontMeshData {
        public float[] verts, uv, norm, special;

        public WavefrontMeshData(){
            uvs = null;
            vertecies = null;
            normals = null;
            special = null;
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

            _edges = new Vector3[special.length / 3];
            for(int i = 0; i < _edges.length; i++){
                _edges[i] = new Vector3(special[i * 3], special[i * 3 + 1], special[i * 3 + 2]);
            }
        }
    }

    private final static HashMap<String, Mesh> Cache = new HashMap<>();

    private String path;
    private boolean failed;
    private int size;

    private Vector3[] _edges;

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
        (md = makeMeshData()).parse(text);
        md.constructBuffers();
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

    public Vector3[] edges(Quaternion rotation, Vector3 scale) {
        Vector3[] e = new Vector3[_edges.length];
        for(int i = 0; i < e.length; i++) {
            e[i] = _edges[i];
            if(!scale.equals(Vector3.One)) e[i] = e[i].scale(scale);
            if(!rotation.equals(Quaternion.identity())) e[i] = e[i].rotate(rotation);
        }
        return e;
    }

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
