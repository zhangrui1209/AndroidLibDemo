package rayco.androidlib.demo.activity.httpdemo;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;
import rayco.androidlib.net.http.callback.Callback;

/**
 * Created by zhy on 15/12/14.
 */
public abstract class ListUserCallback extends Callback<List<User>> {

    @Override
    public List<User> parseNetworkResponse(Response response, int id) throws IOException {
        String string = response.body().string();
        List<User> user = new Gson().fromJson(string, List.class);
        return user;
    }
}
