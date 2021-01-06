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
import com.roncho.engine.helpers.MathF;
import com.roncho.engine.helpers.Screen;
import com.roncho.engine.structs.Texture2D;
import com.roncho.engine.structs.primitive.d2.Rect;
import com.roncho.engine.structs.primitive.d3.Vector3;

import java.util.HashMap;
import java.util.Objects;

public class TextAtlas extends Texture2D {
    private final static String Characters = "1 2 3 4 5 6 7 8 9 0 - = q  w e r t y u i o p [ ] a s d f g h j k l ; ' \\ z x c v b n m , . / ~ ! @ # $ % ^ & * ( ) _ + Q W E R T Y U I O P  { } A S D F G H J K L : | Z X C V B N M < > ? \"";
    private final static HashMap<String, TextAtlas> Cache = new HashMap<>();

    private final HashMap<Character, Glyph> glyphs;

    private final float spaceJump, newlineJump;

    private static class Pair {
        public int start;
        public int charStart;
        public int end;
    }

    public static class Glyph {
        public final Rect rect;
        public final Vector3 scaledSize;

        public Glyph(Rect rect, Vector3 scaledSize){
            this.rect = rect;
            this.scaledSize = scaledSize;
        }
    }

    private TextAtlas (String font, float size){
        super();
        glyphs = new HashMap<>();

        if(size < 8.0f) size = 8;
        else if(size > 500f) size = 500;

        Paint textPainter = new Paint();
        textPainter.setTextSize(size);
        textPainter.setFakeBoldText(false);
        textPainter.setAntiAlias(true);
        FontInfo font_ = AssetHandler.loadFont(font);
        textPainter.setTypeface(font_.getFont());
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
        newlineJump = size / Screen.height() * 2;
        spaceJump = newlineJump;
        for(char c : Characters.toCharArray()){
            if(font_.getEmptyChars().indexOf(c) != -1
                || c == ' ') continue;

            Pair pair = scanCharacter(x, textureWidth, textureHeight, texture);
            int startX = pair.charStart;
            int width = pair.end - pair.charStart;
            if(c == '\"') {
                x = pair.end;
                pair = scanCharacter(x, textureWidth, textureHeight, texture);
                width += pair.end - pair.start;
            }
            x = pair.end;

            float xScale = (width) / size;
            Rect rect = new Rect((float)startX / textureWidth, 0, (float)width / textureWidth, 1);
            Vector3 scaledSize = new Vector3(newlineJump * xScale, newlineJump,
                    newlineJump * MathF.max(width * 2, size * .8f) / size);
            glyphs.put(c, new Glyph(rect, scaledSize));
        }

        texture.recycle();
    }

    private Pair scanCharacter(int x, int tWidth, int tHeight, Bitmap texture){
        Pair pair = new Pair();
        pair.start = x;
        int solidCount = 0;
        while (solidCount <= 0 && x < tWidth){
            solidCount = 0;
            for(int y = 0; y < tHeight; y++){
                if(Color.alpha(texture.getPixel(x, y)) != 0) solidCount++;
            }
            x++;
        }
        pair.charStart = x;
        while (solidCount > 0 && x < tWidth){
            solidCount = 0;
            for(int y = 0; y < tHeight; y++){
                if(Color.alpha(texture.getPixel(x, y)) != 0) solidCount++;
            }
            x++;
        }
        pair.end = x;
        return pair;
    }

    public static TextAtlas loadAtlas(String font){
        return new TextAtlas(font, 24);
    }

    public Glyph getChar(char c){
        if(!glyphs.containsKey(c)) return null;
        return Objects.requireNonNull(glyphs.get(c));
    }

    public float space() { return spaceJump; }
    public float newline() { return newlineJump; }
}
