precision mediump float;
uniform sampler2D texture;
uniform float shinyness;

varying vec2 textureCoord;
varying vec3 transformedNormal;
varying vec3 eye;

struct GlobalLight {
    vec3 ambientIntensity;
    vec3 sunDirection;
    vec3 diffuseIntensity;
    vec3 specularReflectionConstant;
};
/* Light settings */
uniform GlobalLight glights;

float lerp(float a, float b, float t){
    return (1.0 - t) * a + t * b;
}

void main(){
    vec3 inverseLight = normalize(glights.sunDirection);
    vec3 fragColour = vec3(0.0);

    vec3 sun = normalize(glights.sunDirection);

    /* Smoothen the hard shadows */
    float intensity = max(dot(transformedNormal, sun) + 1.0, 0.5) / 2.0 + 0.1;

    vec3 specularLight = vec3(0.0);

    if(intensity > 0.0){
        vec3 e = normalize(eye);
        vec3 h = normalize(glights.sunDirection + e);
        float specularIntensity = max(0.0, dot(h, transformedNormal));
        specularLight = glights.specularReflectionConstant * pow(specularIntensity, shinyness);
    }

    fragColour = max(intensity * glights.diffuseIntensity + specularLight, glights.ambientIntensity);

    // clamp(fragColour, 0.0, 1.0);

    gl_FragColor = texture2D(texture, textureCoord) * vec4(fragColour, 1.0);
}