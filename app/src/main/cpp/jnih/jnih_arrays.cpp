#include "jnih.h"

jfloatArray createArray(JNIEnv* env, const float* array, const int size){
    jfloatArray jArray = env->NewFloatArray(size);

    jfloat jFill[size];
    for(int i = 0; i < size; i++){
        jFill[i] = array[i];
    }

    env->SetFloatArrayRegion(jArray, 0, size, jFill);

    return jArray;
}
// Creates a java int array
jintArray createArray(JNIEnv* env, const int* array, const int size){
    jintArray jArray = env->NewIntArray(size);

    jint* jFill = new jint[size];
    for(int i = 0; i < size; i++){
        jFill[i] = array[i];
    }

    env->SetIntArrayRegion(jArray, 0, size, jFill);
    return jArray;
}
// Creates a java int array
jbyteArray createArray(JNIEnv* env, const int8_t* array, const int size){
    jbyteArray  jArray = env->NewByteArray(size);

    auto* jFill = new jbyte[size];
    for(int i = 0; i < size; i++){
        jFill[i] = array[i];
    }

    env->SetByteArrayRegion(jArray, 0, size, jFill);
    return jArray;
}
