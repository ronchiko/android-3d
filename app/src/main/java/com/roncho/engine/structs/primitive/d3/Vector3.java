package com.roncho.engine.structs.primitive.d3;

import com.roncho.engine.structs.primitive.Quaternion;
import com.roncho.engine.structs.primitive.d2.Int2;
import com.roncho.engine.structs.primitive.d2.Vector2;

public class Vector3 {
    public float x, y, z;

    public final static Vector3 Zero = new Vector3(0,0,0);
    public final static Vector3 One = new Vector3(1,1,1);
    public final static Vector3 Right = new Vector3(1,0,0);
    public final static Vector3 Left = new Vector3(-1,0,0);
    public final static Vector3 Up = new Vector3(0,1,0);
    public final static Vector3 Down = new Vector3(0,-1,0);
    public final static Vector3 Forward = new Vector3(0,0,1);
    public final static Vector3 Back = new Vector3(0,0,-1);

    public final static int ARRAY_SIZE = 3;

    /**
     * Makes a new vector
     * @param x the X component of the vector
     * @param y the Y component of the vector
     * @param z the Z component of the vector
     */
    public Vector3(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    /**
     * Makes a new vector, with Z = 0
     * @param x the X component of the vector
     * @param y the Y component of the vector
     */
    public Vector3(float x, float y){
        this(x, y, 0);
    }
    /**
     * Copy constructor
     */
    public Vector3(Vector3 o){
        this(o.x, o.y, o.z);
    }
    /**
     * Makes a zero vector
     */
    public Vector3(){
        this(0, 0, 0);
    }

    /**
     * Returns the length of this vector
     * @return float
     */
    public float magnitude(){
        return (float)Math.sqrt(sqrMagnitude());
    }

    /**
     * Returns the length of this vector squared
     * @return
     */
    public float sqrMagnitude() { return x * x + y * y + z * z; }
    /**
     * Returns a normalized copy of this vector
     * @return Vector3
     */
    public Vector3 normalize(){
        float length = magnitude();
        if(length == 0) length = 1; // Allow normalization for (0,0,0) vector
        return new Vector3(x / length, y / length, z / length);
    }

    /**
     * Adds a vector to this vector
     * @param vec Vector3
     */
    public Vector3 add(Vector3 vec){
        return new Vector3(x + vec.x, y + vec.y, z + vec.z);
    }
    /**
     * Subtracts a vector to this vector
     * @param vec Vector3
     */
    public Vector3 sub(Vector3 vec){
        return new Vector3(x - vec.x, y - vec.y, z - vec.z);
    }
    /**
     * Returns a negated vector
     * @return Vector3
     */
    public Vector3 negate(){
        return new Vector3(-x, -y,-z);
    }
    /**
     * Scales the vector
     * @param s Vector3
     */
    public Vector3 scale(float s) {
        return new Vector3(x * s, y * s, z * s);
    }
    public Vector3 scale(Vector3 s) {
        return new Vector3(x * s.x, y * s.y, z * s.z);
    }

    /**
     * Computes a dot product of this vector and another
     * @param other Vector3
     * @return float
     */
    public float dot(Vector3 other){
        return other.x * x + other.y * y + other.z * z;
    }
    /**
     * Computes the cross product of this vector and another
     * @param other Vector3
     * @return Vector3
     */
    public Vector3 cross(Vector3 other){
        return new Vector3(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    /**
     * Returns the vector as a float array
     * @return float[]
     */
    public float[] toArray(){
        return  new float[] {x, y, z};
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }
    public static native Vector3 parse(String str);

    public Vector2 toVector2(){
        return new Vector2(x, y);
    }

    /**
     * Creates a copy of this vector3
     * @return Vector3
     */
    public Vector3 copy() { return new Vector3(this); }

    public Vector3 rotate(Quaternion q){
        float scalar = q.w;
        Vector3 u = new Vector3(q.x, q.y, q.z);

        return u.scale(dot(u) * 2).add(scale(scalar * scalar - u.dot(u))).add(u.cross(this).scale(2 * scalar));
    }

    public boolean equals(Vector3 other){
        return x == other.x && y == other.y && z == other.z;
    }
    public boolean equals(Int3 other){
        return x == other.x && y == other.y && z == other.z;
    }
    public boolean equals(Vector2 other){
        return z == 0 && x == other.x && y == other.y;
    }
    public boolean equals(Int2 other){
        return z == 0 && x == other.x && y == other.y;
    }
}
