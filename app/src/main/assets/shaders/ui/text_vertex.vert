attribute vec2 position;
attribute vec2 uv;

struct Transform {
    vec2 position;
    vec2 scale;
    float angle;
};
uniform Transform transform;
uniform Transform internal;

varying vec2 textureCoord;

vec2 rotate(vec2 v, float a){
    float ca = cos(a);
    float sa = sin(a);

    return vec2(v.x * ca - v.y * sa, v.x * sa + v.y * ca);
}

vec2 transform_point(vec2 point, Transform transform){
    return rotate(point * transform.scale, transform.angle) + transform.position;
}

void main(){
    vec2 point = transform_point(transform_point(position, internal), transform);
    gl_Position = vec4(point.x, point.y, 0.0, 1.0);
    textureCoord = uv;
}