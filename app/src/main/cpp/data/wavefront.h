#pragma once

#include <jni.h>
#include <vector>

#include "data/structs.h"
#include "jnih/jnih.h"

struct WavefrontMeshData {
private:
    static jclass clazz;
    static jfieldID verteciesId, uvsId, normalsId, specialId;

    std::vector<jfloat> vVertecies, vUVs, vNormals;
public:
    // Init this java class interface
    static void init(JNIEnv* env);

    inline WavefrontMeshData() : vVertecies(), vUVs(), vNormals() {}

    // Applies the places vertecies, uvs and normals to the a java object
    jobject createJObject(JNIEnv* env, jobject o, std::vector<Vec3*>& spec) const;

    // Puts values into the buffers
    void put(const Vec3& vertex, const Vec2& uv, const Vec3& normal) noexcept;

    void appendNormal(int index, const Vec3& normal) noexcept;

    void normalizeNormals() noexcept;
};