cmake_minimum_required(VERSION 3.18)

if(ANDROID_ABI STREQUAL "armeabi-v7a")
    message(WARNING "Skipping unsupported architecture: ${ANDROID_ABI}")
    return()
endif()

message(STATUS "Building libadrenotools for ${ANDROID_ABI}")
