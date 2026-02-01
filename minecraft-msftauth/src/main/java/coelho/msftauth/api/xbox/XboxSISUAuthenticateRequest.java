package coelho.msftauth.api.xbox;

import com.google.gson.annotations.SerializedName;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import coelho.msftauth.api.APIEncoding;
import coelho.msftauth.api.APIRequest;
import okhttp3.Request.Builder;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class XboxSISUAuthenticateRequest extends APIRequest<XboxSISUAuthenticate> {
    @SerializedName("AppId")
    private String appId;
    private transient XboxDeviceKey deviceKey;
    @SerializedName("DeviceToken")
    private String deviceToken;
    @SerializedName("Offers")
    private List<String> offers;
    @SerializedName("Query")
    private Query query;
    @SerializedName("RedirectUri")
    private String redirectURI;
    @SerializedName("Sandbox")
    private String sandbox;
    @SerializedName("TokenType")
    private String tokenType = "code";

    public static class Query {
        @SerializedName("code_challenge")
        private String codeChallenge;
        @SerializedName("code_challenge_method")
        private String codeChallengeMethod;
        @SerializedName("display")
        private String display;
        @SerializedName("state")
        private String state;

        public Query(String codeChallenge, String codeChallengeMethod, String state, String display) {
            this.codeChallenge = codeChallenge;
            this.codeChallengeMethod = codeChallengeMethod;
            this.state = state;
            this.display = display;
        }

        public Query(String display) throws Exception {
            this.codeChallenge = getCodeChallengeFromCodeVerifier(generateCodeVerifier());
            this.codeChallengeMethod = "S256";
            this.state = generateRandomState();
            this.display = display;
        }

        public static String generateCodeVerifier() {
            byte[] randomBytes = new byte[32];
            new Random().nextBytes(randomBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        }

        public static String getCodeChallengeFromCodeVerifier(String codeVerifier) throws Exception {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes()));
        }

        public static String generateRandomState() {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(UUID.randomUUID().toString().getBytes());
        }

        public String getState() {
            return this.state;
        }

        public String getCodeChallenge() {
            return this.codeChallenge;
        }

        public String getCodeChallengeMethod() {
            return this.codeChallengeMethod;
        }

        public String getDisplay() {
            return this.display;
        }
    }

    public XboxSISUAuthenticateRequest(String appId, XboxDevice device, String offer, Query query, String redirectURI, String sandbox) {
        this.appId = appId;
        this.deviceKey = device.getKey();
        this.deviceToken = device.getToken().getToken();
        this.offers = Collections.singletonList(offer);
        this.query = query;
        this.redirectURI = redirectURI;
        this.sandbox = sandbox;
    }

    public void applyHeader(Builder requestBuilder) {
        requestBuilder.header("x-xbl-contract-version", "1");
        this.deviceKey.sign(requestBuilder);
    }

    public String getHttpURL() {
        return "https://sisu.xboxlive.com/authenticate";
    }

    public APIEncoding getRequestEncoding() {
        return APIEncoding.JSON;
    }

    public APIEncoding getResponseEncoding() {
        return APIEncoding.JSON;
    }

    public Class<XboxSISUAuthenticate> getResponseClass() {
        return XboxSISUAuthenticate.class;
    }
}
