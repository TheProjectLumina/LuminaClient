package coelho.msftauth.api.minecraft;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("FieldMayBeFinal")
public class MinecraftProfileSkin {
    @SerializedName("alias")
    private String alias;
    @SerializedName("state")
    private String state;
    @SerializedName("url")
    private String url;
    @SerializedName("id")
    private String uuid;
    @SerializedName("variant")
    private String variant;

    public MinecraftProfileSkin(String uuid, String alias, String state, String url, String variant) {
        this.uuid = uuid;
        this.alias = alias;
        this.state = state;
        this.url = url;
        this.variant = variant;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getState() {
        return this.state;
    }

    public String getUrl() {
        return this.url;
    }

    public String getVariant() {
        return this.variant;
    }
}
