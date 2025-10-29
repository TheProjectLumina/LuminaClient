package coelho.msftauth.api.xbox;

import com.google.gson.JsonElement;

import java.util.Map;

public class XboxDeviceToken extends XboxToken {
    public XboxDeviceToken(String issueInstant, String notAfter, String token, Map<String, JsonElement> displayClaims) {
        super(issueInstant, notAfter, token, displayClaims);
    }
}
