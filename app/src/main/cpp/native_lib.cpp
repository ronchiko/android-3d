#include <jni.h>
#include <string>
#include <android/log.h>
#include <strstream>
#include <vector>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/api-level.h>
#include <unistd.h>

#include "jnih/jnih.h"
#include "data/structs.h"
#include "data/wavefront.h"

#include "audio/engine.h"
#include "parsers/struc_parsers.h"
#include "parsers/wavefront_parser.h"

#include "data/files/audio/wave_pcm.h"


#define LOG_TAG "Engine (Native)"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static SoundEngine *audioEngine;

extern "C" {
    /** Math */
    JNIEXPORT jfloat JNICALL Java_com_roncho_engine_helpers_MathF_min(JNIEnv *, jclass, jfloat a, jfloat b) {
        return a > b ? b : a;
    }
    JNIEXPORT jfloat JNICALL Java_com_roncho_engine_helpers_MathF_max(JNIEnv *, jclass, jfloat a, jfloat b) {
        return a < b ? b : a;
    }

    /** Structure parsers */
    JNIEXPORT jobject JNICALL Java_com_roncho_engine_structs_primitive_Quaternion_parse(JNIEnv* env, jclass cls, jstring str){
        const char* value = env->GetStringUTFChars(str, JNI_FALSE);
        return parseF<4>(value, env, cls, "x", "y", "z", "w");
    }
    JNIEXPORT jobject JNICALL Java_com_roncho_engine_structs_primitive_Color_parse(JNIEnv* env, jclass cls, jstring str){
        const char* value = env->GetStringUTFChars(str, JNI_FALSE);
        return parseF<4>(value, env, cls, "r", "g", "b", "a");
    }
    JNIEXPORT jobject JNICALL Java_com_roncho_engine_structs_primitive_d3_Vector3_parse(JNIEnv* env, jclass cls, jstring str){
        const char* value = env->GetStringUTFChars(str, JNI_FALSE);
        return parseF<3>(value, env, cls, "x", "y", "z");
    }
    JNIEXPORT jobject JNICALL Java_com_roncho_engine_structs_primitive_d3_Int3_parse(JNIEnv* env, jclass cls, jstring str){
        const char* value = env->GetStringUTFChars(str, JNI_FALSE);
        return parseI<3>(value, env, cls, "x", "y", "z");
    }
    JNIEXPORT jobject JNICALL Java_com_roncho_engine_structs_primitive_d2_Vector2_parse(JNIEnv* env, jclass cls, jstring str){
        const char* value = env->GetStringUTFChars(str, JNI_FALSE);
        return parseF<2>(value, env, cls, "x", "y");
    }
    JNIEXPORT jobject JNICALL Java_com_roncho_engine_structs_primitive_d2_Int2_parse(JNIEnv* env, jclass cls, jstring str){
        const char* value = env->GetStringUTFChars(str, JNI_FALSE);
        return parseI<2>(value, env, cls, "x", "y");
    }
    JNIEXPORT void JNICALL Java_com_roncho_engine_structs_Mesh_00024WavefrontMeshData_parse(JNIEnv* env, jobject obj, jstring data){
        parseWavefront(env, obj, data);
    }

    JNIEXPORT void JNICALL Java_com_roncho_engine_android_GameRendererView_init(JNIEnv* env, jclass) {
        WavefrontMeshData::init(env);
        LOGI("Initiated native library");
    }

    JNIEXPORT void JNICALL Java_com_roncho_engine_audio_AudioEngine_start(JNIEnv*, jclass, jint maxStreams){
        LOGI("Making sound engine");
        audioEngine = new SoundEngine(maxStreams, SL_SAMPLINGRATE_44_1, SL_PCMSAMPLEFORMAT_FIXED_16);
        if(audioEngine->invalid()) {
            delete audioEngine;
            LOGE("Failed to start audio engine.");
        }
        else LOGI("Successfully started sound engine.");
    }

    JNIEXPORT void JNICALL Java_com_roncho_engine_audio_AudioEngine_shutdown(JNIEnv*, jclass){
        if(audioEngine) {
            delete audioEngine;
            audioEngine = nullptr;
            LOGI("Successfully shutdown the sound engine.");
        }
    }

    JNIEXPORT jint JNICALL Java_com_roncho_engine_audio_AudioEngine_play(JNIEnv*, jclass, jint sampleId, jfloat volume){
        if(audioEngine && sampleId > 0) return audioEngine->play(sampleId, volume);
        return 0;
    }

    JNIEXPORT jint JNICALL Java_com_roncho_engine_audio_AudioEngine_load(JNIEnv* env, jclass, jbyteArray wavData, jint size) {
        if(audioEngine) {
            char *wavRaw8Bit = (char *) env->GetByteArrayElements(wavData, JNI_FALSE);

            WavePcm pcm(wavRaw8Bit, size);

            if (pcm.isvalid()) {
                WavePcmData &data = pcm.wpcmData();
                int id = audioEngine->load(pcm);
                data.rawVoid = nullptr;
                return id;
            }
        }
        return 0;
    }

    JNIEXPORT void JNICALL Java_com_roncho_engine_audio_AudioStreamPlayer_stop(JNIEnv* env, jobject self){
        jclass audioStreamPlayerClass = env->GetObjectClass(self);
        jfieldID streamFieldId = env->GetFieldID(audioStreamPlayerClass, "stream", "I");
        int streamId = env->GetIntField(self, streamFieldId);

        if(streamId >= 0 && audioEngine){
            audioEngine->stop(streamId);
        }
    }
    JNIEXPORT void JNICALL Java_com_roncho_engine_audio_AudioStreamPlayer_resume(JNIEnv* env, jobject self){
        jclass audioStreamPlayerClass = env->GetObjectClass(self);
        jfieldID streamFieldId = env->GetFieldID(audioStreamPlayerClass, "stream", "I");
        int streamId = env->GetIntField(self, streamFieldId);

        if(streamId >= 0 && audioEngine){
            audioEngine->resume(streamId);
        }
    }
    JNIEXPORT void JNICALL Java_com_roncho_engine_audio_AudioStreamPlayer_setVolume(JNIEnv* env, jobject self, jfloat volume){
        jclass audioStreamPlayerClass = env->GetObjectClass(self);
        jfieldID streamFieldId = env->GetFieldID(audioStreamPlayerClass, "stream", "I");
        int streamId = env->GetIntField(self, streamFieldId);

        if(streamId >= 0 && audioEngine){
            audioEngine->setVolume(streamId, volume);
        }
    }

}











