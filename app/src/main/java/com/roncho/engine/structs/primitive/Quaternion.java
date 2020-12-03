package com.roncho.engine.structs.primitive;

import android.opengl.GLES20;

import com.roncho.engine.helpers.MathF;

public class Quaternion {
    // Where w + xi + yj + zk
    public float w, x, y, z;

    public Quaternion(float w, float x, float y, float z, boolean normalize){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        if(normalize){
            float mg = magnitude();
            this.x /= mg;
            this.y /= mg;
            this.z /= mg;
            this.w /= mg;
        }
    }
    public Quaternion(float w, float x, float y, float z) {this(w, x, y, z, false);}
    public Quaternion(Quaternion o){this(o.w, o.x, o.y, o.z);}
    public Quaternion() {this(1,0,0,0);}
    public static Quaternion identity(){
        return new Quaternion(1, 0, 0, 0);
    }

    /**
     * Makes a quaternion from eular angles
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Quaternion euler(float x, float y, float z){
        x *= MathF.Deg2Rad;
        y *= MathF.Deg2Rad;
        z *= MathF.Deg2Rad;

        float cy = (float)Math.cos(y * .5f);
        float sy = (float)Math.sin(y * .5f);
        float cx = (float)Math.cos(x * .5f);
        float sx = (float)Math.sin(x * .5f);
        float cz = (float)Math.cos(z * .5f);
        float sz = (float)Math.sin(z * .5f);

        return new Quaternion(
                cx *cy * cz + sx * sy * sz,
                sx * cy * cz - cx * sy * sz,
                cx * sy * cz + sx * cy * sz,
                cx * cy * sz - sx * sy * cz,
                true
        );
    }

    /**
     * The euler angles of this quaternion
     * @return
     */
    public Vector3 euler() {
        Vector3 euler = Vector3.Zero;

        float sinXCosY = 2 * (w * x + y * z);
        float cosXCosY = 1 - 2 * (x * x + y * y);

        euler.x = (float)Math.atan2(sinXCosY, cosXCosY);

        float sinY = 2 * (w * y - x * z);
        if(Math.abs(sinY) >= 1) euler.y = (float)((Math.PI / 2) * Math.signum(sinY));
        else euler.y = (float)Math.asin(sinY) ;

        float sinZCosY = 2 * (w * z + y * x);
        float cosZCosY = 1 - 2 * (z * z + y * y);
        euler.z = (float)Math.atan2(sinZCosY, cosZCosY);

        return euler.scale(MathF.Rad2Deg);
    }

