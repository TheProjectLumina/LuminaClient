package coelho.msftauth.api.minecraft;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;

@SuppressWarnings("FieldMayBeFinal")
public class MinecraftProfileRequest extends APIRequest<MinecraftProfile> {
    @SerializedName("token")
    private MinecraftToken token;

    public MinecraftProfileRequest(MinecraftToken token) {
        this.token = token;
    }

    public MinecraftToken getToken() {
        return this.token;
    }

    public String getHttpURL() {
        return "https://api.minecraftservices.com/minecraft/profile";
    }

    public String getHttpAuthorization() {
        return this.token.toHttpAuthorization();
    }

    public APIEncoding getRequestEncoding() {
        return null;
    }

    public APIEncoding getResponseEncoding() {
        return APIEncoding.JSON;
    }

    public Class<MinecraftProfile> getResponseClass() {
        return MinecraftProfile.class;
    }
}
