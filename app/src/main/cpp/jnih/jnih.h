#pragma once

#include <jni.h>

inline jmethodID defaultConstructor(JNIEnv* env, jclass clazz) noexcept {
    return env->GetMethodID(clazz, "<init>", "V()");
}

/** Creates an java array within c++ */
jfloatArray createArray(JNIEnv* env, const float* array, const int size);
jbyteArray createArray(JNIEnv* env, const int8_t* array, const int size);
jintArray createArray(JNIEnv* env, const int* array, const int size);
