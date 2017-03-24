package rayco.androidlib.net.http.callback;

/**
 * Created by JimGong on 2016/6/23.
 */
public interface IGenericsSerializer {
    <T> T transform(String response, Class<T> classOfT);
}
