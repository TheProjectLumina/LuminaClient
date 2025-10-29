@file:Suppress("VulnerableLibrariesLocal")

plugins {
    id("java-library")
    alias(libs.plugins.lombok)
    alias(libs.plugins.checkerframework)
}




dependencies {
    compileOnly(libs.netty.transport.raknet)
    api(project(":Protocol:bedrock-codec"))
    api(libs.snappy)
}