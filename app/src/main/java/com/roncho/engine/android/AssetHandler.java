package com.roncho.engine.android;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import com.roncho.engine.gl.text.FontInfo;

import java.io.IOException;
import java.io.InputStream;

public class AssetHandler {
    private static AssetManager assets;
    private static Context context;

    public static final String SHADERS_PATH = "shaders";
    public static final String MODELS_PATH = "models";
    public static final String FONTS_PATH = "fonts/";

    public static void init(AssetManager assets,
                            Context context){
        AssetHandler.assets = assets;
        AssetHandler.context = context;
    }

    /**
     * Reads a text asset
     * @param path
     * @return
     */
    public static String loadText(String path){
        if(path == null){
            return "";
        }
        try(InputStream file = assets.open(path)){
            StringBuilder sb = new StringBuilder();
            int v;
            while((v = file.read()) >= 0){
                sb.append((char)v);
            }
            return sb.toString();
        }catch (IOException e){
            Logger.Exception(e);
            return null;
        }
    }

    public static InputStream loadRaw(String path){
        if(path == null) return null;

        try {
            return assets.open(path);
        }catch (IOException e){
            Logger.Exception(e);
            return null;
        }
    }

    /**
     * Loads an image from the assets
     * @param path path to the image
     * @return the image as a bitmap
     */
    public static Bitmap loadImage(String path){
        if(path == null) return null;

        try(InputStream stream = assets.open(path)){
            return BitmapFactory.decodeStream(stream);
        }catch (IOException e){
            Logger.Exception(e);
            return null;
        }
    }

    public static Bitmap loadDrawable(int drawableId){
        return BitmapFactory.decodeResource(context.getResources(), drawableId);
    }

    /**
     * Loads a font from the assets
     * @param path
     * @return
     */
    public static FontInfo loadFont(String path){
        Typeface font = Typeface.createFromAsset(assets, FONTS_PATH + path);
        String echars = loadText(FONTS_PATH + path + ".DT");
        return new FontInfo(font, echars);
    }
}
