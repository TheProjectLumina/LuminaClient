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
#include <vector>
#include <algorithm>

struct CodecInfo {
    int protocolVersion;
    const char* className;
};


static const std::vector<CodecInfo> protocols = {
        {291, "org/cloudburstmc/protocol/bedrock/codec/v291/Bedrock_v291"},
        {313, "org/cloudburstmc/protocol/bedrock/codec/v313/Bedrock_v313"},
        {332, "org/cloudburstmc/protocol/bedrock/codec/v332/Bedrock_v332"},
        {340, "org/cloudburstmc/protocol/bedrock/codec/v340/Bedrock_v340"},
        {354, "org/cloudburstmc/protocol/bedrock/codec/v354/Bedrock_v354"},
        {361, "org/cloudburstmc/protocol/bedrock/codec/v361/Bedrock_v361"},
        {388, "org/cloudburstmc/protocol/bedrock/codec/v388/Bedrock_v388"},
        {389, "org/cloudburstmc/protocol/bedrock/codec/v389/Bedrock_v389"},
        {390, "org/cloudburstmc/protocol/bedrock/codec/v390/Bedrock_v390"},
        {407, "org/cloudburstmc/protocol/bedrock/codec/v407/Bedrock_v407"},
        {408, "org/cloudburstmc/protocol/bedrock/codec/v408/Bedrock_v408"},
        {419, "org/cloudburstmc/protocol/bedrock/codec/v419/Bedrock_v419"},
        {422, "org/cloudburstmc/protocol/bedrock/codec/v422/Bedrock_v422"},
        {428, "org/cloudburstmc/protocol/bedrock/codec/v428/Bedrock_v428"},
        {431, "org/cloudburstmc/protocol/bedrock/codec/v431/Bedrock_v431"},
        {440, "org/cloudburstmc/protocol/bedrock/codec/v440/Bedrock_v440"},
        {448, "org/cloudburstmc/protocol/bedrock/codec/v448/Bedrock_v448"},
        {465, "org/cloudburstmc/protocol/bedrock/codec/v465/Bedrock_v465"},
        {471, "org/cloudburstmc/protocol/bedrock/codec/v471/Bedrock_v471"},
        {475, "org/cloudburstmc/protocol/bedrock/codec/v475/Bedrock_v475"},
        {486, "org/cloudburstmc/protocol/bedrock/codec/v486/Bedrock_v486"},
        {503, "org/cloudburstmc/protocol/bedrock/codec/v503/Bedrock_v503"},
        {527, "org/cloudburstmc/protocol/bedrock/codec/v527/Bedrock_v527"},
        {534, "org/cloudburstmc/protocol/bedrock/codec/v534/Bedrock_v534"},
        {544, "org/cloudburstmc/protocol/bedrock/codec/v544/Bedrock_v544"},
        {545, "org/cloudburstmc/protocol/bedrock/codec/v545/Bedrock_v545"},
        {554, "org/cloudburstmc/protocol/bedrock/codec/v554/Bedrock_v554"},
        {557, "org/cloudburstmc/protocol/bedrock/codec/v557/Bedrock_v557"},
        {560, "org/cloudburstmc/protocol/bedrock/codec/v560/Bedrock_v560"},
        {567, "org/cloudburstmc/protocol/bedrock/codec/v567/Bedrock_v567"},
        {568, "org/cloudburstmc/protocol/bedrock/codec/v568/Bedrock_v568"},
        {575, "org/cloudburstmc/protocol/bedrock/codec/v575/Bedrock_v575"},
        {582, "org/cloudburstmc/protocol/bedrock/codec/v582/Bedrock_v582"},
        {589, "org/cloudburstmc/protocol/bedrock/codec/v589/Bedrock_v589"},
        {594, "org/cloudburstmc/protocol/bedrock/codec/v594/Bedrock_v594"},
        {618, "org/cloudburstmc/protocol/bedrock/codec/v618/Bedrock_v618"},
        {622, "org/cloudburstmc/protocol/bedrock/codec/v622/Bedrock_v622"},
        {630, "org/cloudburstmc/protocol/bedrock/codec/v630/Bedrock_v630"},
        {649, "org/cloudburstmc/protocol/bedrock/codec/v649/Bedrock_v649"},
        {662, "org/cloudburstmc/protocol/bedrock/codec/v662/Bedrock_v662"},
        {671, "org/cloudburstmc/protocol/bedrock/codec/v671/Bedrock_v671"},
        {685, "org/cloudburstmc/protocol/bedrock/codec/v685/Bedrock_v685"},
        {686, "org/cloudburstmc/protocol/bedrock/codec/v686/Bedrock_v686"},
        {712, "org/cloudburstmc/protocol/bedrock/codec/v712/Bedrock_v712"},
        {729, "org/cloudburstmc/protocol/bedrock/codec/v729/Bedrock_v729"},
        {748, "org/cloudburstmc/protocol/bedrock/codec/v748/Bedrock_v748"},
        {766, "org/cloudburstmc/protocol/bedrock/codec/v766/Bedrock_v766"},
        {786, "org/cloudburstmc/protocol/bedrock/codec/v786/Bedrock_v786"},
        {800, "org/cloudburstmc/protocol/bedrock/codec/v800/Bedrock_v800"},
        {818, "org/cloudburstmc/protocol/bedrock/codec/v818/Bedrock_v818"},
        {819, "org/cloudburstmc/protocol/bedrock/codec/v819/Bedrock_v819"},
        {827, "org/cloudburstmc/protocol/bedrock/codec/v827/Bedrock_v827"},
        {844, "org/cloudburstmc/protocol/bedrock/codec/v844/Bedrock_v844"},
        {859, "org/cloudburstmc/protocol/bedrock/codec/v859/Bedrock_v859"}
};

