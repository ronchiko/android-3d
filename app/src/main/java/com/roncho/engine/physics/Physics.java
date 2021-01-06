package com.roncho.engine.physics;

import com.roncho.engine.structs.primitive.d3.Vector3;

import java.util.ArrayList;

public class Physics {

    static class SimulatedCollider {

        private final PhysicsObject object;
        private final PhysicsObject.Collider collider;
        private final Vector3 shift;

        public SimulatedCollider(PhysicsObject.Collider collider, Vector3 shift){
            this.collider = collider;
            object = collider.object;

            this.shift = shift;
        }

        public boolean intersects(PhysicsObject.Collider other){
            return collider.contains(this.collider.getClosestPoint(other.object.transform.position).add(shift));
        }
    }

    private final static ArrayList<PhysicsObject> objects = new ArrayList<>();

    static void add(PhysicsObject object){
        objects.add(object);
    }
    
    static void remove(PhysicsObject object){
        objects.remove(object);
    }
    
    public static void clear() { objects.clear(); }

    public static Collision[] getCollisions(PhysicsObject object, Vector3 point){
        if(object.collider == null) return new Collision[0];
        SimulatedCollider collider = new SimulatedCollider(object.collider, point);
        ArrayList<Collision> collisions = new ArrayList<>();
        for(PhysicsObject po : objects){
            if(po.collider == null || po == object) continue;
            if(collider.intersects(po.collider)) collisions.add(new Collision(po.collider));
        }
        Collision[] objects = new Collision[collisions.size()];
        for(int i = 0; i < objects.length; i++) objects[i] = collisions.get(i);
        return objects;
    }
}
