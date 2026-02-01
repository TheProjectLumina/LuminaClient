package coelho.msftauth.api.xbox;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;
import okhttp3.Request.Builder;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class XboxDeviceAuthRequest extends APIRequest<XboxDeviceToken> {
    private transient XboxDeviceKey deviceKey;
    @SerializedName("Properties")
    private Properties properties = new Properties();
    @SerializedName("RelyingParty")
    private String relyingParty;
    @SerializedName("TokenType")
    private String tokenType;

    private static class Properties {
        @SerializedName("AuthMethod")
        public String authMethod;
        @SerializedName("DeviceType")
        public String deviceType;
        @SerializedName("Id")
        public String id;
        @SerializedName("ProofKey")
        public XboxProofKey proofKey;
        @SerializedName("Version")
        public String version;

        private Properties() {
        }
    }

    public XboxDeviceAuthRequest(String relyingParty, String tokenType, String deviceType, String version, XboxDeviceKey key) {
        this.relyingParty = relyingParty;
        this.tokenType = tokenType;
        this.properties.authMethod = "ProofOfPossession";
        this.properties.id = "{" + key.getId() + "}";
        this.properties.deviceType = deviceType;
        this.properties.version = version;
        this.properties.proofKey = key.getProofKey();
        this.deviceKey = key;
    }

    public void applyHeader(Builder requestBuilder) {
        requestBuilder.header("x-xbl-contract-version", "1");
        this.deviceKey.sign(requestBuilder);
    }

    public String getHttpURL() {
        return "https://device.auth.xboxlive.com/device/authenticate";
    }

    public APIEncoding getRequestEncoding() {
        return APIEncoding.JSON;
    }

    public APIEncoding getResponseEncoding() {
        return APIEncoding.JSON;
    }

    public Class<XboxDeviceToken> getResponseClass() {
        return XboxDeviceToken.class;
    }
}
