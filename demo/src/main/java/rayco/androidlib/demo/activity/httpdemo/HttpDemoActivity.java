package rayco.androidlib.demo.activity.httpdemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.Request;
import rayco.androidlib.demo.R;
import rayco.androidlib.net.http.Http;
import rayco.androidlib.net.http.callback.BitmapCallback;
import rayco.androidlib.net.http.callback.FileCallBack;
import rayco.androidlib.net.http.callback.GenericsCallback;
import rayco.androidlib.net.http.callback.StringCallback;
import rayco.androidlib.net.http.cookie.CookieJarImpl;

public class HttpDemoActivity extends AppCompatActivity {

    private String mBaseUrl = "http://192.168.2.129:8082/webserver/";

    private static final String TAG = "HttpDemoActivity";

    private TextView mTv;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    public class MyStringCallback extends StringCallback {
        @Override
        public void onBefore(Request request, int id) {
            setTitle("loading...");
        }

        @Override
        public void onAfter(int id) {
            setTitle("AndroidLibDemo");
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
            mTv.setText("onError:" + e.getMessage());
        }

        @Override
        public void onResponse(String response, int id) {
            Log.e(TAG, "onResponse：complete");
            mTv.setText("onResponse:" + response);

            switch (id) {
                case 100:
                    Toast.makeText(HttpDemoActivity.this, "http", Toast.LENGTH_SHORT).show();
                    break;
                case 101:
                    Toast.makeText(HttpDemoActivity.this, "https", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void inProgress(float progress, long total, int id) {
            Log.e(TAG, "inProgress:" + progress);
            mProgressBar.setProgress((int) (100 * progress));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_demo);

        mTv = (TextView) findViewById(R.id.id_textview);
        mImageView = (ImageView) findViewById(R.id.id_imageview);
        mProgressBar = (ProgressBar) findViewById(R.id.id_progress);
        mProgressBar.setMax(100);
    }

    public void getHtml(View view) {
        String url = "http://apis.juhe.cn/mobile/get?phone=15012345678&key=6bfc6250233605ffbe86dc7ba073d127";
        Http.get()
            .url(url)
            .id(100)
            .build()
            .execute(new MyStringCallback());
    }

    public void postString(View view) {
        String url = mBaseUrl + "api_web/postString";
        Http.postString()
            .url(url)
            .mediaType(MediaType.parse("application/json; charset=utf-8"))
            .content(new Gson().toJson(new User("rayco", "123456")))
            .build()
            .execute(new MyStringCallback());
    }

    public void postFile(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "test.png");
        if (!file.exists()) {
            Toast.makeText(HttpDemoActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = mBaseUrl + "api_web/postFile";
        Http.postFile()
            .url(url)
            .file(file)
            .build()
            .execute(new MyStringCallback());
    }

    public void getUser(View view) {
        String url = mBaseUrl + "api_web/getUser";
        Http.post()
            .url(url)
            .addParams("username", "rayco")
            .addParams("password", "123456")
            .build()
            .execute(
                    new UserCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            mTv.setText("onError:" + e.getMessage());
                        }

                        @Override
                        public void onResponse(User response, int id) {
                            mTv.setText("onResponse:" + response.username);
                        }
                    }
//                    new GenericsCallback<User>(new JsonGenericsSerializator()) {
//                        @Override
//                        public void onError(Call call, Exception e, int id) {
//                            mTv.setText("onError:" + e.getMessage());
//                        }
//
//                        @Override
//                        public void onResponse(User response, int id) {
//                            mTv.setText("onResponse:" + response.username);
//                        }
//                    }
            );
    }

    public void getUsers(View view) {
        String url = mBaseUrl + "api_web/getUsers";
        Http.post()
            .url(url)
            .build()
            .execute(new ListUserCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    mTv.setText("onError:" + e.getMessage());
                }

                @Override
                public void onResponse(List<User> response, int id) {
                    mTv.setText("onResponse:" + response);
                }
            });
    }

    public void getHttpsHtml(View view) {
        String url = "https://kyfw.12306.cn/otn/";
        Http.get()
            .url(url)
            .id(101)
            .build()
            .execute(new MyStringCallback());
    }

    public void getImage(View view) {
        mTv.setText("");
        String url = "http://images.csdn.net/20150817/1.jpg";
        Http.get()
            .url(url)
            .tag(this)
            .build()
            .connTimeOut(20000)
            .readTimeOut(20000)
            .writeTimeOut(20000)
            .execute(new BitmapCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    mTv.setText("onError:" + e.getMessage());
                }

                @Override
                public void onResponse(Bitmap bitmap, int id) {
                    Log.e("TAG", "onResponse：complete");
                    mImageView.setImageBitmap(bitmap);
                }
            });
    }

    public void uploadFile(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "test.png");
        if (!file.exists()) {
            Toast.makeText(HttpDemoActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("username", "rayco");
        params.put("password", "123456");

        Map<String, String> headers = new HashMap<>();
        headers.put("APP-Key", "APP-Secret222");
        headers.put("APP-Secret", "APP-Secret111");

        String url = mBaseUrl + "api_web/uploadFile";

        Http.post()
            .addFile("mFile", "test.png", file)
            .url(url)
            .params(params)
            .headers(headers)
            .build()
            .execute(new MyStringCallback());
    }

    public void multiFileUpload(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "test.png");
        File file2 = new File(Environment.getExternalStorageDirectory(), "test1.txt");
        if (!file.exists()) {
            Toast.makeText(HttpDemoActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("username", "rayco");
        params.put("password", "123456");

        String url = mBaseUrl + "api_web/uploadFile";
        Http.post()
            .addFile("mFile", "test.png", file)
            .addFile("mFile", "test1.txt", file2)
            .url(url)
            .params(params)
            .build()
            .execute(new MyStringCallback());
    }

    public void downloadFile(View view) {
        String url = "http://images.csdn.net/20150817/1.jpg";
        Http.get()
            .url(url)
            .build()
            .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), "111.jpg") {
                @Override
                public void onBefore(Request request, int id) {
                }

                @Override
                public void inProgress(float progress, long total, int id) {
                    mProgressBar.setProgress((int) (100 * progress));
                    Log.e(TAG, "inProgress :" + (int) (100 * progress));
                }

                @Override
                public void onError(Call call, Exception e, int id) {
                    Log.e(TAG, "onError :" + e.getMessage());
                }

                @Override
                public void onResponse(File file, int id) {
                    Log.e(TAG, "onResponse :" + file.getAbsolutePath());
                }
            });
    }

    public void otherRequestDemo(View view) {
        //also can use delete ,head , patch
        Http.put()
            .url("http://11111.com")
            .requestBody("may be something")
            .build()
            .execute(new MyStringCallback());

        try {
            Http.head()
                .url("http://11111.com")
                .addParams("name", "zhy")
                .build()
                .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearSession(View view) {
        CookieJar cookieJar = Http.getInstance().getOkHttpClient().cookieJar();
        if (cookieJar instanceof CookieJarImpl) {
            ((CookieJarImpl) cookieJar).getCookieStore().removeAll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Http.getInstance().cancelTag(this);
    }
}
