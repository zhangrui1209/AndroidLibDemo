package rayco.androidlib.demo.activity.httpdemo;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Response;
import rayco.androidlib.net.http.callback.Callback;

/**
 * Created by zhy on 15/12/14.
 */
public abstract class UserCallback extends Callback<User> {
    @Override
    public User parseNetworkResponse(Response response, int id) throws IOException {
        String string = response.body().string();
        User user = new Gson().fromJson(string, User.class);
        return user;
    }
}
