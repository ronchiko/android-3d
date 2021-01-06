#pragma once

#include <cstdint>
#include <string>

typedef struct {
	// RIFF Header
	char riff[4];		// == "RIFF"
	int32_t chunkSize;	// 36 + Chunk 2 size
	char wave[4];		// == "WAVE"

	// Format chunk
	char format[4];			// == "fmt "
	int32_t subChunk1Size;
	int16_t audioFormat;	// 1 = PCM
	int16_t numChannels;
	int32_t sampleRate;
	int32_t byteRate;		// sampleRate * numChannels * bitsPerSample / 8 
	int16_t blockAlign;		// numChannels * bitsPerSample / 8
	int16_t bitsPerSample;	// Multiple of 8

	// Data chunk
	char data[4];	// == "data"
} WavePcmHeader;

typedef struct {
	int32_t subChunk2Size;
	union {
		void* rawVoid;
		char* raw8;
		int16_t* raw16;
		int32_t* raw32;
		int64_t* raw64;
	};
} WavePcmData;

class WavePcm {
public:
	WavePcm(const std::string&);
	WavePcm(char*, size_t);
	~WavePcm();

	WavePcmData& wpcmData() { return data; }
	WavePcmHeader& wpcmh() { return header; }
	bool isvalid() const { return valid; }
	int64_t operator[](int index) const;
private:
	void setbuf(std::istream&);
	bool validate() const;

	WavePcmHeader header;
	WavePcmData data;
	bool valid;
};