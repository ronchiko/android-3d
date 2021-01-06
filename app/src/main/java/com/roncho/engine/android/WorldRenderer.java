package com.roncho.engine.android;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.roncho.engine.Engine;
import com.roncho.engine.World;
import com.roncho.engine.gl.objects.Camera;
import com.roncho.engine.gl.Shader;
import com.roncho.engine.gl.objects.GLDrawable;
import com.roncho.engine.gl.objects.UiObject;
import com.roncho.engine.gl.objects.WorldObject;
import com.roncho.engine.helpers.FrameRateLogger;
import com.roncho.engine.helpers.Screen;
import com.roncho.engine.helpers.Time;
import com.roncho.engine.structs.primitive.d2.Int2;
import com.roncho.engine.structs.primitive.d3.Vector3;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class WorldRenderer implements GLSurfaceView.Renderer {

    public static Shader worldVertexShader, worldFragmentShader, uiVertexShader, uiFragmentShader;

    private final Engine engine;
    private final World world;

    private ArrayList<WorldObject> GLDrawables;
    private ArrayList<UiObject> uiObjects;
    public static Camera camera;

    public WorldRenderer(Engine engine) {
        this.engine = engine;

        world = new World(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Time.begin();
        FrameRateLogger.init(60);

        // Initiate layer arrays
        GLDrawables = new ArrayList<>();
        uiObjects = new ArrayList<>();

        // Initiate world lighting
        WorldObject.ambientIntensity = Vector3.One.scale(.2f);
        WorldObject.sunDirection = new Vector3(0, 1.0f, 0.0f);
        WorldObject.diffuseIntensity = Vector3.One.copy();
        WorldObject.specularConstant = new Vector3(1.0f, 1.0f, 1.0f);

        if(worldFragmentShader == null){
            worldFragmentShader =  Shader.load("fragment.frag");
            worldVertexShader = Shader.load("vertex.vert");
            uiVertexShader = Shader.load("ui/vertex.vert");
            uiFragmentShader = Shader.load("ui/fragment.frag");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        boolean shouldInit = Screen.screen == null;

        Screen.screen = new Int2(width, height);

        if(shouldInit) lateInit();
        camera.recalculateProjectionMatrix(width, height);
    }

    private void lateInit(){
        camera = new Camera();

        engine.onLoad(world);
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
            d.update();
            d.draw(mvpMatrix);
        }


        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mvpMatrix = camera.getOrthographicMatrix();
        for(UiObject ui : uiObjects){
            ui.update();
            ui.draw(mvpMatrix);
        }
        GLES20.glDisable(GLES20.GL_BLEND);
        //uio.transform.angle += .1f;

        FrameRateLogger.record();
    }

    public static GLDrawable create(UiObject other){
        UiObject uio = other.clone();
        uio.makeProgram(uiVertexShader, uiFragmentShader);
        return uio;
    }

    public void register(WorldObject o, Shader vert, Shader frag){
        GLDrawables.add(o);
        if(vert == null) vert = worldVertexShader;
        if(frag == null) frag = worldFragmentShader;

        o.makeProgram(vert, frag);
    }
    public void register(UiObject o, Shader vert, Shader frag){
        uiObjects.add(o);

        if(vert == null) vert = uiVertexShader;
        if(frag == null) frag = uiFragmentShader;

        o.makeProgram(vert, frag);
    }
}
