package coelho.msftauth.api.minecraft;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("FieldMayBeFinal")
public class MinecraftToken {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("expires_in")
    private long expiresIn;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("username")
    private String uuid;

    public MinecraftToken(String uuid, String accessToken, String tokenType, long expiresIn) {
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public long getExpiresIn() {
        return this.expiresIn;
    }

    public String toHttpAuthorization() {
        return "Bearer " + this.accessToken;
    }
}
