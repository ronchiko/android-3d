#pragma once

#include <iostream>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>


#define LOG_TAG "Engine (Audio)"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static const char *result_strings[] = {
        "SUCCESS",
        "PRECONDITIONS_VIOLATED",
        "PARAMETER_INVALID",
        "MEMORY_FAILURE",
        "RESOURCE_ERROR",
        "RESOURCE_LOST",
        "IO_ERROR",
        "BUFFER_INSUFFICIENT",
        "CONTENT_CORRUPTED",
        "CONTENT_UNSUPPORTED",
        "CONTENT_NOT_FOUND",
        "PERMISSION_DENIED",
        "FEATURE_UNSUPPORTED",
        "INTERNAL_ERROR",
        "UNKNOWN_ERROR",
        "OPERATION_ABORTED",
        "CONTROL_LOST"
};

static const char *result_to_string(SLresult result)
{
    static char buffer[32];
    if ( /* result >= 0 && */ result < sizeof(result_strings) / sizeof(result_strings[0]))
        return result_strings[result];
    LOGI(buffer, "%d", (int) result);
    return buffer;
}

// Compare result against expected and exit suddenly if wrong

inline void check2(SLresult result, int line)
{
    if (SL_RESULT_SUCCESS != result) {
        LOGI("error %s at line %d\n", result_to_string(result), line);
        exit(EXIT_FAILURE);
    }
}

// Same as above but automatically adds the source code line number

//#define check(result) check2(result, __LINE__)


// FIXME: GCC compiles OK, but Eclipse CDT displays errors for OpenSL slCreateEngine references.
// Redefining the function here seems to remove the error. Try commenting the declaration out
// after few CDT updates.
