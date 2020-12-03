#include <jni.h>
#include <string>
#include <android/log.h>
#include <strstream>
#include <vector>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/api-level.h>

#define LOG_TAG "Engine (Native)"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct Vec2 {
    float x, y;

    Vec2() : x(0), y(0) {}
};
struct Vec3 {
    float x, y, z;

    Vec3() : x(0), y(0), z(0) {}
    Vec3(float x, float y, float z) : x(0), y(0), z(0) {}

    Vec3 operator+(const Vec3& other) const{
        return {x + other.x, y + other.y, z + other.z};
    }
    Vec3 operator-(const Vec3& other) const{
        return {x - other.x, y - other.y, z - other.z};
    }

    Vec3& operator-=(const Vec3& other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        return *this;
    }
    Vec3& operator+=(const Vec3& other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return *this;
    }

    float magnitude() const {
        return sqrt(x * x + y * y  + z * z);
    }
    void normalize() {
        float mag = magnitude();
        x /= mag; y /= mag; z /= mag;
    }
};

struct Face {
    int v[3];
};

Vec3 cross(const Vec3& a, const Vec3 b){
    Vec3 o;
    o.x = a.y * b.z - a.z * b.y;
    o.y = a.z * b.x - a.x * b.z;
    o.z = a.x * b.y - a.y * b.x;
    return  o;
}

std::istream& operator>>(std::istream& stream, Vec3& v){
    stream >> v.x;
    stream >> v.y;
    stream >> v.z;
    return stream;
}
std::istream& operator>>(std::istream& stream, Vec2& v){
    stream >> v.x;
    stream >> v.y;
    return stream;
}

// Creates a java float array
static jfloatArray createArray(JNIEnv* env, const float* array, const int size){
    jfloatArray jArray = env->NewFloatArray(size);

    jfloat jFill[size];
    for(int i = 0; i < size; i++){
        jFill[i] = array[i];
    }

    env->SetFloatArrayRegion(jArray, 0, size, jFill);

    return jArray;
}

// Creates a java int array
static jintArray createArray(JNIEnv* env, const int* array, const int size){
    jintArray jArray = env->NewIntArray(size);

    jint* jFill = new jint[size];
    for(int i = 0; i < size; i++){
        jFill[i] = array[i];
    }

    env->SetIntArrayRegion(jArray, 0, size, jFill);
    return jArray;
}

// Creates a java int array
static jbyteArray createArray(JNIEnv* env, const int8_t* array, const int size){
    jbyteArray  jArray = env->NewByteArray(size);

    auto* jFill = new jbyte[size];
    for(int i = 0; i < size; i++){
        jFill[i] = array[i];
    }

    env->SetByteArrayRegion(jArray, 0, size, jFill);
    return jArray;
}

struct WavefrontMeshData {
private:
    static jclass clazz;
    static jfieldID verteciesId, uvsId, normalsId;

    std::vector<jfloat> vVertecies, vUVs, vNormals;
public:
    // Init this java class interface
    static void init(JNIEnv* env){
        clazz = env->FindClass("com/roncho/engine/structs/Mesh$WavefrontMeshData");

        verteciesId = env->GetFieldID(clazz, "verts", "[F");
        uvsId = env->GetFieldID(clazz, "uv", "[F");
        normalsId = env->GetFieldID(clazz, "norm", "[F");
    }

    WavefrontMeshData() : vVertecies(), vUVs(), vNormals() {}

    // Applies the places vertecies, uvs and normals to the a java object
    jobject createJObject(JNIEnv* env, jobject o) const {
        jfloatArray verts = createArray(env, vVertecies.data(), vVertecies.size());
        jfloatArray  uv = createArray(env, vUVs.data(), vUVs.size());
        jfloatArray norms = createArray(env, vNormals.data(), vNormals.size());

        env->SetObjectField(o, verteciesId, verts);
        env->SetObjectField(o, uvsId, uv);
        env->SetObjectField(o, normalsId, norms);
        return o;
    }

    // Puts values into the buffers
    void put(const Vec3& vertex, const Vec2& uv, const Vec3& normal){
        vVertecies.push_back(vertex.x);
        vVertecies.push_back(vertex.y);
        vVertecies.push_back(vertex.z);

        vUVs.push_back(uv.x);
        vUVs.push_back(uv.y);

        vNormals.push_back(normal.x);
        vNormals.push_back(normal.y);
        vNormals.push_back(normal.z);
    }

    void appendNormal(int index, const Vec3& normal) {
        vNormals[index * 3] += normal.x;
        vNormals[index * 3 + 1] += normal.y;
        vNormals[index * 3 + 2] += normal.z;
    }

