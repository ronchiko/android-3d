package com.roncho.engine.gl.objects;

import android.opengl.Matrix;

import com.roncho.engine.structs.primitive.Quaternion;
import com.roncho.engine.structs.primitive.d3.Vector3;

public class Camera {
    public class Transform {
        public Vector3 position;
        public Vector3 scale;
        public Quaternion rotation;

        public Vector3 up, front;

        public Transform(){
            position = new Vector3(0, 0, -3); Vector3.Zero.copy();
            rotation = Quaternion.identity();
        }

        public void setViewMatrix(){
            front = Vector3.Forward.rotate(rotation);
            up = Vector3.Up.rotate(rotation);

            Matrix.setLookAtM(viewMatrix, 0, position.x, position.y, position.z,
                    position.x + front.x ,position.y + front.y, position.z + front.z,
                    up.x, up.y, up.z);
        }

        public Vector3 forwards(){
            return Vector3.Forward.rotate(rotation);
        }
    }

    public Transform transform;
    public float fov;
    public float farPlane, nearPlane;
    public float aspect;

    private final float[] viewMatrix, perspectiveMatrix, mvpMatrix, orthoMatrix;


    public Camera(){
        transform = new Transform();
        fov = 90f;
        farPlane = 1000f;
        nearPlane = .01f;
        viewMatrix = new float[16];
        perspectiveMatrix = new float[16];
        mvpMatrix = new float[16];
        orthoMatrix = new float[16];
        aspect = 1;

    }

    public void recalculateProjectionMatrix(float w, float h){
        aspect = w / h;
        Matrix.perspectiveM(perspectiveMatrix, 0, fov, aspect, nearPlane, farPlane);
        Matrix.orthoM(orthoMatrix, 0, 0, w, 0, h, 0, 1);
    }
    public float[] getMvpMatrix(){
        transform.setViewMatrix();
        Matrix.multiplyMM(mvpMatrix, 0, perspectiveMatrix, 0, viewMatrix, 0);
        return mvpMatrix;
    }
    public float[] getOrthographicMatrix(){
        return orthoMatrix;
    }
}
