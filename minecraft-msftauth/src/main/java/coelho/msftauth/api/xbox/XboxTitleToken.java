package coelho.msftauth.api.xbox;

import com.google.gson.JsonElement;

import java.util.Map;

public class XboxTitleToken extends XboxToken {
    public XboxTitleToken(String issueInstant, String notAfter, String token, Map<String, JsonElement> displayClaims) {
        super(issueInstant, notAfter, token, displayClaims);
    }
}
