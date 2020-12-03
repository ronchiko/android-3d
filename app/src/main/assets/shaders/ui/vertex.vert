attribute vec2 position;
attribute vec2 uv;

struct Transform {
    vec2 position;
    vec2 scale;
    float angle;
};
uniform Transform transform;

varying vec2 textureCoord;

vec2 rotate(vec2 v, float a){
    float ca = cos(a);
    float sa = sin(a);

    return vec2(v.x * ca - v.y * sa, v.x * sa + v.y * ca);
}

void main(){
    gl_Position = vec4(rotate(position * transform.scale, transform.angle) + transform.position, 0.0, 1.0);
    textureCoord = uv;
}