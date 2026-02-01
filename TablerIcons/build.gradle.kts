plugins {
    id("com.android.library")
    id("maven-publish")
    kotlin("android")
}

val libraryGroupId = "ir.alirezaivaz"
val libraryArtifactId = "tablericons"
val libraryVersion = "1.13.0"
val tablerIconsVersion = "3.6.0"

android {
    namespace = "ir.alirezaivaz.tablericons"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "LIBRARY_VERSION", "\"$libraryVersion\"")
        buildConfigField("String", "TABLER_ICONS_VERSION", "\"$tablerIconsVersion\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

/**
 * Generate an object that holds list of all icons
 */
task("generateIconsObject") {
    val drawableDir = File("${projectDir}/src/main/res/drawable")
    val iconObjectCode = buildString {
        appendLine("package ir.alirezaivaz.tablericons")
        appendLine()
        appendLine("object TablerIcons {")
        drawableDir.listFiles()?.sorted()?.forEach {
            val variableName = it.name.toVariableName()
            appendLine("    val $variableName = R.drawable.${it.nameWithoutExtension}")
        }
        appendLine("}")
    }
    val iconObjectFile = File("${projectDir}/src/main/kotlin/ir/alirezaivaz/tablericons/TablerIcons.kt")
    iconObjectFile.parentFile.mkdirs()
    iconObjectFile.writeText(iconObjectCode)
}

/**
 * Convert icon file name to variable name
 */
fun String.toVariableName(): String {
    return removePrefix("ic")
        .removeSuffix(".xml")
        .toCamelCase()
        .fixNumericName()
}

/**
 * Add an underscore to name of variable if it starts with number
 */
fun String.fixNumericName(): String {
    return if (this[0].isDigit()) "_$this" else this
}

/**
 * Convert variable name to camel case
 */
fun String.toCamelCase(): String {
    return split("_").joinToString("") { it.capitalize() }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = libraryGroupId
            artifactId = libraryArtifactId
            version = libraryVersion
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
