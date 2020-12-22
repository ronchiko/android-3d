package com.roncho.engine.templates;

import android.opengl.GLES20;

import com.roncho.engine.android.AssetHandler;
import com.roncho.engine.gl.Shader;
import com.roncho.engine.gl.objects.UIObject;
import com.roncho.engine.gl.text.TextAtlas;
import com.roncho.engine.helpers.Builder;

import java.nio.FloatBuffer;

public class UiText extends UIObject {

    private static Shader vertexShader, fragmentShader;

    private static void loadShaders(){
        vertexShader = Shader.load("ui/text_vertex.vert");
        fragmentShader = Shader.load("ui/fragment.frag");
    }

    public class TextComponent {
        private TextAtlas atlas;
        private String text;

        public TextComponent(String font, String text){
            atlas = TextAtlas.loadAtlas(font);
            this.text = text;
            texture = atlas;
        }
    }

    public TextComponent text;

    private int internalPositionHandle, internalScaleHandle, internalAngleHandle, matrixHandle;

    public UiText(String font, String text){
        super();
        this.text = new TextComponent(font, text);

        if(vertexShader == null || fragmentShader == null) loadShaders();
        makeProgram(vertexShader, fragmentShader);
    }

    @Override
    public void setupGraphics() {
        super.setupGraphics();
        internalPositionHandle = GLES20.glGetUniformLocation(program, "internal.position");
        internalScaleHandle = GLES20.glGetUniformLocation(program, "internal.scale");
        internalAngleHandle = GLES20.glGetUniformLocation(program, "internal.angle");
    }

    @Override
    public void draw(float[] mvpMatrix) {
        passShader();

        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glEnableVertexAttribArray(positionAttributeHandle);

        float[] internalTransform = {
                0, 0, .01f, .01f, 0
        };

        for(char c : text.text.toCharArray()){
            switch (c) {
                case ' ':
                    internalTransform[0] += text.atlas.space();
                    break;
                case '\n':
                    internalTransform[0] = 0;
                    internalTransform[1] -= text.atlas.newline() * 1.5f;
                    break;
                default:
                    TextAtlas.Glyph glyph = text.atlas.getChar(c);
                    if(glyph == null){
                        break;
                    }
                    FloatBuffer buffer = Builder.makeRectBuffer(glyph.rect);
                    internalTransform[2] = glyph.scaledSize.x;
                    internalTransform[3] = glyph.scaledSize.y;

                    GLES20.glUniform2fv(internalPositionHandle, 1, internalTransform, 0);
                    GLES20.glUniform2fv(internalScaleHandle, 1, internalTransform, 2);
                    GLES20.glUniform1fv(internalAngleHandle, 1, internalTransform, 4);

                    GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, buffer);
                    GLES20.glVertexAttribPointer(positionAttributeHandle, 2, GLES20.GL_FLOAT, false, 0, quad);

                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

                    internalTransform[0] += glyph.scaledSize.z;
                    break;
            }
        }

        closeShader(mvpMatrix);
    }

    private native void drawUnit(float[] mvpMatrix);
}
