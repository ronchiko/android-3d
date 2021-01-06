package com.roncho.engine.structs.primitive.d3;

public class Box {

    public Vector3 size;
    public Vector3 offset;

    public Box(Vector3 size){
        this(size, Vector3.Zero.copy());
    }

    public Box(Vector3 size, Vector3 offset){
        this.offset = offset;
        this.size = size;
    }

    public boolean contains(Vector3 point){
        Vector3 min = offset.sub(size.scale(.5f));
        Vector3 max = offset.add(size.scale(.5f));
        return  min.x <= point.x && point.x <= max.x &&
                min.y <= point.y && point.y <= max.y &&
                min.z <= point.z && point.z <= max.z;
    }
}
