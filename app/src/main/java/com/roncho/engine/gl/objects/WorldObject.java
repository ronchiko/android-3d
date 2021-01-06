package com.roncho.engine.gl.objects;

import android.opengl.GLES20;

import com.roncho.engine.DontSerializeField;
import com.roncho.engine.structs.ComponentBase;
import com.roncho.engine.structs.Texture2D;
import com.roncho.engine.structs.primitive.Quaternion;
import com.roncho.engine.structs.primitive.d3.Vector3;
import com.roncho.engine.structs.Mesh;

import java.util.ArrayList;
import java.util.List;

public class WorldObject extends GLDrawable {

    public static float[] cameraForwards;

    /* Scene Lights Intensities & Settings */
    public static Vector3 ambientIntensity, diffuseIntensity;
    public static Vector3 specularConstant, sunDirection;

    /**
     * Handles passing the global lights to the shader
     */
    public class GlobalLights {
        private int specularConstantHandler;
        private int ambientIntensityHandler;
        private int diffuseIntensityHandler;
        private int sunDirectionHandler;

        /**
         * Prepares the handles for the program
         */
        private void setup(){
            specularConstantHandler = GLES20.glGetUniformLocation(program, "glights.specularReflectionConstant");

            ambientIntensityHandler = GLES20.glGetUniformLocation(program, "glights.ambientIntensity");

            diffuseIntensityHandler = GLES20.glGetUniformLocation(program, "glights.diffuseIntensity");
            sunDirectionHandler = GLES20.glGetUniformLocation(program, "glights.sunDirection");
        }

        private void pass() {
            GLES20.glUniform3fv(sunDirectionHandler, 1, sunDirection.toArray(), 0);
            GLES20.glUniform3fv(diffuseIntensityHandler, 1, diffuseIntensity.toArray(), 0);
            GLES20.glUniform3fv(ambientIntensityHandler, 1, ambientIntensity.toArray(), 0);
            GLES20.glUniform3fv(specularConstantHandler, 1, specularConstant.toArray(), 0);
        }
    }

    public class Transform extends ComponentBase {

        private @DontSerializeField int scaleHandle, positionHandle, rotationHandle;
        private final @DontSerializeField float[] buffer;

        public Vector3 position;
        public Vector3 scale;
        public Quaternion rotation;

        public Transform(){
            super();
            position = new Vector3(0, 0, 1); Vector3.Zero.copy();
            scale = Vector3.One.copy();
            rotation = Quaternion.identity();

            buffer = new float[10];
        }

        private void setup(){
            rotationHandle = GLES20.glGetUniformLocation(program, "transform.rotation");
            positionHandle = GLES20.glGetUniformLocation(program, "transform.position");
            scaleHandle = GLES20.glGetUniformLocation(program, "transform.scale");
        }

        private void pass(){
            GLES20.glUniform3fv(positionHandle, 1, buffer, 0);
            GLES20.glUniform3fv(scaleHandle, 1, buffer, 3);
            GLES20.glUniform4fv(rotationHandle, 1, buffer, 6);
        }

        private void resetBuffer(){
            buffer[0] = position.x;
            buffer[1] = position.y;
            buffer[2] = position.z;
            buffer[3] = scale.x;
            buffer[4] = scale.y;
            buffer[5] = scale.z;
            buffer[6] = rotation.x;
            buffer[7] = rotation.y;
            buffer[8] = rotation.z;
            buffer[9] = rotation.w;
        }

        private void inherit(Transform other){
            //Quaternion.rotateVQ(buffer, 0, buffer, 0, other.buffer, 6);
            buffer[0] += other.position.x;
            buffer[1] += other.position.y;
            buffer[2] += other.position.z;
            buffer[3] *= other.scale.x;
            buffer[4] *= other.scale.y;
            buffer[5] *= other.scale.z;
            //Quaternion.multiplyQQ(buffer, 6, other.buffer, 6, buffer, 6);
        }

