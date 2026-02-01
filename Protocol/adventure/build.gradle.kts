plugins {
    id("java-library")
    alias(libs.plugins.lombok)
    alias(libs.plugins.checkerframework)
}


dependencies {
    api(project(":Protocol:bedrock-codec"))
    api(libs.adventure.text.serializer.legacy)
    api(libs.adventure.text.serializer.json)

    // Tests
    testImplementation(libs.junit)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.cloudburstmc.protocol.adventure")
    }
}
