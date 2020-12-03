attribute vec4 position;
attribute vec2 uvs;
attribute vec3 vNormals;

uniform mat4 mvpMatrix;
uniform vec3 eyeDirection;

varying vec2 textureCoord;
varying vec3 transformedNormal;
varying vec3 eye;

struct Transform {
    vec3 position;
    vec4 rotation;
    vec3 scale;
};
uniform Transform transform;

// The conhugate of a quaternion
vec4 quat_conjugate(vec4 q){
    return vec4(-q.x, -q.y, -q.z, q.w);
}

// hamilton product
vec4 quat_mult(vec4 q1, vec4 q2){
    vec4 q;
    q.x = (q1.w * q2.x) + (q1.x * q2.w) + (q1.y * q2.z) - (q1.z * q2.y);
    q.y = (q1.w * q2.y) - (q1.x * q2.z) + (q1.y * q2.w) + (q1.z * q2.x);
    q.z = (q1.w * q2.z) + (q1.x * q2.y) - (q1.y * q2.x) + (q1.z * q2.w);
    q.w = (q1.w * q2.w) - (q1.x * q2.x) - (q1.y * q2.y) - (q1.z * q2.z);
    return q;
}

// Rotates a vetrex by a quaternion
vec3 rotate_vertex_position(vec3 p, vec4 qr){
    float s = qr.w;
    vec3 u = qr.xyz;
    return 2.0 * dot(p, u) * u + (s * s - dot(u, u)) * p + cross(u, p) * 2.0 * s;
}

vec3 transformVertex(vec3 v){
    v = rotate_vertex_position(v * transform.scale, transform.rotation);
    v += transform.position;
    return v;
}

void main(){
    vec3 p = transformVertex(position.xyz);
    gl_Position = mvpMatrix * vec4(p, 1);

    transformedNormal = rotate_vertex_position(vNormals, transform.rotation);
    textureCoord = uvs.xy;
    eye = transformVertex(eyeDirection);
}