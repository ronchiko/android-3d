package com.roncho.engine.android;

import android.util.Log;

public final class Logger {

    private static final String LOG_TAG = "Engine";

    /**
     * Logs a message
     * @param message
     */
    public static void Log(String message){
        Log.d(LOG_TAG, message);
    }

    /**
     * Logs a warning
     * @param message
     */
    public static void Warn(String message){
        Log.w(LOG_TAG, message);
    }

    /**
     * Logs an error
     * @param message
     */
    public static void Error(String message){
        Log.e(LOG_TAG, message);
    }

    /**
     * Logs an exceeption error
     * @param e
     */
    public static void Exception(Exception e){
        Log.e(LOG_TAG, String.format("Exception %s Thrown: %s", e.getClass().getName(), e.getMessage()));
    }
}
