plugins {
    id("java-library")
    alias(libs.plugins.lombok)
    alias(libs.plugins.checkerframework)
}




dependencies {
    api(libs.netty.buffer)
    api(platform(libs.fastutil.bom))
    api(libs.fastutil.int.obj.maps)
    api(libs.fastutil.obj.int.maps)
    api(libs.math)
    api(libs.netty.buffer)
}