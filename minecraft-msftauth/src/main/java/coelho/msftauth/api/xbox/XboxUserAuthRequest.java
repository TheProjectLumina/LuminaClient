package coelho.msftauth.api.xbox;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;

@SuppressWarnings("FieldMayBeFinal")
public class XboxUserAuthRequest extends APIRequest<XboxToken> {
    @SerializedName("Properties")
    private Properties properties = new Properties();
    @SerializedName("RelyingParty")
    private String relyingParty;
    @SerializedName("TokenType")
    private String tokenType;

    private static class Properties {
        @SerializedName("AuthMethod")
        public String authMethod;
        @SerializedName("RpsTicket")
        public String rpsTicket;
        @SerializedName("SiteName")
        public String siteName;

        private Properties() {
        }
    }

    public XboxUserAuthRequest(String relyingParty, String tokenType, String authMethod, String siteName, String rpsTicket) {
        this.relyingParty = relyingParty;
        this.tokenType = tokenType;
        this.properties.authMethod = authMethod;
        this.properties.siteName = siteName;
        this.properties.rpsTicket = rpsTicket;
    }

    public String getRelyingParty() {
        return this.relyingParty;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public String getAuthMethod() {
        return this.properties.authMethod;
    }

    public String getSiteName() {
        return this.properties.siteName;
    }

    public String getRpsTicket() {
        return this.properties.rpsTicket;
    }

    public String getHttpURL() {
        return "https://user.auth.xboxlive.com/user/authenticate";
    }

    public APIEncoding getRequestEncoding() {
        return APIEncoding.JSON;
    }

    public APIEncoding getResponseEncoding() {
        return APIEncoding.JSON;
    }

    public Class<XboxToken> getResponseClass() {
        return XboxToken.class;
    }
}
