package com.roncho.engine.structs.primitive.d2;

public class Rect {
    public Vector2 position, size;

    public Rect(float x, float y, float width, float height){
        this.position = new Vector2(x, y);
        this.size = new Vector2(width,height);
    }

    public Rect(Vector2 origin, Vector2 size){
        this(origin.x, origin.y, size.x, size.y);
    }

    public float endX() {return position.x + size.x;}
    public float endY() {return position.y + size.y;}
    public Vector2 end() {return position.add(size);}

    /** Checks if a point is within the bounds of this rectangle*/
    public boolean inBounds(Vector2 point){
        return point.x >= position.x && point.x <= endX() && point.y >= position.y && point.y <= endY();
    }

    public float[] toArray(){
        // Logger.Log( "Rect: " + position + ", (" + endX() + ", " + endY() + ")");
        return new float[]{position.x, position.y, endX(), endY()};
    }

    public float width() { return size.x; }
    public float height() { return size.y; }

    public float xMax() { return position.x + size.x; }
    public float yMax() { return position.y + size.y; }
}
