package coelho.msftauth.api.minecraft;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;
import coelho.msftauth.api.xbox.XboxToken;

@SuppressWarnings("FieldMayBeFinal")
public class MinecraftTokenRequest extends APIRequest<MinecraftToken> {
    @SerializedName("identityToken")
    private String identityToken;
    private transient XboxToken token;

    public MinecraftTokenRequest(XboxToken token) {
        this.token = token;
        this.identityToken = token.toIdentityToken();
    }

    public XboxToken getToken() {
        return this.token;
    }

    public String getIdentityToken() {
        return this.identityToken;
    }

    public String getHttpURL() {
        return "https://api.minecraftservices.com/authentication/login_with_xbox";
    }

    public APIEncoding getRequestEncoding() {
        return APIEncoding.JSON;
    }

    public APIEncoding getResponseEncoding() {
        return APIEncoding.JSON;
    }

    public Class<MinecraftToken> getResponseClass() {
        return MinecraftToken.class;
    }
}
