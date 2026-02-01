package coelho.msftauth.api.minecraft;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;

@SuppressWarnings("FieldMayBeFinal")
public class MinecraftEntitlementsRequest extends APIRequest<MinecraftEntitlements> {
    @SerializedName("token")
    private MinecraftToken token;

    public MinecraftEntitlementsRequest(MinecraftToken token) {
        this.token = token;
    }

    public MinecraftToken getToken() {
        return this.token;
    }

    public String getHttpURL() {
        return "https://api.minecraftservices.com/entitlements/mcstore";
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

    public Class<MinecraftEntitlements> getResponseClass() {
        return MinecraftEntitlements.class;
    }
}
