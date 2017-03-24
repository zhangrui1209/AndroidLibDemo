package rayco.androidlib.net.http.request.builder;

import java.io.File;

import okhttp3.MediaType;
import rayco.androidlib.net.http.request.PostFileRequest;
import rayco.androidlib.net.http.request.RequestCall;

/**
 * Created by zhy on 15/12/14.
 */
public class PostFileBuilder extends HttpRequestBuilder<PostFileBuilder> {
    private File file;
    private MediaType mediaType;

    public HttpRequestBuilder file(File file) {
        this.file = file;
        return this;
    }

    public HttpRequestBuilder mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public RequestCall build() {
        return new PostFileRequest(url, tag, params, headers, file, mediaType, id).build();
    }
}
