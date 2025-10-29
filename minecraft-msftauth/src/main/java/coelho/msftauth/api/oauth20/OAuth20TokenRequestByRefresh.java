package coelho.msftauth.api.oauth20;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class OAuth20TokenRequestByRefresh extends APIRequest<OAuth20Token> {
    @SerializedName("client_id")
    private String clientId;
    @SerializedName("grant_type")
    private String grantType = "refresh_token";
    @SerializedName("redirect_uri")
    private String redirectURI = OAuth20Util.REDIRECT_URI;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("scope")
    private String scope;

    public OAuth20TokenRequestByRefresh(String clientId, String refreshToken, String scope) {
        this.clientId = clientId;
        this.refreshToken = refreshToken;
        this.scope = scope;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getGrantType() {
        return this.grantType;
    }

    public String getRedirectURI() {
        return this.redirectURI;
    }

    public String getScope() {
        return this.scope;
    }

    public String getHttpURL() {
        return "https://login.live.com/oauth20_token.srf";
    }

    public APIEncoding getRequestEncoding() {
        return APIEncoding.QUERY;
    }

    public APIEncoding getResponseEncoding() {
        return APIEncoding.JSON;
    }

    public Class<OAuth20Token> getResponseClass() {
        return OAuth20Token.class;
    }
}
