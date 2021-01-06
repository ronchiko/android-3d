package com.roncho.engine.audio;

import android.content.res.AssetManager;

import com.roncho.engine.android.AssetHandler;

import java.io.FileDescriptor;

public class AudioEngine {

    public static void init(int streams){
        start(streams);
    }

    private static native void start(int maxStreams);
    public static native void shutdown();
    private static native int load(byte[] wav, int size);

    static native int play(int id, float volume);
    static int loadFile(String file){
        byte[] raw = AssetHandler.loadBytes(file);
        if(raw != null) {
            return load(raw, raw.length);
        }
        return 0;
    }
}
