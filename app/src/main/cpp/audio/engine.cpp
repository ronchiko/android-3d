
#include <jni.h>

#include <android/log.h>
#include <dlfcn.h>
#include <dlfcn.h>

#include "engine.h"
#include "checker.h"

#define check(r) if(r != SL_RESULT_SUCCESS) {LOGI("error %s at line %d\n", result_to_string(result), __LINE__); return false; }

SoundEngine* SoundEngine::instance = nullptr;

__attribute__((constructor)) static void onDlOpen(){
    LOGI("Dl opened");
}

bool ResourceSpecs::operator==(const __Rs &o) const {
    return o.samplingRate == samplingRate && o.channels == channels && o.bps == bps;
}

BufferQueue::~BufferQueue() {
    (**player)->Destroy(*player);
    player = nullptr;
    playerPlay = nullptr;
    volume = nullptr;
    queue = nullptr;
}

ResourceBuffer::ResourceBuffer(WavePcm& wpcm) : buffer(wpcm.wpcmData().raw8),
                                                size(wpcm.wpcmData().subChunk2Size),
                                                specs() {
    auto& h = wpcm.wpcmh();
    specs.channels = h.numChannels;
    specs.samplingRate = h.sampleRate * 1000;
    specs.bps = h.bitsPerSample;
    wpcm.wpcmData().rawVoid = nullptr;  // Move ownership of rawVoid to the Resource buffer
}
ResourceBuffer::~ResourceBuffer() {
    free(buffer);
}

SoundEngine::SoundEngine(int maxStreams, SLuint32 samplingRate, SLuint32 bitrate) :
            maxStreams(maxStreams),
            samplingRate(samplingRate),
            bitrate(bitrate),
            engineEngine(nullptr),
            engineObject(nullptr),
            outputMixObject(nullptr),
            bufferQueues(*new std::vector<BufferQueue*>()),
            samples(*new std::vector<ResourceBuffer*>()),
            valid(true){

    if(instance != nullptr){
        LOGE("Can only be one sound engine at once.");
        delete &bufferQueues;
        delete &samples;
        valid = false;
        return;
    }
    instance = this;

    void* handle = dlopen("libOpenSLES.so", RTLD_LAZY);
    if(handle == nullptr){
        LOGE("Open SL ES wasn't found");
        delete &bufferQueues;
        delete &samples;
        valid = false;
        return;
    }

    valid &= createEngine();
    valid &= createBufferQueueAudioPlayer();

    minVolume = SL_MILLIBEL_MIN;
    maxVolume = 0;
}

int SoundEngine::load(WavePcm& wpcm) {
    auto* b = new ResourceBuffer(wpcm);
    samples.push_back(b);
    return samples.size();
}

bool SoundEngine::createEngine() {
    SLresult result;

    // Init the SL ES sound engine
    const SLInterfaceID eng_ids[1] = {SL_IID_ENGINE};
    const SLboolean eng_req[1] = {SL_BOOLEAN_TRUE};
    result = slCreateEngine(&engineObject, 0, nullptr, 0, eng_ids, eng_req);
    check(result);

    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    check(result);

    // Start the engine object
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    check(result);

    // Create & realize the output mix object
    const SLInterfaceID ids[0] = {};
    const SLboolean req[0] = {};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, ids, req);
    check(result);

    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    check(result);

    return true;
}

void SoundEngine::staticBqPlayerCallback(SLBufferQueueItf itf, void *context) {
    SoundEngine* tmp;
    if((tmp = instance) != nullptr){
        tmp->bqPlayerCallback(itf, context);
    }
}

// Called when a buffer finishes playing
void SoundEngine::bqPlayerCallback(SLBufferQueueItf itf, void *context) {
    BufferQueue* avail = nullptr;

    for(BufferQueue* bq : bufferQueues){
        if(*bq->queue == itf){
            avail = bq;
            avail->playing = false;
            break;
        }
    }
}

