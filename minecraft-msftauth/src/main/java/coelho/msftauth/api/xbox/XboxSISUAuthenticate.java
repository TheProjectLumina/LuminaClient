package coelho.msftauth.api.xbox;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIResponseExt;
import okhttp3.Response;

@SuppressWarnings("FieldMayBeFinal")
public class XboxSISUAuthenticate implements APIResponseExt {
    @SerializedName("MsaOauthRedirect")
    private String msaOauthRedirect;
    @SerializedName("MsaRequestParameters")
    private JsonObject msaRequestParameters;
    private transient String sessionId;

    public XboxSISUAuthenticate(String msaOauthRedirect, JsonObject msaRequestParameters) {
        this.msaOauthRedirect = msaOauthRedirect;
        this.msaRequestParameters = msaRequestParameters;
    }

    public void applyResponse(Response response) {
        this.sessionId = response.header("X-SessionId");
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getMsaOauthRedirect() {
        return this.msaOauthRedirect;
    }

    public JsonObject getMsaRequestParameters() {
        return this.msaRequestParameters;
    }
}
