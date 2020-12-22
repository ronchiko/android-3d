package com.roncho.engine.helpers;

import android.util.Log;

import com.roncho.engine.android.Logger;
import com.roncho.engine.structs.Mesh;
import com.roncho.engine.structs.primitive.Rect;
import com.roncho.engine.structs.primitive.Vector2;
import com.roncho.engine.structs.primitive.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Builder {
    public static FloatBuffer newFloatBuffer(int size){
        ByteBuffer bb = makeDirectByteBuffer(size * 4);
        return bb.asFloatBuffer();
    }

    public static ByteBuffer makeByteBuffer(int size){
        ByteBuffer bb = ByteBuffer.allocate(size);
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }
    public static ByteBuffer makeDirectByteBuffer(int size){
        ByteBuffer bb = ByteBuffer.allocateDirect(size);
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }

    public static FloatBuffer makeRectBuffer(Rect rect) {
        return makeRectBuffer(rect.toArray());
    }
    public static FloatBuffer makeRectBuffer(float[] array){
        if(array == null) return newFloatBuffer(0);

        FloatBuffer fb = newFloatBuffer(8);
        fb.put(array[0]);
        fb.put(array[1]);
        fb.put(array[2]);
        fb.put(array[1]);
        fb.put(array[0]);
        fb.put(array[3]);
        fb.put(array[2]);
        fb.put(array[3]);
        fb.position(0);
        return fb;
    }

    public static class MeshBuilder {
        public class VertexLink {
            public Vector3 position;
            public Vector3 normal;
            public Vector2 uv;

            private VertexLink(Vector3 position){
                this.position = position;
                normal = Vector3.Zero.copy();
                uv = Vector2.Zero.copy();
                verteciesMap.put(serializeVector3(position), this);
            }
        }

        public class FaceLink {
            private final static int VERTEX_PER_FACE = 3;

            public final VertexLink[] vertecies;

            private FaceLink(){
                vertecies = new VertexLink[VERTEX_PER_FACE];
            }

            public void setVertex(Vector3 v3, int index){
                vertecies[index] = newVertex(v3);
            }
        }

        private final HashMap<String, VertexLink> verteciesMap;
        private final ArrayList<FaceLink> faces;

        private static String serializeVector3(Vector3 v){
            return v.toString();
        }

        public MeshBuilder(){
            verteciesMap = new HashMap<>();
            faces = new ArrayList<>();
        }

        public VertexLink newVertex(Vector3 v3){
            String key = serializeVector3(v3);
            if(verteciesMap.containsKey(key)) return verteciesMap.get(key);
            return new VertexLink(v3);
        }

        public FaceLink newFace(){
            FaceLink face = new FaceLink();
            faces.add(face);
            return face;
        }

        public Mesh build(){
            Mesh mesh = Mesh.allocateNew(faces.size() * 3);

            for(FaceLink f : faces) {
                for(int i = 0; i < FaceLink.VERTEX_PER_FACE; i++){
                    VertexLink link = f.vertecies[i];
                    mesh.vertecies.put(link.position.x);
                    mesh.vertecies.put(link.position.y);
                    mesh.vertecies.put(link.position.z);

                    mesh.uvs.put(link.uv.x);
                    mesh.uvs.put(link.uv.y);

                    mesh.normals.put(link.normal.x);
                    mesh.normals.put(link.normal.y);
                    mesh.normals.put(link.normal.z);
                }
            }
            return mesh;
        }
    }
}
