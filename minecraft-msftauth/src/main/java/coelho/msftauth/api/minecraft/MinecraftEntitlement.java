package coelho.msftauth.api.minecraft;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("FieldMayBeFinal")
public class MinecraftEntitlement {
    @SerializedName("name")
    private String name;
    @SerializedName("signature")
    private String signature;

    public MinecraftEntitlement(String name, String signature) {
        this.name = name;
        this.signature = signature;
    }

    public String getName() {
        return this.name;
    }

    public String getSignature() {
        return this.signature;
    }
}
