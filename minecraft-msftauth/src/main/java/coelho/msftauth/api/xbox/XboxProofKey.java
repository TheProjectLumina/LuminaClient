package coelho.msftauth.api.xbox;

import com.google.gson.annotations.SerializedName;

import java.util.Base64;

public class XboxProofKey {
    @SerializedName("alg")
    public String alg;
    @SerializedName("crv")
    public String crv;
    @SerializedName("kty")
    public String kty;
    @SerializedName("use")
    public String use;
    @SerializedName("x")
    public String x;
    @SerializedName("y")
    public String y;

    public XboxProofKey(XboxDeviceKey key) {
        this.crv = key.getCrv();
        this.alg = key.getAlg();
        this.use = key.getUse();
        this.kty = key.getKty();
        this.x = Base64.getUrlEncoder().withoutPadding().encodeToString(key.getPublicXBytes());
        this.y = Base64.getUrlEncoder().withoutPadding().encodeToString(key.getPublicYBytes());
    }

    public String getCrv() {
        return this.crv;
    }

    public String getAlg() {
        return this.alg;
    }

    public String getUse() {
        return this.use;
    }

    public String getKty() {
        return this.kty;
    }

    public String getX() {
        return this.x;
    }

    public String getY() {
        return this.y;
    }
}