bool SoundEngine::createBufferQueueAudioPlayer() {
    SLresult result;

    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, samplingRate, bitrate, bitrate,  SL_SPEAKER_FRONT_RIGHT | SL_SPEAKER_FRONT_LEFT, SL_BYTEORDER_LITTLEENDIAN};

    SLDataSource audioSource = {&loc_bufq, &format_pcm};

    const SLInterfaceID playerIds[3] = {SL_IID_BUFFERQUEUE, SL_IID_PLAY, SL_IID_VOLUME};
    const SLboolean playerReqs[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    for(int i =  0; i < maxStreams; i++){
        auto* bf = new BufferQueue();
        bf->playing = false;
        bf->queue = new SLBufferQueueItf();
        bf->player = new SLObjectItf();
        bf->playerPlay = new SLPlayItf();
        bf->volume = new SLVolumeItf();
        bf->specs.channels = 2;
        bf->specs.samplingRate = samplingRate;
        bf->specs.bps = bitrate;

        SLDataLocator_OutputMix loc_mix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
        SLDataSink audioSnk = {&loc_mix, nullptr};

        result = (*engineEngine)->CreateAudioPlayer(engineEngine, &*bf->player, &audioSource, &audioSnk, 3, playerIds, playerReqs);
        check(result);

        result = (**bf->player)->Realize(*bf->player, SL_BOOLEAN_FALSE);
        check(result);

        result = (**bf->player)->GetInterface(*bf->player, SL_IID_PLAY, &*bf->playerPlay);
        check(result);

        result = (**bf->player)->GetInterface(*bf->player, SL_IID_BUFFERQUEUE, &*bf->queue);
        check(result);

        result = (**bf->queue)->RegisterCallback(*bf->queue, staticBqPlayerCallback, nullptr);
        check(result);

        result = (**bf->player)->GetInterface(*bf->player, SL_IID_VOLUME, &*bf->volume);
        check(result);

        if(i == 0){
            result = (**bf->volume)->GetMaxVolumeLevel(*bf->volume, &maxVolume);
            check(result);
        }

        result = (**bf->playerPlay)->SetPlayState(*bf->playerPlay, SL_PLAYSTATE_PLAYING);
        check(result);

        bufferQueues.push_back(bf);
    }
    LOGI("Made %i audio streams", maxStreams);

    return true;
}

