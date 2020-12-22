package com.roncho.engine.android;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.roncho.engine.gl.objects.Camera;
import com.roncho.engine.gl.Shader;
import com.roncho.engine.gl.objects.GLDrawable;
import com.roncho.engine.gl.objects.UiObject;
import com.roncho.engine.gl.objects.WorldObject;
import com.roncho.engine.helpers.FrameRateLogger;
import com.roncho.engine.helpers.Screen;
import com.roncho.engine.helpers.Time;
import com.roncho.engine.structs.Mesh;
import com.roncho.engine.structs.primitive.Color;
import com.roncho.engine.structs.primitive.Int2;
import com.roncho.engine.structs.primitive.Vector2;
import com.roncho.engine.structs.primitive.Vector3;
import com.roncho.engine.templates.ui.FpsText;
import com.roncho.engine.templates.ui.UiButton;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class WorldRenderer implements GLSurfaceView.Renderer {

    private static Shader worldVertexShader, worldFragmentShader, uiVertexShader, uiFragmentShader;

    private ArrayList<WorldObject> GLDrawables;
    private ArrayList<UiObject> uio;
    public static Camera camera;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Time.begin();
        FrameRateLogger.init(60);

        GLDrawables = new ArrayList<>();

        WorldObject.ambientIntensity = Vector3.One.scale(.2f);
        WorldObject.sunDirection = new Vector3(0, 1.0f, 0.0f);
        WorldObject.diffuseIntensity = Vector3.One.copy();
        WorldObject.specularConstant = new Vector3(1.0f, 1.0f, 1.0f);

        worldFragmentShader =  Shader.load("fragment.frag");
        worldVertexShader = Shader.load("vertex.vert");
        uiVertexShader = Shader.load("ui/vertex.vert");
        uiFragmentShader = Shader.load("ui/fragment.frag");


        //UIObject v = (UIObject) create(uio.get(0));
        /*v.transform.position = new Vector2(1, 1f);
        uio.add(v);*/

        // ObjectFactory.loadObject(AssetHandler.loadText("data/objects/demo.x"));
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        boolean shouldInit = Screen.screen == null;
        Screen.screen = new Int2(width, height);
        setup(width, height);
        if(shouldInit) lateInit();
        camera.recalculateProjectionMatrix(width, height);
    }

    private void lateInit(){
        camera = new Camera();

        WorldObject x = new WorldObject();
        x.makeProgram(worldVertexShader, worldFragmentShader);
        WorldObject y = new WorldObject();
        y.makeProgram(worldVertexShader, worldFragmentShader);
        y.mesh = Mesh.load("samples/cube.obj");
        y.transform.scale = Vector3.One.scale(1.2f);

        GLDrawables.add(y);
        GLDrawables.add(x);
        //GLDrawables.add(y);

        uio = new ArrayList<>();
        UiObject fpsRecorder = new FpsText("impact.ttf", new Vector2(-.9f, .9f));
        fpsRecorder.tint = Color.black();
        //h.makeProgram(uiVertexShader, uiFragmentShader);
        uio.add(fpsRecorder);
        UiButton button = new UiButton(new Vector2(0, 0), new Vector2(.5f, .25f), "Epic Fortnite", "impact.ttf");
        button.text.transform.scale = button.text.transform.scale.scale(2);
        button.recenterText();
        uio.add(button);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if(worldVertexShader.isFailure() || worldFragmentShader.isFailure()) return;

        Time.update();

        GLES20.glClearColor(0f, 1f, 1f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] mvpMatrix = camera.getMvpMatrix();

        // camera.transform.rotation.rotate(1f, 0, 0, 1);
        WorldObject.cameraForwards = camera.transform.forwards().toArray();
        // uio.draw(mvpMatrix);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        for(WorldObject d : GLDrawables){
            d.draw(mvpMatrix);
            d.transform.rotation.rotate(15 * Time.deltaTime(), 1, 1, 1);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mvpMatrix = camera.getOrthographicMatrix();
        for(UiObject ui : uio){
            ui.draw(mvpMatrix);
        }
        GLES20.glDisable(GLES20.GL_BLEND);
        //uio.transform.angle += .1f;

        FrameRateLogger.record();
    }

    private native void passPrivateComponents(float[] cameraForwards);
    private native void setup(int width, int height);

    public static GLDrawable create(UiObject other){
        UiObject uio = other.clone();
        uio.makeProgram(uiVertexShader, uiFragmentShader);
        return uio;
    }
}
