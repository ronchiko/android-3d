package com.roncho.engine.helpers;

import com.roncho.engine.android.Logger;

public final class FrameRateLogger {

    private static final float ALERT_THRESHOLD = .25f;
    private static final float ALERT_MIN_DIP = 15;

    private static float[] fps;
    private static int frame = 0;

    private static float averageFps;
    private static float highestFps;

    public static void init() { init(60); }
    public static void init(int bufferSize){
        fps = new float[bufferSize];
        for(int i = 0; i < bufferSize; i++)
            fps[i] = 45;
        frame = 0;
    }

    public static void record(){
        if(frame >= fps.length) {
            averageFps = flush();
        }
        fps[frame++] = 1f / Time.deltaTime();
    }

    private static float flush(){
        float avg = 0;
        highestFps = 5000;
        for(int i = 0; i < fps.length; i++){
            highestFps = MathF.min(highestFps, fps[i]);
            avg += fps[i];
            fps[i] = 0;
        }

        if(highestFps < averageFps * ALERT_THRESHOLD
            || highestFps <= ALERT_MIN_DIP){
            Logger.Warn("Fps Drop: " + highestFps + " fps: " + ((int)((1 - highestFps / averageFps) * 100)) + "% less then the average.");
        }
        frame = 0;
        return avg / fps.length;
    }
}
