package com.roncho.engine.structs;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;

import com.roncho.engine.android.AssetHandler;
import com.roncho.engine.android.Logger;
import com.roncho.engine.helpers.Builder;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class Texture2D {
    private enum ImageFormat {
        Unknown(-1),// Unknown image format
        Png(0),    // No shifting required
        Jpg(1);    // Should channel shift

        int id;

        ImageFormat(int id) {
            this.id = id;
        }
    };

    private final static HashMap<String, Texture2D> Cache = new HashMap<>();

    protected int textureId, width, height;
    private String path;

    protected Texture2D(){
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        int[] tid = new int[1];
        GLES20.glGenTextures(1, tid, 0);
        textureId = tid[0];

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tid[0]);
    }

    private Texture2D(String path, Bitmap bmp, ImageFormat format){
        width = bmp.getWidth();
        height = bmp.getHeight();

        // Convert the image to a byte buffer
        ByteBuffer buffer = Builder.makeByteBuffer(width * height * 4);

        byte[] array = new byte[width * height * 4];

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++){
                int pixel = bmp.getPixel(x, y);

                byte r = (byte)Color.red(pixel);
                byte g = (byte)Color.green(pixel);
                byte b = (byte)Color.blue(pixel);
                byte a = (byte)Color.alpha(pixel);

                array[(x + y * width) * 4] = r;
                array[(x + y * width) * 4 + 1] = g;
                array[(x + y * width) * 4 + 2] = b;
                array[(x + y * width) * 4 + 3] = a;
            }
        }

        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);

        int[] tid = new int[1];
        GLES20.glGenTextures(1, tid, 0);
        textureId = tid[0];

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tid[0]);
        // Put to buffer in reverse order
        for(int i = array.length - 1; i >= 0; i -= 4)
        {
            buffer.put(array[i - 3]);
            buffer.put(array[i - 2]);
            buffer.put(array[i - 1]);
            buffer.put(array[i]);
        }
        buffer.position(0);


        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        Cache.put(path, this);
        this.path = path;
    }

    public static Texture2D load(String path){
        Bitmap bmp = AssetHandler.loadImage("textures/" + path);
        if(Cache.containsKey(path)) return Cache.get(path);
        Logger.Log("Loading image: " + path);
        return new Texture2D(path, bmp, getFormatFromPath(path));
    }

    public static Texture2D load(int drawableId){
        Bitmap bmp = AssetHandler.loadDrawable(drawableId);
        String path = "drawables/" + drawableId + ".drawable";
        if(Cache.containsKey(path)) return Cache.get(path);
        return new Texture2D(path, bmp, ImageFormat.Png);
    }

    public int getId() {return textureId;}

    private static ImageFormat getFormatFromPath(String pth){
        int index = pth.indexOf('.');
        String ext = pth.substring(index + 1);
        switch (ext){
            case "jpg": case "jpeg": return ImageFormat.Jpg;
            case "png": return ImageFormat.Png;
            default: return ImageFormat.Unknown;
        }
    }

    @Override
    public String toString(){
        return "texture(" + path + ")";
    }
}
