package com.roncho.engine.structs.primitive;

public class Int2 {

    public int x, y;

    private Int2(float x, float y){
        this.x = (int)x;
        this.y = (int)y;
    }

    public Int2(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Int2(Int2 other){
        x = other.x;
        y = other.y;
    }

    public Int2 scale(float s){
        return new Int2(x * s, y * s);
    }
    public Int2 add(Int2 other){
        return  new Int2(x + other.x, y + other.y);
    }
    public Int2 sub(Int2 other){
        return new Int2(x - other.x, y - other.y);
    }
}
