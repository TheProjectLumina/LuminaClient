#include <jni.h>
#include <string>
#include <stdexcept>
#include <android/log.h>
#include <cstdlib>
#include <Obfuscate.h>
#include <vector>



static const std::string base64_chars =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

std::string zxq_r9j(const std::vector<const char*>& segments) {
    std::string merged;
    for (const auto& seg : segments) {
        merged += seg;
    }
    return merged;
}

static std::string base64_decode(const std::string &encoded_string) {
    std::string decoded;
    int in_len = encoded_string.size();
    int i = 0;
    int j = 0;
    int in_ = 0;
    unsigned char char_array_4[4], char_array_3[3];

    while (in_len-- && (encoded_string[in_] != '=') &&
           (base64_chars.find(encoded_string[in_]) != std::string::npos)) {
        char_array_4[i++] = encoded_string[in_]; in_++;
        if (i == 4) {
            for (i = 0; i < 4; i++)
                char_array_4[i] = base64_chars.find(char_array_4[i]);

            char_array_3[0] = (char_array_4[0] << 2) + ((char_array_4[1] & 0x30) >> 4);
            char_array_3[1] = ((char_array_4[1] & 0xf) << 4) + ((char_array_4[2] & 0x3c) >> 2);
            char_array_3[2] = ((char_array_4[2] & 0x3) << 6) + char_array_4[3];

            for (i = 0; i < 3; i++)
                decoded += char_array_3[i];
            i = 0;
        }
    }

    if (i) {
        for (j = i; j < 4; j++)
            char_array_4[j] = 0;

        for (j = 0; j < 4; j++)
            char_array_4[j] = base64_chars.find(char_array_4[j]);

        char_array_3[0] = (char_array_4[0] << 2) + ((char_array_4[1] & 0x30) >> 4);
        char_array_3[1] = ((char_array_4[1] & 0xf) << 4) + ((char_array_4[2] & 0x3c) >> 2);

        for (j = 0; j < i - 1; j++)
            decoded += char_array_3[j];
    }

    return decoded;
}


static bool parseUpdateField(const std::string &jsonString) {
    try {

        std::string searchTrue = "\"update\"";
        size_t pos = jsonString.find(searchTrue);
        if (pos == std::string::npos) {
            return false;
        }


        pos = jsonString.find(":", pos + searchTrue.length());
        if (pos == std::string::npos) {
            return false;
        }


        while (pos < jsonString.length() && (jsonString[pos] == ':' || jsonString[pos] == ' ' || jsonString[pos] == '\t' || jsonString[pos] == '\n')) {
            pos++;
        }


        std::string trueValue = "true";
        std::string falseValue = "false";
        if (jsonString.substr(pos, trueValue.length()) == trueValue) {
            return true;
        } else if (jsonString.substr(pos, falseValue.length()) == falseValue) {
            return false;
        } else {

            return false;
        }
    } catch (const std::exception &e) {
        return false;
    }
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_project_lumina_client_util_UpdateCheck_resolveEndpoint(JNIEnv *env, jobject) {
    try {
        const char* k1[] = { "aH", "R0", "cH", "M6" };
        const char* k2[] = { "Ly", "9u", "eX", "hl" };
        const char* k3[] = { "bG", "xl", "Lm", "5l" };
        const char* k4[] = { "dG", "xp", "Zn", "ku" };
        const char* k5[] = { "YXB", "wL", "0Z", "pb" };
        const char* k6[] = { "GV", "zL", "3N", "lcn" };
        const char* k7[] = { "Zp", "Y2", "Uv", "Y2" };
        const char* k8[] = { "hl", "Y2", "th", "Lmp" };
        const char* k9[] = { "zb2", "4=" };

        std::vector<const char*> obf = {
                k1[0], k1[1], k1[2], k1[3],
                k2[0], k2[1], k2[2], k2[3],
                k3[0], k3[1], k3[2], k3[3],
                k4[0], k4[1], k4[2], k4[3],
                k5[0], k5[1], k5[2], k5[3],
                k6[0], k6[1], k6[2], k6[3],
                k7[0], k7[1], k7[2], k7[3],
                k8[0], k8[1], k8[2], k8[3],
                k9[0], k9[1]
        };

        std::string e = zxq_r9j(obf);
        std::string d = base64_decode(e);
        return env->NewStringUTF(d.c_str());

    } catch (...) {
        return env->NewStringUTF("");
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_project_lumina_client_util_UpdateCheck_retrieveFallback(JNIEnv *env, jobject /* this */) {
    try {
        std::string message = OBFUSCATE("Outdated NORT");
        return env->NewStringUTF(message.c_str());
    } catch (const std::exception &e) {
        return env->NewStringUTF("");
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_project_lumina_client_util_UpdateCheck_verifySignature(JNIEnv *env, jobject /* this */, jstring jsonString) {
    try {
        const char *jsonStr = env->GetStringUTFChars(jsonString, nullptr);
        if (jsonStr == nullptr) {
            return JNI_FALSE;
        }
        std::string jsonCppStr(jsonStr);
        env->ReleaseStringUTFChars(jsonString, jsonStr);
        bool result = parseUpdateField(jsonCppStr);

        return result ? JNI_TRUE : JNI_FALSE;
    } catch (const std::exception &e) {
        return JNI_FALSE;
    }
}