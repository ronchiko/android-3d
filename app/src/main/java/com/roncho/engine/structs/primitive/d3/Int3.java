package com.roncho.engine.structs.primitive.d3;

public class Int3 {
    public int x, y, z;

    public Int3(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Int3() {
        this(0, 0, 0);
    }

    @Override
    public String toString(){
        return "(" + x  +"," + y  +"," + z + ")";
    }
    public static native Int3 parse(String str);
}
