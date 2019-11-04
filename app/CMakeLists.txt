cmake_minimum_required(VERSION 3.4.1)

# Build our own native library
add_library (native-lib
        SHARED
        src/main/cpp/SoundGenerator.cpp
        src/main/cpp/SoundGenerator.h
        )

# Set the path to the Oboe directory.
set (OBOE_DIR ../oboe)

# Add the Oboe library as a subdirectory in your project.
# add_subdirectory tells CMake to look in this directory to
# compile oboe source files using oboe's CMake file.
# ./oboe specifies where the compiled binaries will be stored
add_subdirectory (${OBOE_DIR} ./oboe)

# Specify the path to the Oboe header files.
# This allows targets compiled with this CMake (application code)
# to see public Oboe headers, in order to access its API.
include_directories (${OBOE_DIR}/include)

# Specify the libraries which our native library is dependent on, including Oboe
target_link_libraries (
        native-lib
        log
        oboe)