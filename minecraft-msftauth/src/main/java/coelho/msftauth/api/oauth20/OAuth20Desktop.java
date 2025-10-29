package coelho.msftauth.api.oauth20;

import java.net.MalformedURLException;
import java.util.Objects;

import okhttp3.HttpUrl;

public class OAuth20Desktop {
    private String code;
    private String lc;
    private String status;

    public OAuth20Desktop(String urlString) throws MalformedURLException {
        HttpUrl url = HttpUrl.parse(urlString);
        for (String queryName : Objects.requireNonNull(url).queryParameterNames()) {
            Object obj = -1;
            switch (queryName.hashCode()) {
                case -892481550:
                    if (queryName.equals("status")) {
                        obj = null;
                        break;
                    }
                    break;
                case 3447:
                    if (queryName.equals("lc")) {
                        obj = 2;
                        break;
                    }
                    break;
                case 3059181:
                    if (queryName.equals("code")) {
                        obj = 1;
                        break;
                    }
                    break;
            }
            if (obj == null) {
                this.status = url.queryParameter(queryName);
            } else if (obj.equals(1)) {
                this.code = url.queryParameter(queryName);
            } else if (obj.equals(2)) {
                this.lc = url.queryParameter(queryName);
            }

        }
        if (this.status == null) {
            throw new IllegalArgumentException(urlString);
        }
    }

    public String getStatus() {
        return this.status;
    }

    public String getCode() {
        return this.code;
    }

    public String getLc() {
        return this.lc;
    }
}
