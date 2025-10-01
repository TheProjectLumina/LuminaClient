cmake_minimum_required(VERSION 3.22.1)

project("pixie")

set(CMAKE_CXX_STANDARD 20)

set(LINKER_FLAGS "-ffixed-x18 -Wl,--hash-style=both")
set(C_FLAGS "-Werror=format -fdata-sections -ffunction-sections -std=c2x -Wno-error=format-security -w -fno-rtti -fpermissive")
set(CXX_FLAGS "${CXX_FLAGS} -Wno-error=format-security -ffunction-sections -fdata-sections -w -Werror -s -std=c++20 -Wno-error=c++20-narrowing -fms-extensions -fno-rtti -fpermissive")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${C_FLAGS} -O0")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${C_FLAGS} ${CXX_FLAGS} -O0")

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${LINKER_FLAGS}")
set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} ${LINKER_FLAGS}")

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/includes
        ${CMAKE_CURRENT_SOURCE_DIR}/misc
        ${CMAKE_CURRENT_SOURCE_DIR}/misc/dobby/include
        ${CMAKE_CURRENT_SOURCE_DIR}/misc/imgui
        ${CMAKE_CURRENT_SOURCE_DIR}/misc/xdl
        ${CMAKE_CURRENT_SOURCE_DIR}/misc/dobby
        ${CMAKE_CURRENT_SOURCE_DIR}/misc/imgui/backends
)

add_library(pixie SHARED
        Muffin.cpp
        puffs/riruru.cpp
        misc/imgui/imgui.cpp
        misc/imgui/imgui_draw.cpp
        misc/imgui/imgui_tables.cpp
        misc/imgui/imgui_widgets.cpp
        misc/imgui/backends/imgui_impl_android.cpp
        misc/imgui/backends/imgui_impl_opengl3.cpp
        misc/xdl/xdl.c
        misc/xdl/xdl_iterate.c
        misc/xdl/xdl_linker.c
        misc/xdl/xdl_lzma.c
        misc/xdl/xdl_util.c
)

set(DOBBY_LIB ${CMAKE_CURRENT_SOURCE_DIR}/misc/dobby/${CMAKE_ANDROID_ARCH_ABI}/libdobby.a)

target_link_libraries(pixie android EGL GLESv3 log ${DOBBY_LIB})