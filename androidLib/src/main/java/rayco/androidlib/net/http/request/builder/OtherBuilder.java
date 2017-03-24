package rayco.androidlib.net.http.request.builder;

import okhttp3.RequestBody;
import rayco.androidlib.net.http.request.OtherRequest;
import rayco.androidlib.net.http.request.RequestCall;

/**
 * DELETE、PUT、PATCH等其他方法
 */
public class OtherBuilder extends HttpRequestBuilder<OtherBuilder> {
    private RequestBody requestBody;
    private String method;
    private String content;

    public OtherBuilder(String method) {
        this.method = method;
    }

    @Override
    public RequestCall build() {
        return new OtherRequest(requestBody, content, method, url, tag, params, headers, id).build();
    }

    public OtherBuilder requestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public OtherBuilder requestBody(String content) {
        this.content = content;
        return this;
    }
}