    void normalizeNormals(){
        for(int i = 0; i < vNormals.size(); i+=3){
            float& x = vNormals[i];
            float& y = vNormals[i + 1];
            float& z = vNormals[i + 2];
            float mag =  sqrt(x * x + y * y + z * z);
            x /= mag; y /= mag; z /= mag;
        }
    }

    void logScales(){
        LOGI("Model scales: %i, %i, %i", vVertecies.size() / 3, vUVs.size() / 2, vNormals.size() / 3);
    }
};

jfieldID WavefrontMeshData::uvsId = nullptr;
jfieldID WavefrontMeshData::verteciesId = nullptr;
jfieldID WavefrontMeshData::normalsId = nullptr;
jclass WavefrontMeshData::clazz = nullptr;

jclass worldObjectClass, cameraClass, vector3Class;
jfieldID worldCameraForwardsCache;
jmethodID cameraForwardsMethod, toArrayMethod;

extern "C" JNIEXPORT void JNICALL
Java_com_roncho_engine_android_GameRendererView_init(JNIEnv* env, jclass) {
    LOGI("Initiated native library");
    WavefrontMeshData::init(env);

    worldObjectClass = env->FindClass("com/roncho/engine/gl/objects/WorldObject");
    cameraClass = env->FindClass("com/roncho/engine/gl/objects/Camera$Transform");
    vector3Class = env->FindClass("com/roncho/engine/structs/primitive/Vector3");

    worldCameraForwardsCache = env->GetStaticFieldID(worldObjectClass, "cameraForwards", "[F");
    cameraForwardsMethod = env->GetMethodID(cameraClass, "forwards", "()Lcom/roncho/engine/structs/primitive/Vector3;");
    toArrayMethod = env->GetMethodID(vector3Class, "toArray", "()[F");


}

extern "C" JNIEXPORT void JNICALL
Java_com_roncho_engine_structs_Mesh_00024WavefrontMeshData_parse(JNIEnv* env, jobject obj, jstring data){
    std::strstream stream;
    WavefrontMeshData wvd;

    // Feed the data stream
    stream << env->GetStringUTFChars(data, JNI_FALSE);

    std::vector<Vec3> vertecies, normals;
    std::vector<Vec2> uvs;

    std::vector<Face> faces;

    // Place default uv & normal coordinates
    uvs.emplace_back();
    normals.emplace_back();

    const int BUFFER_SIZE = 256;
    bool calculateNormals = true;

    // Read the .obj file
    while(true){
        char type;
        // Reads a line from the stream
        char buffer[BUFFER_SIZE];
        stream.getline(buffer, BUFFER_SIZE);

        std::strstream line; line << buffer;

        // Check if the stream is over?
        if(stream.eof()){
            break;
        }


        // Switch vertex type
        line >> type;
        switch (type) {
            case 'v': {
                switch (line.peek()) {
                    case ' ':{  // Model vertex
                        Vec3 vertex;
                        line >> vertex;
                        vertecies.push_back(vertex);
                        // Add indexed vertecies
                        // LOGI("Vertex %f, %f, %f", vertex.x, vertex.y, vertex.z);
                    }break;
                    case 't':{
                        line >> type;
                        Vec2 uv;
                        line >> uv;
                        uvs.push_back(uv);
                        // LOGI("UV %f, %f", uv.x, uv.y);
                    }break;
                    case 'n': {   // Mode normal
                        line >> type;
                        Vec3 normal;
                        line >> normal;
                        normals.push_back(normal);
                        // LOGI("Normal %f, %f, %f", normal.x, normal.y, normal.z);
                    }break;
                    default:
                        break;
                }
            }break;
            case 'f': { // Face
                // Pattern should repeat 3 times
                char junk;
                Face face{};
                for(int & i : face.v){
                    int a[3] = {0, 0, 0};
                    line >> a[0];
                    for(int j = 0; j < 2; j++) {
                        if (line.peek() == '/') {
                            line >> junk;
                            if(line.peek() == '/') { a[j + 1] = 0; }
                            else line >> a[j + 1];
                        }
                    }
                    if(a[2] != 0) calculateNormals = false;
                    // LOGI("Face vertex %d v: %d, uv: %d, norm: %d", i, a[0], a[1], a[2]);
                    wvd.put(vertecies[a[0] - 1], uvs[a[1]], normals[a[2]]);
                    i = a[0] - 1;
                }
                faces.push_back(face);
            }break;
            // Something else that is ignored, advance
            default: break;
        }
    }

    // Calculate the normals for undefined surface
    if(calculateNormals){
        for(Face& f : faces){
            Vec3 normal{0,0,0};
            for(int v = 0; v < 3; v++){
                int nv = (v + 1) % 3;
                int nnv = (v + 2) % 3;
                Vec3 e1 = vertecies[nv] - vertecies[v],
                     e2 = vertecies[nnv] - vertecies[nv];
                normal += cross(e1, e2);
            }
            normal.normalize();
            wvd.appendNormal(f.v[0], normal);
            wvd.appendNormal(f.v[1], normal);
            wvd.appendNormal(f.v[2], normal);
        }

        wvd.normalizeNormals();
    }

    wvd.createJObject(env, obj);
}

