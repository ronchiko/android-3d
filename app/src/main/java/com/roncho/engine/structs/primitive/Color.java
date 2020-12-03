package com.roncho.engine.structs.primitive;

public class Color {
    public float a, r, g, b;

    public Color(int r, int g, int b, int a){
        this.a = a / 255f;
        this.b = b / 255f;
        this.g = g / 255f;
        this.r = r / 255f;
    }

    public Color(int r, int g, int b){
        this(r, g, b, 255);
    }

    public Color(float r, float g, float b, float a){
        this.a = a;
        this.b = b;
        this.g = g;
        this.r = r;
    }

    public Color(float r, float g, float b){
        this(r, g, b, 1);
    }

    public Color() {this(0, 0, 0, 0);}

    public float[] asArray(){
        return new float[]{r, g, b, a};
    }

    public static Color white() {return new Color(255, 255, 255);}
    public static Color black() {return new Color(0, 0, 0);}

    public Color copy(){
        return new Color(r,g,b,a);
    }

    public String toString(){
        return "(" + r + "," + g + "," + b + "," + a + ")";
    }

    public static native Color parse(String str);
}