    /**
     * The magnitude of the quaternion
     * @return
     */
    public float magnitude(){
        return (float)Math.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * Returns the hamilton product of 2 quaternions
     * @param o
     * @return
     */
    public Quaternion hamiltonProduct(Quaternion o){
        return new Quaternion(
                w * o.w - x * o.x - y * o.y - z * o.z,
                w * o.x + x * o.w + y * o.z - z * o.y,
                w * o.y - x * o.z + y * o.w + z * o.x,
                w * o.z + x * o.y - y * o.x + z * o.w
        );
    }

    /**
     * The conjugate of this quaternion
     * @return
     */
    public Quaternion conjugate(){
        return new Quaternion(w, -x, -y, -z);
    }

    /**
     * The invert of this quaternion
     * @return
     */
    public Quaternion invert(){
        return conjugate().scale(1f / (x * x + y * y + z * z + w * w));
    }

    /**
     * Returns the scaled version of this quaterion
     * @param t
     * @return
     */
    public Quaternion scale(float t){
        return new Quaternion(t * x, t * y, t * z, t * w);
    }

    /**
     * Returns a normalized version of this quaternion
     * @return
     */
    public Quaternion normalize() {
        float mag = magnitude();
        return new Quaternion(w / mag, x / mag, y / mag, z/ mag);
    }

    public float[] asMatrix4x4(){
        // Do on normalized quaternion
        if(magnitude() != 1) return normalize().asMatrix4x4();
        float a = w * w, b = x * x, c = y *y, d = z * z;

        return new float[] {
                2 * (w * w + x * x) - 1, 2 * x * y - 2 * w * z, 2 * x * z + 2 * w * y, 0,
                2 * x * y + 2 * w * z, 2 * (w * w + y * y) - 1, 2 * y * z - 2 * x * w, 0,
                2 * x * z - 2 * w * y, 2 * y * z + 2 * x * w, 2 * (w * w + z * z) - 1, 0,
                0,0,0,1
        };
    }

    public static Quaternion fromAxisAngle(float angle, float x, float y, float z){
        float mag = (float)Math.sqrt(x * x + y * y + z * z); if(mag == 0) mag = 1;
        x /= mag;
        y /= mag;
        z /= mag;
        float sinA = (float)Math.sin(angle * MathF.Deg2Rad / 2f);

        return new Quaternion((float)Math.cos(angle * MathF.Deg2Rad / 2), x * sinA, y * sinA, z * sinA).normalize();
    }

    public void rotate(float angle, float x, float y, float z){
        // Normalize a head
        float mag = (float)Math.sqrt(x * x + y * y + z * z); if(mag == 0) mag = 1;
        x /= mag;
        y /= mag;
        z /= mag;

        angle = ((float) Math.acos(w) * 2 / MathF.Deg2Rad + angle) % 360;
        float sinA = (float)Math.sin(angle * MathF.Deg2Rad / 2f);

        this.x = x * sinA;
        this.y = y * sinA;
        this.z = z * sinA;
        this.w = (float)Math.cos(angle * MathF.Deg2Rad / 2);

        // Normalize after
        mag = magnitude(); if(mag == 0) mag = 1;
        this.x /= mag;
        this.y /= mag;
        this.z /= mag;
        this.w /= mag;
    }

    public float[] toArray(){
        // Only work with normalized quaternion
        if(MathF.round(magnitude(), 6) != 1) return normalize().toArray();
        return new float[] {x, y, z, w};
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ", " + w + ")";
    }
    public static native Quaternion parse(String str);


    public static void multiplyQQ(float[] o, int i3, float[] a, int i1, float[] b, int i2){
        final float x = a[i1], y = a[i1 + 1], z = a[i1 + 2], w = a[i1 + 3];
        final float bx = -b[i2], by = -b[i2 + 1], bz = -b[i2 + 2], bw = a[i2 + 3];
        o[i3] = w * bw - x * bx - y * by - z * bz;
        o[i3 + 1] = w * bx + x * bw + y * bz - z * by;
        o[i3 + 2] = w * by - x * bz + y * bw + z * bx;
        o[i3 + 3] = w * bz + x * by - y * bx + z * bw;
    }
    public static void identityQ(float[] a, int i1){
        a[i1] = 0;
        a[i1 + 1] = 0;
        a[i1 + 2] = 0;
        a[i1 + 3] = 1;
    }

    public static void rotateVQ(float[] o, int i1, float[] v, int i2, float[] q, int i3){
        final float x = v[i2], y = v[i2 + 1], z = v[i2 + 2];

        final float dotQV = q[i3] * x + q[i3 + 1] * y + q[i3 + 2] * z, dotQQ = q[i3] * q[i3] + q[i3 + 1] * q[i3 + 1] + q[i3 + 2] * q[i3 + 2];
        final float w = q[i3 + 3], s = w * w - dotQQ;

        o[i1] = 2 * q[i3] * dotQV + s * x + 2 * w * (q[i3 + 1] * z - q[i3 + 2] * y);
        o[i1 + 1] = 2 * q[i3 + 1] * dotQV + s * y + 2 * w * (q[i3 + 2] * x - q[i3] * z);
        o[i1 + 2] = 2 * q[i3 + 2] * dotQV + s * y + 2 * w * (q[i3] * y - q[i3 + 1] * x);
    }
}
