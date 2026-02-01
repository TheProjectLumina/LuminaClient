package coelho.msftauth.api.minecraft;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class MinecraftEntitlements {
    @SerializedName("items")
    private List<MinecraftEntitlements> all;
    @SerializedName("keyId")
    private String keyId;
    @SerializedName("signature")
    private String signature;

    public MinecraftEntitlements(List<MinecraftEntitlements> all, String signature, String keyId) {
        this.all = all;
        this.signature = signature;
        this.keyId = keyId;
    }

    public List<MinecraftEntitlements> getAll() {
        return Collections.unmodifiableList(this.all);
    }

    public String getSignature() {
        return this.signature;
    }

    public String getKeyId() {
        return this.keyId;
    }
}