bool SoundEngine::updateSpecs(BufferQueue* bf, const ResourceSpecs& specs){
    SLresult result;

    // Destroy the previous player (if there is any)
    if(bf->player){
        (**bf->player)->Destroy(*bf->player);
        bf->player = nullptr;
        bf->playerPlay = nullptr;
        bf->volume = nullptr;
        bf->queue = nullptr;
    }

    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLuint32 channelMask = specs.channels == (SLuint32)1 ? SL_SPEAKER_FRONT_CENTER : SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    LOGI("Channels: %u (m:%u), SR: %u, BPS: %u", specs.channels, channelMask, specs.samplingRate, specs.bps);
    LOGI("Channels: %u (m:%u), SR: %u, BPS: %u", bf->specs.channels, channelMask, bf->specs.samplingRate, bf->specs.bps);
    SLDataFormat_PCM format_pcm;
    if(specs.channels == 1)
        format_pcm = {SL_DATAFORMAT_PCM, 1, specs.samplingRate, specs.bps, specs.bps, SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
    else
        format_pcm = {SL_DATAFORMAT_PCM, 2, specs.samplingRate, specs.bps, specs.bps, SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSource = {&loc_bufq, &format_pcm};

    const SLInterfaceID playerIds[3] = {SL_IID_BUFFERQUEUE, SL_IID_PLAY, SL_IID_VOLUME};
    const SLboolean playerReqs[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    SLDataLocator_OutputMix loc_mix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_mix, nullptr};

    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &*bf->player, &audioSource, &audioSnk, 3, playerIds, playerReqs);
    check(result);

    result = (**bf->player)->Realize(*bf->player, SL_BOOLEAN_FALSE);
    check(result);

    result = (**bf->player)->GetInterface(*bf->player, SL_IID_PLAY, &bf->playerPlay);
    check(result);

    result = (**bf->player)->GetInterface(*bf->player, SL_IID_BUFFERQUEUE, &*bf->queue);
    check(result);

    result = (**bf->queue)->RegisterCallback(*bf->queue, staticBqPlayerCallback, nullptr);
    check(result);

    result = (**bf->player)->GetInterface(*bf->player, SL_IID_VOLUME, &*bf->volume);
    check(result);

    result = (**bf->playerPlay)->SetPlayState(*bf->playerPlay, SL_PLAYSTATE_PLAYING);
    check(result);

    return true;
}

SoundEngine::~SoundEngine() {

    for(auto* bf : bufferQueues) delete bf;
    delete &bufferQueues;

    for(auto* sample : samples) delete sample;
    delete &samples;

    // Delete output mix
    if(outputMixObject){
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = nullptr;
    }

    // Delete engine
    if(engineObject){
        (*engineObject)->Destroy(engineObject);
        engineObject = nullptr;
        engineEngine = nullptr;
    }
}

void SoundEngine::stop(int streamId) {
    BufferQueue* bf = bufferQueues[streamId - 1];
    if(bf && bf->playing){
        (**bf->playerPlay)->SetPlayState(*bf->playerPlay, SL_PLAYSTATE_PAUSED);
    }
}

void SoundEngine::setVolume(int streamId, float volume) {
    BufferQueue* bf = bufferQueues[streamId - 1];
    if(bf && bf->playing){
        SLmillibel minVol = minVolume;
        if(minVol < MIN_VOL_MILLIBEL)
            minVol = MIN_VOL_MILLIBEL;

        SLmillibel newVolume = ((float)(minVol - maxVolume) * (1.0f - volume)) + (float)maxVolume;
        (**bf->volume)->SetVolumeLevel(*bf->volume, newVolume);
    }
}

void SoundEngine::resume(int streamId) {
    BufferQueue* bf = bufferQueues[streamId - 1];
    if(bf && bf->playing){
        (**bf->playerPlay)->SetPlayState(*bf->playerPlay, SL_PLAYSTATE_PLAYING);
    }
}

int SoundEngine::play(int sampleId, float volume) {
    int streamId = 0;
    LOGI("Playing sample %i", sampleId);
    // Search for an empty channel
    BufferQueue* avail = nullptr;
    int i = 0;
    for(;i < bufferQueues.size(); i++){
        if(bufferQueues[i] && !bufferQueues[i]->playing){
            avail = bufferQueues[i];
            avail->playing = true;
            streamId = i + 1;
            break;
        }
    }

    if(avail){
        if(sampleId > samples.size() + 1 || samples[sampleId - 1] == nullptr){
            LOGE("No sample found");
            return 0;
        }



        ResourceBuffer* buf = samples[sampleId - 1];
        if(!(avail->specs == buf->specs)){
            updateSpecs(avail, buf->specs);
        }
        SLresult result;

        SLmillibel minVol = minVolume;
        if(minVol < MIN_VOL_MILLIBEL)
            minVol = MIN_VOL_MILLIBEL;

        SLmillibel newVolume = ((float)(minVol - maxVolume) * (1.0f - volume)) + (float)maxVolume;

        result = (**avail->volume)->SetVolumeLevel(*avail->volume, newVolume);
        check(result);

        result = (**avail->queue)->Enqueue(*avail->queue, (void*)(char*)buf->buffer, buf->size);
        if(result != SL_RESULT_SUCCESS){
            LOGE("Failed to enqueue sample");
        }
    }else{
        LOGI("No channels available for playing");
    }
    return streamId;
}
