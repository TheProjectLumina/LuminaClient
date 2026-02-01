package coelho.msftauth.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static final Gson GSON_MIN = new GsonBuilder().disableHtmlEscaping().create();
}
