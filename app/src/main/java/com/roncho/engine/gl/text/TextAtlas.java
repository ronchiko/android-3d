package com.roncho.engine.gl.text;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.roncho.engine.android.AssetHandler;
import com.roncho.engine.structs.Texture2D;
import com.roncho.engine.structs.primitive.Rect;

import java.util.HashMap;
import java.util.Objects;

public class TextAtlas extends Texture2D {
    private final static String Characters = "`1234567890-=qwertyuiop[]asdfghjkl;'\\zxcvbnm,./~!@#$%^&*()_+QWERTYUIOP{}ASDFGHJKL:\"|ZXCVBNM<>?";
    private final static HashMap<String, TextAtlas> Cache = new HashMap<>();

    private final HashMap<Character, Rect> glyphs;

    private TextAtlas (String font, float size){
        super();
        glyphs = new HashMap<>();

        if(size < 8.0f) size = 8;
        else if(size > 500f) size = 500;

        Paint textPainter = new Paint();
        textPainter.setTextSize(size);
        textPainter.setFakeBoldText(false);
        textPainter.setAntiAlias(true);
        textPainter.setTypeface(AssetHandler.loadFont(font));
        textPainter.setARGB(255, 255, 255, 255);

        textPainter.setSubpixelText(true);
        textPainter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));

        float textWidth = textPainter.measureText(Characters);

        int textureWidth = (int)(textWidth + 2);
        int textureHeight = (int)size + 2;

        Bitmap texture = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);
        texture.eraseColor(Color.argb(0, 255, 255, 255));

        Canvas canvas = new Canvas(texture);
        canvas.drawText(Characters, 1, 1 + size * .75f, textPainter);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, texture, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        int x = 0;
        for(char c : Characters.toCharArray()){
            int solidCount = 0;
            while (solidCount <= 0 && x < textureWidth){
                solidCount = 0;
                for(int y = 0; y < textureHeight; y++){
                    if(Color.alpha(texture.getPixel(x, y)) != 0) solidCount++;
                }
                x++;
            }
            int sx = x;
            while (solidCount > 0 && x < textureWidth){
                solidCount = 0;
                for(int y = 0; y < textureHeight; y++){
                    if(Color.alpha(texture.getPixel(x, y)) != 0) solidCount++;
                }
                x++;
            }
            int width = x - sx;
            glyphs.put(c, new Rect((float)sx / textureWidth, 0, (float)width / textureWidth, 1));
        }

        texture.recycle();
    }


    public static TextAtlas loadAtlas(String font){
        return new TextAtlas(font, 24);
    }

    public float[] getChar(char c){
        if(!glyphs.containsKey(c)) return null;
        return Objects.requireNonNull(glyphs.get(c)).toArray();
    }
}
