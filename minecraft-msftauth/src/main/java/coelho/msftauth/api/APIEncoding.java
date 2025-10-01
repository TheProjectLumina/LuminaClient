package coelho.msftauth.api;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import coelho.msftauth.util.GsonUtil;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public enum APIEncoding implements APIEncodingEncode, APIEncodingDecode {
    QUERY {
        public void encode(Builder requestBuilder, Object object) {
            Map<String, String> formMap = GsonUtil.GSON.fromJson(GsonUtil.GSON.toJson(object), new TypeToken<Map<String, String>>() {
            }.getType());
            FormBody.Builder formBuilder = new FormBody.Builder();
            for (Entry<String, String> entry : formMap.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
            requestBuilder.post(formBuilder.build());
        }

        public <T> T decode(Response response, Class<T> cls) {
            throw new UnsupportedOperationException();
        }
    },
    JSON {
        public void encode(Builder requestBuilder, Object object) {
            String json = GsonUtil.GSON.toJson(object);
            requestBuilder.addHeader("Accept", "application/json");
            requestBuilder.post(RequestBody.create(json, MediaType.parse("application/json")));
        }

        public <T> T decode(Response response, Class<T> objectClass) throws IOException {
            return GsonUtil.GSON.fromJson(Objects.requireNonNull(response.body()).charStream(), objectClass);
        }
    };
}
