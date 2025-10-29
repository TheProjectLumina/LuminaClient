package coelho.msftauth.api;

import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;

public abstract class APIRequest<R> {
    public abstract String getHttpURL();

    public abstract APIEncoding getRequestEncoding();

    public abstract Class<R> getResponseClass();

    public abstract APIEncoding getResponseEncoding();

    public R request(OkHttpClient client) throws Exception {
        Builder requestBuilder = new Builder().url(getHttpURL());
        if (getRequestEncoding() != null) {
            getRequestEncoding().encode(requestBuilder, this);
        } else {
            requestBuilder.get();
        }
        if (getHttpAuthorization() != null) {
            requestBuilder.addHeader("Authorization", getHttpAuthorization());
        }
        applyHeader(requestBuilder);
        Response response = client.newCall(requestBuilder.build()).execute();
        boolean contains = false;
        for (Class<?> klass : getResponseClass().getInterfaces()) {
            if (klass == APIRequestWithStatus.class) {
                contains = true;
            }
        }
        if (contains || response.code() == 200) {
            R decoded = getResponseEncoding().decode(response, getResponseClass());
            if (decoded instanceof APIResponseExt) {
                ((APIResponseExt) decoded).applyResponse(response);
            }
            if (decoded instanceof APIRequestWithStatus) {
                ((APIRequestWithStatus) decoded).setStatus(response.code());
            }
            return decoded;
        }
        throw new IllegalStateException("status code: " + response.code());
    }

    public void applyHeader(Builder requestBuilder) {
    }

    public String getHttpAuthorization() {
        return null;
    }
}
