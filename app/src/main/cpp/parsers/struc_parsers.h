#pragma once

template <int size, typename... Fields>
jobject parseI(const char* value, JNIEnv* env, jclass clazz, Fields... fields);
template <int size, typename... Fields>
jobject parseF(const char* value, JNIEnv* env, jclass clazz, Fields... fields);
