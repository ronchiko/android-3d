package com.roncho.engine.structs.primitive.d2;

import com.roncho.engine.structs.primitive.d3.Vector3;

public class Vector2 {
    public static final int ARRAY_SIZE = 2;

    public float x, y;

    public static final Vector2 One = new Vector2(1, 1);
    public static final Vector2 Zero = new Vector2(0, 0);
    public static final Vector2 Right  = new Vector2(1, 0);
    public static final Vector2 Left  = new Vector2(-1, 0);
    public static final Vector2 Up  = new Vector2(0, 1);
    public static final Vector2 Down  = new Vector2(0, -1);

    /**
     * Constructor
     * @param x
     * @param y
     */
    public Vector2(float x, float y){
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor
     * @param v
     */
    public Vector2(Vector2 v){
        this(v.x, v.y);
    }

    public Vector2() {this(0, 0);}

    /**
     * Adds 2 vectors
     * @param v
     * @return
     */
    public Vector2 add(Vector2 v){
        return new Vector2(x + v.x, y + v.y);
    }

    /**
     * Negates the value of the vector
     * @return
     */
    public Vector2 negate(){
        return new Vector2(-x, -y);
    }

    /**
     * Subtracts a vector from this one
     * @param v
     * @return
     */
    public Vector2 sub(Vector2 v){
        return new Vector2(x - v.x, y - v.y);
    }

    /**
     * The magnitude of this vector
     * @return
     */
    public float magnitude(){
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * Returns the normalized vector of this one
     * @return
     */
    public Vector2 normalize(){
        float mag = magnitude(); if(mag == 0) mag = 1;
        return new Vector2(x / mag, y / mag);
    }

    /**
     * Scales this vector by f
     * @param f
     * @return
     */
    public Vector2 scale(float f){
        return new Vector2(x * f, y * f);
    }

    /**
     * Returns this vector as an array
     * @return
     */
    public float[] asArray(){
        return new float[] {x, y};
    }

    /**
     * Computes the dot product of this vector with another
     * @param o
     * @return
     */
    public float dot(Vector2 o){
        return o.x * x + o.y * y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
    public static native Vector2 parse(String str);

    public Vector3 toVector3(){
        return new Vector3(x, y, 0);
    }

    public Vector2 copy(){
        return new Vector2(x, y);
    }
}
