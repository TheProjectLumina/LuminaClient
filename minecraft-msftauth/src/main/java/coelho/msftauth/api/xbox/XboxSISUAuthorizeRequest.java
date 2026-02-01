package coelho.msftauth.api.xbox;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;
import okhttp3.Request.Builder;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class XboxSISUAuthorizeRequest extends APIRequest<XboxSISUAuthorize> {
    @SerializedName("AccessToken")
    private String accessToken;
    @SerializedName("AppId")
    private String appId;
    private transient XboxDeviceKey deviceKey;
    @SerializedName("DeviceToken")
    private String deviceToken;
    @SerializedName("ProofKey")
    private XboxProofKey proofKey;
    @SerializedName("RelyingParty")
    private String relyingParty;
    @SerializedName("Sandbox")
    private String sandbox;
    @SerializedName("SessionId")
    private String sessionId;
    @SerializedName("SiteName")
    private String siteName;
    @SerializedName("UseModernGamertag")
    private boolean useModernGamertag = true;

    public XboxSISUAuthorizeRequest(String accessToken, String appId, XboxDevice device, String sandbox, String sessionId, String siteName, String relyingParty) {
        this.accessToken = accessToken;
        this.appId = appId;
        this.deviceToken = device.getToken().getToken();
        this.proofKey = device.getProofKey();
        this.sandbox = sandbox;
        this.sessionId = sessionId;
        this.siteName = siteName;
        this.relyingParty = relyingParty;
        this.deviceKey = device.getKey();
    }

    public void applyHeader(Builder requestBuilder) {
        requestBuilder.header("x-xbl-contract-version", "1");
        this.deviceKey.sign(requestBuilder);
    }

    public String getHttpURL() {
        return "https://sisu.xboxlive.com/authorize";
    }

    public APIEncoding getRequestEncoding() {
        return APIEncoding.JSON;
    }

    public APIEncoding getResponseEncoding() {
        return APIEncoding.JSON;
    }

    public Class<XboxSISUAuthorize> getResponseClass() {
        return XboxSISUAuthorize.class;
    }
}
