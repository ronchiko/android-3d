package com.roncho.engine.audio;

public class AudioStreamPlayer {

    private final int stream;

    AudioStreamPlayer(int stream){
        this.stream = stream;
    }

    public native void setVolume(float volume);
    public native void resume();
    public native void stop();
}
