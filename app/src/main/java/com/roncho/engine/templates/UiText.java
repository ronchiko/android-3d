package com.roncho.engine.templates;

import android.opengl.GLES20;

import com.roncho.engine.gl.objects.UIObject;
import com.roncho.engine.gl.text.TextAtlas;

public class UiText extends UIObject {

    public class TextComponent {
        private TextAtlas atlas;
        private String text;

        public TextComponent(String font, String text){
            atlas = TextAtlas.loadAtlas(font);
            this.text = text;
        }
    }

    public TextComponent text;

    public UiText(String font, String text){
        super();
        this.text = new TextComponent(font, text);
    }

    @Override
    public void draw(float[] mvpMatrix) {
        passShader();
        drawUnit(mvpMatrix);
        closeShader(mvpMatrix);
    }

    private native void drawUnit(float[] mvpMatrix);
}