extern "C" JNIEXPORT jobject JNICALL
Java_com_project_lumina_relay_listener_AutoCodecPacketListener_pickProtocolCodec(JNIEnv* env, jclass clazz, jint protocolVersion) {

    jclass defaultCodecClass = env->FindClass("org/cloudburstmc/protocol/bedrock/codec/v859/Bedrock_v859");
    if (!defaultCodecClass) return nullptr;
    jfieldID codecField = env->GetStaticFieldID(defaultCodecClass, "CODEC", "Lorg/cloudburstmc/protocol/bedrock/codec/BedrockCodec;");
    if (!codecField) {
        env->DeleteLocalRef(defaultCodecClass);
        return nullptr;
    }
    jobject defaultCodec = env->GetStaticObjectField(defaultCodecClass, codecField);
    if (!defaultCodec) {
        env->DeleteLocalRef(defaultCodecClass);
        return nullptr;
    }

    jobject selectedCodec = defaultCodec;

    for (const auto& codecInfo : protocols) {
        if (codecInfo.protocolVersion > protocolVersion) break;

        jclass codecClass = env->FindClass(codecInfo.className);
        if (!codecClass) continue;

        jfieldID codecFieldID = env->GetStaticFieldID(codecClass, "CODEC", "Lorg/cloudburstmc/protocol/bedrock/codec/BedrockCodec;");
        if (!codecFieldID) {
            env->DeleteLocalRef(codecClass);
            continue;
        }

        jobject codecObj = env->GetStaticObjectField(codecClass, codecFieldID);
        if (codecObj) {
            if (selectedCodec != defaultCodec) {
                env->DeleteLocalRef(selectedCodec);
            }
            selectedCodec = codecObj;
        }
        env->DeleteLocalRef(codecClass);
    }
    jclass codecClass = env->GetObjectClass(selectedCodec);
    jmethodID toBuilderMethod = env->GetMethodID(codecClass, "toBuilder", "()Lorg/cloudburstmc/protocol/bedrock/codec/BedrockCodec$Builder;");
    if (!toBuilderMethod) {
        env->DeleteLocalRef(selectedCodec);
        env->DeleteLocalRef(defaultCodecClass);
        env->DeleteLocalRef(defaultCodec);
        return nullptr;
    }
    jobject builder = env->CallObjectMethod(selectedCodec, toBuilderMethod);
    if (!builder) {
        env->DeleteLocalRef(selectedCodec);
        env->DeleteLocalRef(defaultCodecClass);
        env->DeleteLocalRef(defaultCodec);
        return nullptr;
    }
    jclass builderClass = env->GetObjectClass(builder);
    jmethodID buildMethod = env->GetMethodID(builderClass, "build", "()Lorg/cloudburstmc/protocol/bedrock/codec/BedrockCodec;");
    if (!buildMethod) {
        env->DeleteLocalRef(builder);
        env->DeleteLocalRef(selectedCodec);
        env->DeleteLocalRef(defaultCodecClass);
        env->DeleteLocalRef(defaultCodec);
        return nullptr;
    }
    jobject finalCodec = env->CallObjectMethod(builder, buildMethod);
    env->DeleteLocalRef(builder);
    env->DeleteLocalRef(selectedCodec);
    env->DeleteLocalRef(defaultCodecClass);
    env->DeleteLocalRef(defaultCodec);

    return finalCodec;
}