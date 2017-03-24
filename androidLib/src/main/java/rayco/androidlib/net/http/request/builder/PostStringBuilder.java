package rayco.androidlib.net.http.request.builder;

import okhttp3.MediaType;
import rayco.androidlib.net.http.request.PostStringRequest;
import rayco.androidlib.net.http.request.RequestCall;

/**
 * Created by zhy on 15/12/14.
 */
public class PostStringBuilder extends HttpRequestBuilder<PostStringBuilder> {
    private String content;
    private MediaType mediaType;


    public PostStringBuilder content(String content) {
        this.content = content;
        return this;
    }

    public PostStringBuilder mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public RequestCall build() {
        return new PostStringRequest(url, tag, params, headers, content, mediaType, id).build();
    }
}
