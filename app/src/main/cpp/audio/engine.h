#pragma once

#include <vector>
#include <cstdlib>
#include <android/asset_manager.h>
#include <SLES/OpenSLES_Android.h>
#include "data/files/audio/wave_pcm.h"

#define MIN_VOL_MILLIBEL -500

typedef struct __Rs {
    SLuint32 channels;
    SLuint32 samplingRate;
    SLuint32 bps;

    bool operator==(const __Rs& o) const;
} ResourceSpecs;

struct ResourceBuffer {
    ResourceBuffer(WavePcm&);
    ~ResourceBuffer();

    char* buffer;
    int size;
    ResourceSpecs specs;
};

struct BufferQueue {
    ~BufferQueue();

    SLBufferQueueItf* queue;
    SLObjectItf* player;
    SLPlayItf* playerPlay;
    SLVolumeItf* volume;
    bool playing;
    ResourceSpecs specs;
};

class SoundEngine {
public:
    SoundEngine(int maxStreams, SLuint32 samplingRate, SLuint32 bitrate);
    ~SoundEngine();

    bool invalid() const { return !valid; }

    int play(int sampleId, float volume);
    int load(WavePcm&);

    void setVolume(int streamId, float volume);
    void stop(int streamId);
    void resume(int streamId);

    static SoundEngine* instance; /** Singleton instance */
private:
    bool createEngine();

    static void staticBqPlayerCallback(SLBufferQueueItf itf, void* context);
    void bqPlayerCallback(SLBufferQueueItf itf, void* context);

    bool createBufferQueueAudioPlayer();
    bool updateSpecs(BufferQueue*, const ResourceSpecs& specs);

    int maxStreams;
    SLuint32 samplingRate, bitrate;

    SLmillibel minVolume, maxVolume;

    SLObjectItf engineObject;
    SLEngineItf engineEngine;

    SLObjectItf outputMixObject;

    std::vector<BufferQueue*>& bufferQueues;
    std::vector<ResourceBuffer*>& samples;
    bool valid;
};