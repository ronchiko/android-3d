package com.roncho.engine.gl.objects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.roncho.engine.gl.text.TextAtlas;
import com.roncho.engine.helpers.Builder;
import com.roncho.engine.helpers.MathF;
import com.roncho.engine.structs.ComponentBase;
import com.roncho.engine.structs.Mesh;
import com.roncho.engine.structs.Texture2D;
import com.roncho.engine.structs.primitive.Color;
import com.roncho.engine.structs.primitive.Rect;
import com.roncho.engine.structs.primitive.Vector2;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class UIObject extends GLDrawable {


    protected int positionAttributeHandle, uvHandle, textureHandle, tintHandle;

    public class Transform {
        private int positionHandle;
        private int scaleHandle;
        private int angleHandle;

        public Vector2 position;
        public Vector2 scale;
        public float angle;

        private final float[] buffer;

        public Transform()
        {
            scale = new Vector2(1, .1f);
            position = new Vector2(0, 0);
            buffer = new float[5];
        }

        private void setupHandles(){
            positionHandle = GLES20.glGetUniformLocation(program, "transform.position");
            scaleHandle = GLES20.glGetUniformLocation(program, "transform.scale");
            angleHandle = GLES20.glGetUniformLocation(program, "transform.angle");
        }

        private void pass(){
            GLES20.glUniform2fv(positionHandle, 1, buffer, 0);
            GLES20.glUniform2fv(scaleHandle, 1, buffer, 2);
            GLES20.glUniform1f(angleHandle, buffer[4] * MathF.Deg2Rad);
        }

        private void inherit(Transform parent){
            buffer[0] += parent.position.x;
            buffer[1] += parent.position.y;
            buffer[2] *= parent.scale.x;
            buffer[3] *= parent.scale.y;
            buffer[4] += parent.angle;
        }

        private void resetBuffer(){
            buffer[0] = position.x;
            buffer[1] = position.y;
            buffer[2] = scale.x;
            buffer[3] = scale.y;
            buffer[4] = angle;
        }
    }

    protected abstract class Component extends ComponentBase { }

    private static FloatBuffer quad, uvs;

    public Transform transform;
    public Texture2D texture;
    public Color tint;

    public List<UIObject> children;

    private final List<Component> components;

    public UIObject(){

        TextAtlas atlas = TextAtlas.loadAtlas("ADDECRG.TTF");

        uvs = Builder.makeRectBuffer(atlas.getChar('Z'));

        // Prepare the static quad
        if(quad == null) {
            quad = Builder.newFloatBuffer(8); //uvs = Builder.newFloatBuffer(8);
            quad.put(-1f);  //uvs.put(0);
            quad.put(1f);   //uvs.put(0);
            quad.put(1f);   //uvs.put(1);
            quad.put(1f);   //uvs.put(0);
            quad.put(-1f); // uvs.put(0);
            quad.put(-1f);  //uvs.put(1);
            quad.put(1f);   //uvs.put(1);
            quad.put(-1f);  //uvs.put(1);
            quad.position(0); //uvs.position(0);
        }

        transform = new Transform();
        texture = atlas; //Texture2D.load("pane.png");
        tint = Color.white();
        children = new ArrayList<>();
        transform.resetBuffer();
        components = new ArrayList<>();
    }

    @Override
    public void setupGraphics() {
        positionAttributeHandle = GLES20.glGetAttribLocation(program, "position");
        uvHandle = GLES20.glGetAttribLocation(program, "uv");
        textureHandle = GLES20.glGetUniformLocation(program, "texture");
        tintHandle = GLES20.glGetUniformLocation(program, "tint");
        transform.setupHandles();
    }

    public void addComponent(Component c){
        components.add(c);
        c.onStart();
    }

    @Override
    public void draw(float[] mvpMatrix) {
        passShader();

        GLES20.glEnableVertexAttribArray(positionAttributeHandle);
        GLES20.glVertexAttribPointer(positionAttributeHandle, 2, GLES20.GL_FLOAT, false, 0, quad);
        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, uvs);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionAttributeHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);

        closeShader(mvpMatrix);
    }

    protected void passShader(){
        for(Component component : components) component.onUpdate();

        GLES20.glUseProgram(program);
        transform.pass();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getId());
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glUniform4fv(tintHandle, 1, tint.asArray(), 0);
    }

    protected void closeShader(float[] mvpMatrix) {
        for(UIObject object : children) {
            object.transform.inherit(transform);
            object.draw(mvpMatrix);
        }

        transform.resetBuffer();
    }

    public UIObject clone(){
        UIObject n = new UIObject();
        // Property cloning
        n.transform.position = transform.position.copy();
        n.transform.scale = transform.scale.copy();
        n.transform.angle = transform.angle;
        n.texture = texture;
        n.tint = tint.copy();

        // Preform depth cloning
        for(UIObject obj : children){
            n.children.add(obj.clone());
        }

        for(Component c : components){
            n.addComponent(c);
        }

        n.program = program;
        n.transform.setupHandles();

        return n;
    }
}
