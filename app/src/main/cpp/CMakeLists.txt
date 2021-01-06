# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)
# Declares and names the project.

project("opengl")
include_directories("/")
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#add_library(aaudio STATIC IMPORTED)
add_library( # Sets the name of the library.
                native-lib
                SHARED
                native_lib.cpp
                audio/engine.cpp data/files/audio/wave_pcm.cpp
                jnih/jnih_arrays.cpp
                parsers/struc_parsers.cpp parsers/wavefront_parser.cpp
                data/structs.cpp data/wavefront.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(log-lib log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(native-lib android ${log-lib} GLESv2 OpenSLES)