package coelho.msftauth.api.xbox;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;
import okhttp3.Request.Builder;

@SuppressWarnings("FieldMayBeFinal")
public class XboxXSTSAuthRequest extends APIRequest<XboxToken> {
    private transient XboxDeviceKey deviceKey;
    @SerializedName("Properties")
    private Properties properties;
    @SerializedName("RelyingParty")
    private String relyingParty;
    @SerializedName("TokenType")
    private String tokenType;

    private static class Properties {
        @SerializedName("DeviceToken")
        public String deviceToken;
        @SerializedName("SandboxId")
        public String sandboxId;
        @SerializedName("TitleToken")
        public String titleToken;
        @SerializedName("UserTokens")
        public List<String> userTokens;

        private Properties() {
        }
    }

    public XboxXSTSAuthRequest(String relyingParty, String tokenType, String sandboxId, XboxToken userToken) {
        this(relyingParty, tokenType, sandboxId, Collections.singletonList(userToken));
    }

    public XboxXSTSAuthRequest(String relyingParty, String tokenType, String sandboxId, XboxToken userToken, XboxToken titleToken, XboxDevice device) {
        this(relyingParty, tokenType, sandboxId, Collections.singletonList(userToken), titleToken, device);
    }

    public XboxXSTSAuthRequest(String relyingParty, String tokenType, String sandboxId, List<XboxToken> userTokens) {
        this(relyingParty, tokenType, sandboxId, userTokens, null, null);
    }

    public XboxXSTSAuthRequest(String relyingParty, String tokenType, String sandboxId, List<XboxToken> userTokens, XboxToken titleToken, XboxDevice device) {
        this.properties = new Properties();
        this.relyingParty = relyingParty;
        this.tokenType = tokenType;
        this.properties.sandboxId = sandboxId;
        this.properties.userTokens = transformList(userTokens);
        if (titleToken != null) {
            this.properties.titleToken = titleToken.getToken();
        }
        if (device != null) {
            this.properties.deviceToken = device.getToken().getToken();
            this.deviceKey = device.getKey();
        }
    }

    public void applyHeader(Builder requestBuilder) {
        requestBuilder.header("x-xbl-contract-version", "1");
        if (this.deviceKey != null) {
            this.deviceKey.sign(requestBuilder);
        }
    }

    public String getRelyingParty() {
        return this.relyingParty;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public String getSandboxId() {
        return this.properties.sandboxId;
    }

    public List<String> getUserTokens() {
        return Collections.unmodifiableList(this.properties.userTokens);
    }

    public String getHttpURL() {
        return "https://xsts.auth.xboxlive.com/xsts/authorize";
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

    public static List<String> transformList(List<XboxToken> userTokens) {
        ArrayList<String> stringTokens = new ArrayList<>();
        for (XboxToken token : userTokens) {
            stringTokens.add(token.getToken());
        }
        return stringTokens;
    }
}
