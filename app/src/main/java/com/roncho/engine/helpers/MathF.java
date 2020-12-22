package com.roncho.engine.helpers;

import com.roncho.engine.structs.primitive.Vector3;

public class MathF {

    public final static float PI = 3.1415926f;
    public final static float Rad2Deg = 180f / PI;
    public final static float Deg2Rad = PI / 180f;

    /**
     * Clamps a value between a and b
     * @param v the value to clamp
     * @param a the minimum value
     * @param b the maximum value
     * @return
     */
    public static float clamp(float v, float a, float b){
        return v > b ? b : Math.max(v, a);
    }

    /**
     * Clamps a value between -1 and 1 [=> clamp(v, -1, 1)]
     * @param v
     * @return
     */
    public static float clampUnit(float v){
        return clamp(v, -1, 1);
    }
    /**
     * Clamps a value between 0 and 1 [=> clamp(v, 0, 1)]
     * @param v
     * @return
     * <code> clamp(v, 0, 1) </code>
     */
    public static float clamp01(float v){
        return clamp(v, 0, 1);
    }

    public static float[] translationMatrix(Vector3 p){
        return new float[] {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                p.x, p.y, p.z, 1
        };
    }

    /**
     * Rounds the float up, and keeps *keepDepth* digits after the dot
     * @param v
     * @param keepDepth
     * @return
     */
    public static float round(float v, int keepDepth){
        int mul = (int)Math.pow(10, keepDepth);
        v *= mul;
        return ((int)v) / mul;
    }

    public static native float min(float a, float b);
    public static native float max(float a, float b);
}
