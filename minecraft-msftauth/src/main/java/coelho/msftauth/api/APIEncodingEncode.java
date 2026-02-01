package coelho.msftauth.api;

import okhttp3.Request.Builder;

public interface APIEncodingEncode {
    void encode(Builder builder, Object obj);
}
