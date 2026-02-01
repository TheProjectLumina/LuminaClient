package coelho.msftauth.api.xbox;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
public class XboxToken {
    @SerializedName("DisplayClaims")
    private Map<String, JsonElement> displayClaims;
    @SerializedName("IssueInstant")
    private String issueInstant;
    @SerializedName("NotAfter")
    private String notAfter;
    @SerializedName("Token")
    private String token;

    public XboxToken(String issueInstant, String notAfter, String token, Map<String, JsonElement> displayClaims) {
        this.issueInstant = issueInstant;
        this.notAfter = notAfter;
        this.token = token;
        this.displayClaims = displayClaims;
    }

    public String getIssueInstant() {
        return this.issueInstant;
    }

    public String getNotAfter() {
        return this.notAfter;
    }

    public String getToken() {
        return this.token;
    }

    public Map<String, JsonElement> getDisplayClaims() {
        return this.displayClaims;
    }

    public String toIdentityToken() {
        return "XBL3.0 x=" + ((JsonElement) this.displayClaims.get("xui")).getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString() + ";" + this.token;
    }
}
