package rayco.androidlib.demo.activity.httpdemo;

import com.google.gson.Gson;

import rayco.androidlib.net.http.callback.IGenericsSerializer;

/**
 * Created by JimGong on 2016/6/23.
 */
public class JsonGenericsSerializator implements IGenericsSerializer {
    Gson mGson = new Gson();
    @Override
    public <T> T transform(String response, Class<T> classOfT) {
        return mGson.fromJson(response, classOfT);
    }
}
