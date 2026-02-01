package coelho.msftauth.api.xbox;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;
import okhttp3.Request.Builder;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class XboxTitleAuthRequest extends APIRequest<XboxTitleToken> {
    private transient XboxDeviceKey deviceKey;
    @SerializedName("Properties")
    private Properties properties;
    @SerializedName("RelyingParty")
    private String relyingParty;
    @SerializedName("TokenType")
    private String tokenType;

    private static class Properties {
        @SerializedName("AuthMethod")
        public String authMethod;
        @SerializedName("DeviceToken")
        public String deviceToken;
        @SerializedName("RpsTicket")
        public String rpsTicket;
        @SerializedName("SiteName")
        public String siteName;

        private Properties() {
        }
    }

    public XboxTitleAuthRequest(String relyingParty, String tokenType, String authMethod, String siteName, String rpsTicket, XboxToken deviceToken, XboxDeviceKey key) {
        this(relyingParty, tokenType, authMethod, siteName, rpsTicket, deviceToken.getToken(), key);
    }

    public XboxTitleAuthRequest(String relyingParty, String tokenType, String authMethod, String siteName, String rpsTicket, String deviceToken, XboxDeviceKey key) {
        this.properties = new Properties();
        this.relyingParty = relyingParty;
        this.tokenType = tokenType;
        this.properties.authMethod = authMethod;
        this.properties.siteName = siteName;
        this.properties.rpsTicket = rpsTicket;
        this.properties.deviceToken = deviceToken;
        this.deviceKey = key;
    }

    public void applyHeader(Builder requestBuilder) {
        requestBuilder.header("x-xbl-contract-version", "1");
        this.deviceKey.sign(requestBuilder);
    }

    public String getHttpURL() {
        return "https://title.auth.xboxlive.com/title/authenticate";
    }

    public APIEncoding getRequestEncoding() {
        return APIEncoding.JSON;
    }

    public APIEncoding getResponseEncoding() {
        return APIEncoding.JSON;
    }

    public Class<XboxTitleToken> getResponseClass() {
        return XboxTitleToken.class;
    }
}
