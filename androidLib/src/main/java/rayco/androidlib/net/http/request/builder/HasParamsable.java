package rayco.androidlib.net.http.request.builder;

import java.util.Map;

/**
 * Created by zhy on 16/3/1.
 */
public interface HasParamsable {
    HttpRequestBuilder params(Map<String, String> params);

    HttpRequestBuilder addParams(String key, String val);
}
