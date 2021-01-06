package com.roncho.engine.physics;

import android.opengl.GLES20;

import com.roncho.engine.gl.objects.WorldObject;
import com.roncho.engine.helpers.Builder;
import com.roncho.engine.helpers.MathF;
import com.roncho.engine.helpers.Time;
import com.roncho.engine.structs.primitive.d3.Box;
import com.roncho.engine.structs.primitive.d3.Vector3;

public class PhysicsObject extends WorldObject {

    public abstract class Collider {

        public PhysicsObject object = PhysicsObject.this;

        public abstract Vector3 getClosestPoint(Vector3 point);
        public abstract boolean contains(Vector3 point);

        public boolean intersects(Collider other){
            return contains(other.getClosestPoint(transform.position));
        }
    }
    public class BoxCollider extends Collider {

        public Box box;

        public BoxCollider(Vector3 size){
            box = new Box(size);
        }

        @Override
        public Vector3 getClosestPoint(Vector3 point) {
            Vector3 min = transform.position.add(box.offset).sub(box.size.scale(.5f));
            Vector3 max = transform.position.add(box.offset).add(box.size.scale(.5f));
            return  new Vector3(MathF.clamp(point.x, min.x, max.x),
                                MathF.clamp(point.y, min.y, max.y),
                                MathF.clamp(point.z, min.z, max.z));
        }

        @Override
        public boolean contains(Vector3 point) {
            return box.contains(point.sub(transform.position));
        }
    }
    public class SphereCollider extends Collider {

        public float radius;

        public SphereCollider(float radius){
            this.radius = radius;
        }

        @Override
        public Vector3 getClosestPoint(Vector3 point) {
            Vector3 dir = point.sub(transform.position).normalize();
            return transform.position.add(dir.scale(radius));
        }

        @Override
        public boolean contains(Vector3 point) {
            float dist = point.sub(transform.position).magnitude();
            return dist <= radius;
        }
    }

    public class BoundingBox extends Component {

        private BoxCollider box;

        @Override
        public void onStart() {
            box = new BoxCollider(Vector3.One.copy());
            collider = box;

        }

        @Override
        public void onUpdate() {
            box.box = Builder.buildBox(mesh.edges(transform.rotation, transform.scale));
        }

        @Override
        public void onDestroy() {

        }
    }

    public class PhysicsComponent extends Component {

        public Vector3 velocity;

        @Override
        public void onStart() {

        }

        @Override
        public void onUpdate() {
            Physics.getCollisions(collider.object, velocity.scale(Time.deltaTime()));
        }

        @Override
        public void onDestroy() {

        }
    }

    public Collider collider;
    public boolean isTrigger;       // Collisions calls a trigger
    public boolean isStatic;        // The object can't change its physics shape

    public PhysicsObject(){
        Physics.add(this);
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();
        Physics.remove(this);
    }
}
