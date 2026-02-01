package coelho.msftauth.api.xbox;

import com.google.gson.annotations.SerializedName;

import coelho.msftauth.api.APIRequestWithStatus;
import coelho.msftauth.api.APIResponseExt;
import coelho.msftauth.api.oauth20.OAuth20Util;
import okhttp3.HttpUrl;
import okhttp3.Response;

@SuppressWarnings("FieldMayBeFinal")
public class XboxSISUAuthorize implements APIResponseExt, APIRequestWithStatus {
    @SerializedName("AuthorizationToken")
    private XboxToken authorizationToken;
    @SerializedName("DeviceToken")
    private String deviceToken;
    @SerializedName("Sandbox")
    private String sandbox;
    private transient String sessionId;
    private int status;
    @SerializedName("TitleToken")
    private XboxToken titleToken;
    @SerializedName("UserToken")
    private XboxToken userToken;
    @SerializedName("WebPage")
    private String webPage;

    public XboxSISUAuthorize(XboxToken authorizationToken, String deviceToken, String sandbox, XboxToken titleToken, XboxToken userToken, String webPage) {
        this.authorizationToken = authorizationToken;
        this.deviceToken = deviceToken;
        this.sandbox = sandbox;
        this.titleToken = titleToken;
        this.userToken = userToken;
        this.webPage = webPage;
    }

    public void applyResponse(Response response) {
        this.sessionId = response.header("X-SessionId");
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public XboxToken getAuthorizationToken() {
        return this.authorizationToken;
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    public String getSandbox() {
        return this.sandbox;
    }

    public XboxToken getTitleToken() {
        return this.titleToken;
    }

    public XboxToken getUserToken() {
        return this.userToken;
    }

    public String getWebPage() {
        return this.webPage;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public String getRedirectURL(String sessionId) {
        return HttpUrl.parse(this.webPage).newBuilder().addQueryParameter("redirect", OAuth20Util.REDIRECT_URI).addQueryParameter("sid", sessionId).build().toString();
    }
}