extern "C" JNIEXPORT void JNICALL
Java_com_roncho_engine_android_WorldRenderer_passPrivateComponents(JNIEnv* env, jobject, jfloatArray cameraForwards){
    env->SetStaticObjectField(worldObjectClass, worldCameraForwardsCache, cameraForwards);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_roncho_engine_structs_primitive_Color_parse(JNIEnv* env, jclass clazz, jstring string){
    std::strstream stream;
    char junk;

    jmethodID constructor = env->GetMethodID(clazz, "<init>", "()V");

    jobject color = env->NewObject(clazz, constructor);

    float rgba[4];

    stream << env->GetStringUTFChars(string, JNI_FALSE);
    stream >> junk;
    stream >> rgba[0];
    stream >> junk;
    stream >> rgba[1];
    stream >> junk;
    stream >> rgba[2];
    stream >> junk;
    stream >> rgba[3];
    stream >> junk;

    const char* fieldNames[4] = {"r", "g", "b", "a"};
    for(int i = 0; i < 4; i++){
        jfieldID id = env->GetFieldID(clazz, fieldNames[i], "F");
        env->SetFloatField(color, id, rgba[i]);
    }
    return color;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_roncho_engine_structs_primitive_Vector3_parse(JNIEnv* env, jclass cls, jstring str){
    std::strstream stream;
    char junk;

    jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");

    jobject color = env->NewObject(cls, constructor);

    float rgba[3];

    stream << env->GetStringUTFChars(str, JNI_FALSE);
    stream >> junk;
    stream >> rgba[0];
    stream >> junk;
    stream >> rgba[1];
    stream >> junk;
    stream >> rgba[2];
    stream >> junk;

    const char* fieldNames[3] = {"x", "y", "z"};
    for(int i = 0; i < 3; i++){
        jfieldID id = env->GetFieldID(cls, fieldNames[i], "F");
        env->SetFloatField(color, id, rgba[i]);
    }
    return color;
}
extern "C" JNIEXPORT jobject JNICALL
Java_com_roncho_engine_structs_primitive_Int3_parse(JNIEnv* env, jclass cls, jstring str){
    std::strstream stream;
    char junk;

    jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");

    jobject color = env->NewObject(cls, constructor);

    int rgba[3];

    stream << env->GetStringUTFChars(str, JNI_FALSE);
    stream >> junk;
    stream >> rgba[0];
    stream >> junk;
    stream >> rgba[1];
    stream >> junk;
    stream >> rgba[2];
    stream >> junk;

    const char* fieldNames[3] = {"x", "y", "z"};
    for(int i = 0; i < 3; i++){
        jfieldID id = env->GetFieldID(cls, fieldNames[i], "I");
        env->SetIntField(color, id, rgba[i]);
    }
    return color;
}
extern "C" JNIEXPORT jobject JNICALL
Java_com_roncho_engine_structs_primitive_Vector2_parse(JNIEnv* env, jclass cls, jstring str){
    std::strstream stream;
    char junk;

    jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
    // if (env->ExceptionCheck()) {}
    jobject color = env->NewObject(cls, constructor);

    float rgba[2];

    stream << env->GetStringUTFChars(str, JNI_FALSE);
    stream >> junk;
    stream >> rgba[0];
    stream >> junk;
    stream >> rgba[1];
    stream >> junk;

    const char* fieldNames[2] = {"x", "y"};
    for(int i = 0; i < 2; i++){
        jfieldID id = env->GetFieldID(cls, fieldNames[i], "F");
        env->SetFloatField(color, id, rgba[i]);
    }
    return color;
}
extern "C" JNIEXPORT jobject JNICALL
Java_com_roncho_engine_structs_primitive_Quaternion_parse(JNIEnv* env, jclass cls, jstring str){
    std::strstream stream;
    char junk;

    jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");

    jobject color = env->NewObject(cls, constructor);

    float rgba[4];

    stream << env->GetStringUTFChars(str, JNI_FALSE);
    stream >> junk;
    stream >> rgba[0];
    stream >> junk;
    stream >> rgba[1];
    stream >> junk;
    stream >> rgba[2];
    stream >> junk;
    stream >> rgba[3];
    stream >> junk;

    const char* fieldNames[4] = {"x", "y", "z", "w"};
    for(int i = 0; i < 4; i++){
        jfieldID id = env->GetFieldID(cls, fieldNames[i], "F");
        env->SetFloatField(color, id, rgba[i]);
    }
    return color;
}

#include "uitext.h"
