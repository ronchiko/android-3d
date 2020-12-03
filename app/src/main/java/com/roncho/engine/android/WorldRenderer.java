package com.roncho.engine.android;

import android.content.QuickViewConstants;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.roncho.engine.ObjectFactory;
import com.roncho.engine.gl.objects.Camera;
import com.roncho.engine.gl.Shader;
import com.roncho.engine.gl.objects.GLDrawable;
import com.roncho.engine.gl.objects.UIObject;
import com.roncho.engine.gl.objects.WorldObject;
import com.roncho.engine.structs.Mesh;
import com.roncho.engine.structs.primitive.Color;
import com.roncho.engine.structs.primitive.Vector2;
import com.roncho.engine.structs.primitive.Vector3;
import com.roncho.engine.templates.UiText;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class WorldRenderer implements GLSurfaceView.Renderer {

    private static Shader worldVertexShader, worldFragmentShader, uiVertexShader, uiFragmentShader;

    private ArrayList<WorldObject> GLDrawables;
    private ArrayList<UIObject> uio;
    public static Camera camera;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLDrawables = new ArrayList<>();

        WorldObject.ambientIntensity = Vector3.One.scale(.2f);
        WorldObject.sunDirection = new Vector3(0, 1.0f, 0.0f);
        WorldObject.diffuseIntensity = Vector3.One.copy();
        WorldObject.specularConstant = new Vector3(1.0f, 1.0f, 1.0f);

        worldFragmentShader =  Shader.load("fragment.frag");
        worldVertexShader = Shader.load("vertex.vert");
        uiVertexShader = Shader.load("ui/vertex.vert");
        uiFragmentShader = Shader.load("ui/fragment.frag");

        camera = new Camera();

        WorldObject x = new WorldObject();
        x.makeProgram(worldVertexShader, worldFragmentShader);
        WorldObject y = new WorldObject();
        y.makeProgram(worldVertexShader, worldFragmentShader);
        y.mesh = Mesh.load("samples/cube.obj");
        y.transform.scale = Vector3.One.scale(1.2f);

        x.children.add(y);
        GLDrawables.add(x);
        //GLDrawables.add(y);

        uio = new ArrayList<>();
        UIObject h = new UIObject(); //new UiText("ADDECRG.TTF", "Z");
        h.makeProgram(uiVertexShader, uiFragmentShader);
        uio.add(h);
        //UIObject v = (UIObject) create(uio.get(0));
        /*v.transform.position = new Vector2(1, 1f);
        uio.add(v);*/

        // ObjectFactory.loadObject(AssetHandler.loadText("data/objects/demo.x"));
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        camera.recalculateProjectionMatrix(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if(worldVertexShader.isFailure() || worldFragmentShader.isFailure()) return;

        GLES20.glClearColor(0f, 1f, 1f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] mvpMatrix = camera.getMvpMatrix();

        // camera.transform.rotation.rotate(1f, 0, 0, 1);
        WorldObject.cameraForwards = camera.transform.forwards().toArray();
        // uio.draw(mvpMatrix);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        for(WorldObject d : GLDrawables){
            d.draw(mvpMatrix);
            d.transform.rotation.rotate(.2f, 1, 1, 1);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mvpMatrix = camera.getOrthographicMatrix();
        for(UIObject ui : uio){
            ui.draw(mvpMatrix);
        }
        GLES20.glDisable(GLES20.GL_BLEND);
        //uio.transform.angle += .1f;
    }

    private native void passPrivateComponents(float[] cameraForwards);

    public static GLDrawable create(UIObject other){
        UIObject uio = other.clone();
        uio.makeProgram(uiVertexShader, uiFragmentShader);
       return uio;
    }
}
