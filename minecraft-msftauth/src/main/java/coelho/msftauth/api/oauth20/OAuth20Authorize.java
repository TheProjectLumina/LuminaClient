package coelho.msftauth.api.oauth20;

import java.util.Objects;

import okhttp3.HttpUrl;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class OAuth20Authorize {
    private String clientId;
    private String redirectURI = OAuth20Util.REDIRECT_URI;
    private String responseType;
    private String scope;
    private String url;

    public OAuth20Authorize(String clientId, String responseType, String scope) {
        this.clientId = clientId;
        this.responseType = responseType;
        this.scope = scope;
        this.url = Objects.requireNonNull(HttpUrl.parse(OAuth20Util.AUTHORIZE_URI)).newBuilder().addQueryParameter("client_id", this.clientId).addQueryParameter("response_type", this.responseType).addQueryParameter("scope", this.scope).addQueryParameter("redirect_uri", this.redirectURI).build().toString();
    }

    public String getURL() {
        return this.url;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getResponseType() {
        return this.responseType;
    }

    public String getScope() {
        return this.scope;
    }
}
