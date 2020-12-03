#ifndef OPEN_GL_UI_TEXT_H
#define OPEN_GL_UI_TEXT_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <unordered_map>
#include <string>
#include <sstream>

#define CHECK_NULL(o) if(o == nullptr) return;

std::unordered_map<std::string, jfieldID>& fields = *new std::unordered_map<std::string, jfieldID>();

std::string makeFieldSig(JNIEnv* env, const char* o, const char* c) {
    std::stringstream ss;
    ss << std::string(o) << "." << std::string(c);
    return ss.str();
}

jfieldID getFieldId(JNIEnv* env, jobject slf, const char* clazz, const char* field, const char* sig){
    std::string mk = makeFieldSig(env, clazz, field);
    if(fields.find(mk) == fields.end()) {
        fields[mk] = env->GetFieldID(env->GetObjectClass(slf), field, sig);
        if(env->ExceptionCheck()) { LOGE("No field %s SIG:%s on class %s", field, sig, clazz); }
    }
    return fields[mk];
}

jint getInt(JNIEnv* env, jobject slf, const char* clazz, const char* field){
    jfieldID id = getFieldId(env, slf, clazz, field, "I");
    return env->GetIntField(slf, id);
}
jfloat getFloat(JNIEnv* env, jobject slf, const char* clazz, const char* field){
    jfieldID id = getFieldId(env, slf, clazz, field, "F");
    return env->GetFloatField(slf, id);
}
inline jobject getObject(JNIEnv* env, jobject slf, const char* clazz, const char* field, const char* sig){
    jfieldID id = getFieldId(env, slf, clazz, field, sig);
    jobject o = env->GetObjectField(slf, id);
    if(env->ExceptionCheck()) { LOGE("No field %s on class %s", clazz, field); }
    return o;
}

#define getString(env, slf, clazz, field, sig) static_cast<jstring>(getObject(env, slf, clazz, field, sig))

extern "C" JNIEXPORT void JNICALL
Java_com_roncho_engine_templates_UiText_drawUnit(JNIEnv* env, jobject slf, jfloatArray mvp){
    /** Get the text from the java object */
    jobject textComponent = getObject(env, slf, "uitext", "text", "Lcom/roncho/engine/templates/UiText$TextComponent;");

    // jobject transform = getObject(env, slf, "uitext", "transform", "Lcom/roncho/engine/gl/objects/UIObject/Transform;");
    jstring textValue = getString(env, textComponent, "uitext.text","text", "Ljava/lang/String;");
    jobject atlas = getObject(env, textComponent, "uitext.text", "atlas", "Lcom/roncho/engine/gl/text/TextAtlas;");

    std::string text = env->GetStringUTFChars(textValue, JNI_FALSE);

    /** Get the cached attribute pointers */
    GLuint positionAttributeHandle = static_cast<GLuint>(getInt(env, slf, "uitext", "positionAttributeHandle"));
    GLuint uvAttributeHandle = static_cast<GLuint>(getInt(env, slf, "uitext", "uvHandle"));

    jmethodID getCharId = env->GetMethodID(env->GetObjectClass(atlas), "getChar", "(C)[F");

    glEnableVertexAttribArray(positionAttributeHandle);
    glEnableVertexAttribArray(uvAttributeHandle);

    float* vertexArray = new float[4];
    float* uvs = new float[8] { 0, 0, 1, 0, 0, 1, 1, 1};
    float* pos = new float[8] { -1, 1, 1, 1, -1, -1, 1, -1};

    const int match[8] = {0, 1, 2, 1, 0, 3, 2, 3};

    for(auto& c : text){
        /** Get the correct uvs uvs */
        jfloatArray uvsArray = static_cast<jfloatArray>(env->CallObjectMethod(atlas, getCharId, static_cast<jchar>(c)));
        jfloat* tileUvs = env->GetFloatArrayElements(uvsArray, JNI_FALSE);

        /** Move to the gl buffer array */
        for(int i = 0; i < 8; i++) uvs[i] = tileUvs[match[i]];

        /** Perform drawing */
        glVertexAttribPointer(uvAttributeHandle, 2, GL_FLOAT, GL_FALSE, 0, &uvs);
        glVertexAttribPointer(positionAttributeHandle, 2, GL_FLOAT, GL_FALSE, 0, &pos);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        std::stringstream ss;
        for(int i = 0; i < 8; i+= 2){
            ss << "(" << uvs[i] << "," << uvs[i + 1] << ")";
        }
        //LOGI("%s", ss.str().c_str());
    }

    /** Disable the used arrays */
    glDisableVertexAttribArray(positionAttributeHandle);
    glDisableVertexAttribArray(uvAttributeHandle);

    /** Cleanup */
    delete[] uvs;
    delete[] vertexArray;
}

#endif
