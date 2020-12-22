package com.roncho.engine.templates.ui;

import android.opengl.GLES20;

import com.roncho.engine.gl.Shader;
import com.roncho.engine.gl.objects.UiObject;
import com.roncho.engine.gl.text.TextAtlas;
import com.roncho.engine.helpers.Builder;
import com.roncho.engine.helpers.MathF;
import com.roncho.engine.structs.primitive.Vector2;

import java.nio.FloatBuffer;

public class UiText extends UiObject {

    public static final byte F_DONT_CALCULATE_SIZE = 1;

    protected final static Shader vertexShader = Shader.load("ui/text_vertex.vert");

    public class TextComponent {

        private TextAtlas atlas;
        private String text;

        public final Vector2 size;

        public TextComponent(String font, String text){
            atlas = TextAtlas.loadAtlas(font);
            texture = atlas;
            size = Vector2.Zero.copy();
            setText(text);
        }

        public boolean loadAtlas(String font){
            atlas = TextAtlas.loadAtlas(font);
            texture = atlas;
            return true;
        }

        public void setText(String text) {
            this.text = text;

            // Check if should recalculate the text coordinates
            if(hasFlag(F_DONT_CALCULATE_SIZE)) return;

            size.x = 0;
            size.y = 0;
            float width = 0;
            for(char c : text.toCharArray()){
                switch (c) {
                    case ' ':
                        width += atlas.space();
                        break;
                    case '\n':
                        size.x = MathF.max(width, size.x);
                        size.y -= atlas.newline() * 1.5f;
                        break;
                    default:
                        TextAtlas.Glyph glyph = atlas.getChar(c);
                        if(glyph == null){
                            break;
                        }

                        width += glyph.scaledSize.z;
                        break;
                }
            }
            size.x = MathF.max(width, size.x);
        }
        public String getText() { return text; }
    }

    public TextComponent text;

    protected short flags;
    private int internalPositionHandle, internalScaleHandle, internalAngleHandle, matrixHandle;

    public UiText(String font, String text){
        super();
        flags = 0;

        this.text = new TextComponent(font, text);

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

    public boolean hasFlag(byte flag){
        return (flag & flags) != 0;
    }

    public void enable(byte flag){
        flags |= flag;
    }

    public void disable(byte flag){
        flags &= ~flag;
    }
}
