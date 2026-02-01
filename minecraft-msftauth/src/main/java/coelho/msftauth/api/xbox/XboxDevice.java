package coelho.msftauth.api.xbox;

@SuppressWarnings("FieldMayBeFinal")
public class XboxDevice {
    private XboxDeviceKey key;
    private XboxDeviceToken token;

    public XboxDevice(XboxDeviceKey key, XboxDeviceToken token) {
        this.key = key;
        this.token = token;
    }

    public XboxDeviceKey getKey() {
        return this.key;
    }

    public XboxDeviceToken getToken() {
        return this.token;
    }

    public XboxProofKey getProofKey() {
        return this.key.getProofKey();
    }
}
