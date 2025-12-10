/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * This is open source — not open credit.
 *
 * If you're here to build, welcome. If you're here to repaint and reupload
 * with your tag slapped on it… you're not fooling anyone.
 *
 * Changing colors and class names doesn't make you a developer.
 * Copy-pasting isn't contribution.
 *
 * You have legal permission to fork. But ask yourself — are you improving,
 * or are you just recycling someone else's work to feed your ego?
 *
 * Open source isn't about low-effort clones or chasing clout.
 * It's about making things better. Sharper. Cleaner. Smarter.
 *
 * So go ahead, fork it — but bring something new to the table,
 * or don’t bother pretending.
 *
 * This message is philosophical. It does not override your legal rights under GPLv3.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * GPLv3 Summary:
 * - You have the freedom to run, study, share, and modify this software.
 * - If you distribute modified versions, you must also share the source code.
 * - You must keep this license and copyright intact.
 * - You cannot apply further restrictions — the freedom stays with everyone.
 * - This license is irrevocable, and applies to all future redistributions.
 *
 * Full text: https://www.gnu.org/licenses/gpl-3.0.html
 */

#include <jni.h>
#include <string>

jstring createJavaString(JNIEnv* env, const char* str) {
    return env->NewStringUTF(str);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_project_lumina_relay_LuminaRelay_getNativeDefaultIp(JNIEnv* env, jclass clazz) {
    return createJavaString(env, "0.0.0.0");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_project_lumina_relay_LuminaRelay_getNativeDefaultPort(JNIEnv* env, jclass clazz) {
    return 19132;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_project_lumina_relay_LuminaRelay_getNativeRemoteIp(JNIEnv* env, jclass clazz) {
    return createJavaString(env, "geo.hivebedrock.network");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_project_lumina_relay_LuminaRelay_getNativeRemotePort(JNIEnv* env, jclass clazz) {
    return 19132;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_project_lumina_relay_LuminaRelay_createNativeAdvertisement(JNIEnv* env, jclass clazz) {
    jclass pongClass = env->FindClass("org/cloudburstmc/protocol/bedrock/BedrockPong");
    if (!pongClass) {
        return nullptr;
    }

    jmethodID pongConstructor = env->GetMethodID(pongClass, "<init>", "()V");
    if (!pongConstructor) {
        env->DeleteLocalRef(pongClass);
        return nullptr;
    }

    jobject pongObj = env->NewObject(pongClass, pongConstructor);
    if (!pongObj) {
        env->DeleteLocalRef(pongClass);
        return nullptr;
    }

    jmethodID setEdition = env->GetMethodID(pongClass, "edition", "(Ljava/lang/String;)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");
    jmethodID setGameType = env->GetMethodID(pongClass, "gameType", "(Ljava/lang/String;)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");
    jmethodID setVersion = env->GetMethodID(pongClass, "version", "(Ljava/lang/String;)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");
    jmethodID setProtocolVersion = env->GetMethodID(pongClass, "protocolVersion", "(I)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");
    jmethodID setMotd = env->GetMethodID(pongClass, "motd", "(Ljava/lang/String;)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");
    jmethodID setPlayerCount = env->GetMethodID(pongClass, "playerCount", "(I)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");
    jmethodID setMaxPlayerCount = env->GetMethodID(pongClass, "maximumPlayerCount", "(I)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");
    jmethodID setSubMotd = env->GetMethodID(pongClass, "subMotd", "(Ljava/lang/String;)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");
    jmethodID setNintendoLimited = env->GetMethodID(pongClass, "nintendoLimited", "(Z)Lorg/cloudburstmc/protocol/bedrock/BedrockPong;");

    if (!setEdition || !setGameType || !setVersion || !setProtocolVersion || !setMotd ||
        !setPlayerCount || !setMaxPlayerCount || !setSubMotd || !setNintendoLimited) {
        env->DeleteLocalRef(pongObj);
        env->DeleteLocalRef(pongClass);
        return nullptr;
    }
    env->CallObjectMethod(pongObj, setEdition, createJavaString(env, "MCPE"));
    env->CallObjectMethod(pongObj, setGameType, createJavaString(env, "Survival"));
    env->CallObjectMethod(pongObj, setVersion, createJavaString(env, "1.21.130"));
    env->CallObjectMethod(pongObj, setProtocolVersion, 898);
    env->CallObjectMethod(pongObj, setMotd, createJavaString(env, "§bWelcome To Lunaris§b"));
    env->CallObjectMethod(pongObj, setPlayerCount, 0);
    env->CallObjectMethod(pongObj, setMaxPlayerCount, 20);
    env->CallObjectMethod(pongObj, setSubMotd, createJavaString(env, "Lumina Client"));
    env->CallObjectMethod(pongObj, setNintendoLimited, JNI_FALSE);
    env->DeleteLocalRef(pongClass);

    return pongObj;
}