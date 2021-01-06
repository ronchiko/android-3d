
#include <fstream>
#include <iostream>
#include <android/log.h>

#include "wave_pcm.h"

#define LOG_TAG "Engine (Audio)"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct membuf : std::streambuf {
	membuf(char* data, int32_t size) {
		setg(data, data, data + size);
	}
};

static inline int32_t readi32(std::istream& stream) {
	char buf[4];
	stream.read(buf, 4);
	return *(int32_t*)buf;
}
static inline int16_t readi16(std::istream& stream) {
	char buf[2];
	stream.read(buf, 2);
	return *(int16_t*)buf;
}

static inline bool cmpstring(const char* a, const char* b, int size) {
	for (int i = 0; i < size; i++) if (a[i] != b[i]) return false;
	return true;
}

WavePcm::WavePcm(char* data, size_t size) : header(), data(), valid(true) {
	membuf buf(data, size);
	std::istream stream(&buf);

	setbuf(stream);

	if (valid) {
		valid = validate();
	}
}

WavePcm::WavePcm(const std::string& path) : header(), data(), valid(true) {
	std::fstream file(path, std::ios::in | std::ios::binary);

	if (!file) {
		std::cout << "Failed to open file at " << path << std::endl;
		return;
	}

	setbuf(file);

	file.close();

	if (valid) {
		valid = validate();
	}
}

void WavePcm::setbuf(std::istream& stream) {
	stream.read(header.riff, 4);
	header.chunkSize = readi32(stream);
	stream.read(header.wave, 4);

	stream.read(header.format, 4);
	header.subChunk1Size = readi32(stream);

	header.audioFormat = readi16(stream);
	header.numChannels = readi16(stream);
	header.sampleRate = readi32(stream);
	header.byteRate = readi32(stream);
	header.blockAlign = readi16(stream);
	header.bitsPerSample = readi16(stream);

	stream.read(header.data, 4);

	data.subChunk2Size = readi32(stream);

	data.rawVoid = malloc(data.subChunk2Size);
	stream.read(data.raw8, data.subChunk2Size);
}

bool WavePcm::validate() const {
	if (!cmpstring(header.riff, "RIFF", 4)
		|| !cmpstring(header.wave, "WAVE", 4)
		|| !cmpstring(header.format, "fmt ", 4)
		|| !cmpstring(header.data, "data", 4)) return false;

	int expectedBlockAlign = header.numChannels * (header.bitsPerSample / 8);
	int expectedByteRate = expectedBlockAlign * header.sampleRate;

	return !(expectedByteRate != header.byteRate ||
			 expectedBlockAlign != header.blockAlign);
}

int64_t WavePcm::operator[](int index) const {
	switch (header.bitsPerSample / 8) {
	case 2: return data.raw16[index];
	case 4: return data.raw32[index];
	case 8: return data.raw64[index];
	}
	return data.raw8[index];
}

WavePcm::~WavePcm() {
	if(data.rawVoid) free(data.rawVoid);
}