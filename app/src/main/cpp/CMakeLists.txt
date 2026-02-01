cmake_minimum_required(VERSION 3.22.1)

project("client")

add_library(${CMAKE_PROJECT_NAME} SHARED
        hsv_to_rgb
        hsv_to_rgb.cpp
        jni_bridge.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log)