        /**
         * The forwards vector of this transform
         * @return Vector3
         */
        public Vector3 forwards() {
            return Vector3.Forward.rotate(rotation);
        }
        /**
         * The up vector of this transform
         * @return Vector3
         */
        public Vector3 up() {
            return Vector3.Forward.rotate(rotation);
        }
        /**
         * The right vector of this transform
         * @return Vector3
         */
        public Vector3 right() {
            return Vector3.Right.rotate(rotation);
        }

        /** Rotates this quaternion */
        public void rotate(float x, float y, float z){
            rotation.hamiltonProduct(Quaternion.euler(x, y, z));
        }

        @Override
        public void onDestroy() {

        }
        @Override
        public void onUpdate() {

        }
        @Override
        public void onStart() {

        }
    }

    public static abstract class Component extends ComponentBase {
    }

    /** Handles */
    private @DontSerializeField int uvAttributeHandle, positionAttributeHandle, normalAttributeHandle;
    private @DontSerializeField int matrixHandle, samplerHandle, shinynessHandle, eyeForwardHandle;

    public Mesh mesh;
    public Texture2D uvTexture;
    public String name;

    public @AutoInitiated final Transform transform;


    private @AutoInitiated @DontSerializeField final GlobalLights globalLights;
    public @AutoInitiated final List<WorldObject> children;
    public @AutoInitiated List<Component> components;

    public WorldObject(){
        transform = new Transform();
        globalLights = new GlobalLights();
        mesh = Mesh.load("samples/axis.obj");
        uvTexture = Texture2D.load("cube.png");
        children = new ArrayList<>();
        components = new ArrayList<>();
    }

    public void update(){
        for(Component component : components) component.onUpdate();
    }

    @Override
    public void setupGraphics() {
        transform.setup();

        uvAttributeHandle = GLES20.glGetAttribLocation(program, "uvs");
        positionAttributeHandle = GLES20.glGetAttribLocation(program, "position");
        normalAttributeHandle = GLES20.glGetAttribLocation(program, "vNormals");

        shinynessHandle = GLES20.glGetUniformLocation(program, "shinyness");
        eyeForwardHandle = GLES20.glGetUniformLocation(program, "eyeDirection");
        matrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix");
        samplerHandle = GLES20.glGetUniformLocation(program, "texture");
        globalLights.setup();

        transform.resetBuffer();
    }

    @Override
    public void draw(float[] mvpMatrix) {

        if(mesh == null || isFailure()) return;

        GLES20.glUseProgram(program);

        GLES20.glVertexAttribPointer(positionAttributeHandle, 3, GLES20.GL_FLOAT, false, 0, mesh.vertecies);
        GLES20.glEnableVertexAttribArray(positionAttributeHandle);
        GLES20.glVertexAttribPointer(uvAttributeHandle, 2, GLES20.GL_FLOAT, false, 0, mesh.uvs);
        GLES20.glEnableVertexAttribArray(uvAttributeHandle);
        GLES20.glVertexAttribPointer(normalAttributeHandle, 3, GLES20.GL_FLOAT, false, 0, mesh.normals);
        GLES20.glEnableVertexAttribArray(normalAttributeHandle);

        transform.pass();

        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uvTexture.getId());
        GLES20.glUniform1i(samplerHandle, 0);

        globalLights.pass();
        GLES20.glUniform1f(shinynessHandle, 1f);
        GLES20.glUniform3fv(eyeForwardHandle, 1, cameraForwards, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.size());

        // Draw children
        for(WorldObject child : children){
            child.transform.inherit(transform);
            child.draw(mvpMatrix);
        }

        GLES20.glDisableVertexAttribArray(positionAttributeHandle);
        GLES20.glDisableVertexAttribArray(uvAttributeHandle);
        GLES20.glDisableVertexAttribArray(normalAttributeHandle);

        transform.resetBuffer();
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();
        for(Component cmp : components) cmp.onDestroy();
    }
}
