package coelho.msftauth.api;

import java.io.IOException;

import okhttp3.Response;

public interface APIEncodingDecode {
    <T> T decode(Response response, Class<T> cls) throws IOException;
}
