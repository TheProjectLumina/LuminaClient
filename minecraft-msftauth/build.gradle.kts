@file:Suppress("VulnerableLibrariesLocal")

plugins {
    id("java-library")
}



dependencies {
    api(libs.guava)
    api(libs.gson)
    api(libs.http.client)
    api(libs.bcprov)
    api(libs.okhttp)
}