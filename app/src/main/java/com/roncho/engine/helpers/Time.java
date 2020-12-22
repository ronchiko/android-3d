package com.roncho.engine.helpers;

public class Time {

    private static long _startTime;

    private static long _startMillis;
    private static float _deltaTime;

    public static void begin(){
        _startTime = System.currentTimeMillis();
        _startMillis = _startTime;
    }

    public static void update(){
        long _endMillis = System.currentTimeMillis();
        _deltaTime = (_endMillis - _startMillis) / 1000f;
        _startMillis = _endMillis;
    }

    // The time that passed between until this frame was rendered
    public static float deltaTime() { return _deltaTime; }
    public static long startTime() { return _startTime; }
}
