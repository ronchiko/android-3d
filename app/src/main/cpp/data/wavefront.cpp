
#include "wavefront.h"

jfieldID WavefrontMeshData::uvsId = nullptr;
jfieldID WavefrontMeshData::verteciesId = nullptr;
jfieldID WavefrontMeshData::normalsId = nullptr;
jfieldID WavefrontMeshData::specialId = nullptr;
jclass WavefrontMeshData::clazz = nullptr;

void WavefrontMeshData::init(JNIEnv *env) {
    clazz = env->FindClass("com/roncho/engine/structs/Mesh$WavefrontMeshData");

    verteciesId = env->GetFieldID(clazz, "verts", "[F");
    uvsId = env->GetFieldID(clazz, "uv", "[F");
    normalsId = env->GetFieldID(clazz, "norm", "[F");
    specialId = env->GetFieldID(clazz, "special", "[F");
}

jobject WavefrontMeshData::createJObject(JNIEnv *env, jobject o, std::vector<Vec3 *> &spec) const  {
    jfloatArray verts = createArray(env, vVertecies.data(), vVertecies.size());
    jfloatArray  uv = createArray(env, vUVs.data(), vUVs.size());
    jfloatArray norms = createArray(env, vNormals.data(), vNormals.size());

    auto* specArr = new jfloat[spec.size() * 3];
    uint specLen = spec.size() * 3;
    for(int i = 0; i < spec.size(); i++){
        specArr[i * 3] = spec[i]->x;
        specArr[i * 3 + 1] = spec[i]->y;
        specArr[i * 3 + 2] = spec[i]->z;
        delete spec[i];
    }
    jfloatArray special = createArray(env, specArr, specLen);

    env->SetObjectField(o, verteciesId, verts);
    env->SetObjectField(o, uvsId, uv);
    env->SetObjectField(o, normalsId, norms);
    env->SetObjectField(o, specialId, special);
    return o;
}

void WavefrontMeshData::put(const Vec3 &vertex, const Vec2 &uv, const Vec3 &normal) noexcept  {
    vVertecies.push_back(vertex.x);
    vVertecies.push_back(vertex.y);
    vVertecies.push_back(vertex.z);

    vUVs.push_back(uv.x);
    vUVs.push_back(uv.y);

    vNormals.push_back(normal.x);
    vNormals.push_back(normal.y);
    vNormals.push_back(normal.z);
}

void WavefrontMeshData::appendNormal(int index, const Vec3 &normal) noexcept  {
    vNormals[index * 3] += normal.x;
    vNormals[index * 3 + 1] += normal.y;
    vNormals[index * 3 + 2] += normal.z;
}

void WavefrontMeshData::normalizeNormals() noexcept  {
    for(int i = 0; i < vNormals.size(); i+=3){
        float& x = vNormals[i];
        float& y = vNormals[i + 1];
        float& z = vNormals[i + 2];
        float mag =  sqrt(x * x + y * y + z * z);
        x /= mag; y /= mag; z /= mag;
    }
}