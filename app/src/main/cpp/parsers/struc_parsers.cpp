
#include "jnih/jnih.h"

#include <strstream>
#include <iostream>

template<typename T> inline static void extract(T* array, int count, const char* value) noexcept {
    std::strstream stream;
    char junk;

    stream << value;

    for(int i = 0; i < count;i ++){
        stream >> junk;
        stream >> array[i];
    }
    stream >> junk;
}

template <int size, typename... Fields>
jobject parseF(const char* value, JNIEnv* env, jclass clazz, Fields... fields) {
    jmethodID maker = defaultConstructor(env, clazz);
    jobject o = env->NewObject(clazz, maker);

    float values[size];
    extract<float>(values, size, value);

    const char* fields_[size] = { fields... };
    for(int i = 0; i < size; i++){
        jfieldID id = env->GetFieldID(clazz, fields_[i], "F");
        env->SetFloatField(o, id, values[i]);
    }
    return o;
}

template <int size, typename... Fields>
jobject parseI(const char* value, JNIEnv* env, jclass clazz, Fields... fields) {
    jmethodID maker = defaultConstructor(env, clazz);
    jobject o = env->NewObject(clazz, maker);

    int values[size];
    extract<int>(values, size, value);

    const char* fields_[size] = { fields... };
    for(int i = 0; i < size; i++){
        jfieldID id = env->GetFieldID(clazz, fields_[i], "I");
        env->SetIntField(o, id, values[i]);
    }
    return o;
}

template jobject parseF<4, const char*, const char*, const char*, const char*>(const char*, JNIEnv*, jclass, const char*, const char*, const char*, const char*);
template jobject parseF<3, const char*, const char*, const char*>(const char*, JNIEnv*, jclass, const char*, const char*, const char*);
template jobject parseI<3, const char*, const char*, const char*>(const char*, JNIEnv*, jclass, const char*, const char*, const char*);
template jobject parseF<2, const char*, const char*>(const char*, JNIEnv*, jclass, const char*, const char*);
template jobject parseI<2, const char*, const char*>(const char*, JNIEnv*, jclass, const char*, const char*);