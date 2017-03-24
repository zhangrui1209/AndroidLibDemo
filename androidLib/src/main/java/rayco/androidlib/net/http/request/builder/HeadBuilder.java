package rayco.androidlib.net.http.request.builder;

import rayco.androidlib.net.http.Http;
import rayco.androidlib.net.http.request.OtherRequest;
import rayco.androidlib.net.http.request.RequestCall;

/**
 * Created by zhy on 16/3/2.
 */
public class HeadBuilder extends GetBuilder {
    @Override
    public RequestCall build() {
        return new OtherRequest(null, null, Http.METHOD.HEAD, url, tag, params, headers, id).build();
    }
}
