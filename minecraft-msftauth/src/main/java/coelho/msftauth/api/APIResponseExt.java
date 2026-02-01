package coelho.msftauth.api;

import okhttp3.Response;

public interface APIResponseExt {
    default void applyResponse(Response response) {
    }
}
