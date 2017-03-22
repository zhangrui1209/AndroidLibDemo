package rayco.androidlib.pay.alipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;

import java.util.Map;

public class AliPay {
    public static final int PAY_ERROR_RESULT = 1;  //支付结果解析错误
    public static final int PAY_ERROR_PAY = 2;     //支付失败
    public static final int PAY_ERROR_NETWORK = 3; //网络连接错误

    public static final int AUTH_ERROR_RESULT = 4; //授权失败

    private static AliPay instance;

    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;

    private AliPayCallback mPayCallback;

    private AliAuthCallback mAuthCallback;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG:
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);

                    if (mPayCallback == null) {
                        return;
                    }

                    // 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) { //支付成功
                        mPayCallback.onSuccess();
                    } else if (TextUtils.equals(resultStatus, "8000")) { //支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        mPayCallback.onDealing();
                    } else if (TextUtils.equals(resultStatus, "6001")) { //支付取消
                        mPayCallback.onCancel();
                    } else if (TextUtils.equals(resultStatus, "6002")) { //网络连接出错
                        mPayCallback.onError(PAY_ERROR_NETWORK);
                    } else if (TextUtils.equals(resultStatus, "4000")) { //支付错误
                        mPayCallback.onError(PAY_ERROR_PAY);
                    }
                    break;
                case SDK_AUTH_FLAG:
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String status = authResult.getResultStatus();

                    if (mAuthCallback == null) {
                        return;
                    }

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(status, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        mAuthCallback.onSuccess(authResult.getAuthCode());
                    } else {// 其他状态值则为授权失败
                        mAuthCallback.onError(AUTH_ERROR_RESULT, authResult.getAuthCode());
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 支付回调接口
     */
    public interface AliPayCallback {
        /**
         * 支付成功
         */
        void onSuccess();

        /**
         * 正在处理中 小概率事件 此时以验证服务端异步通知结果为准
         */
        void onDealing();

        /**
         * 支付失败
         * @param errorCode 错误码
         */
        void onError(int errorCode);

        /**
         * 支付取消
         */
        void onCancel();
    }

    /**
     * 授权回调接口
     */
    public interface AliAuthCallback {
        /**
         * 授权成功
         * @param authCode 授权码
         */
        void onSuccess(String authCode);

        /**
         * 授权失败
         * @param errorCode 错误码
         * @param authCode  授权码
         */
        void onError(int errorCode, String authCode);
    }

    private AliPay() {}

    // 获取实例
    public static AliPay getInstance() {
        if (instance == null) {
            instance = new AliPay();
        }
        return instance;
    }

    //支付
    public void doPay(final Context context, final String payParams, AliPayCallback payCallback) {
        mPayCallback = payCallback;

        new Thread(new Runnable() {
            @Override
            public void run() {
                PayTask payTask = new PayTask((Activity) context);
                Map<String, String> pay_result = payTask.payV2(payParams, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = pay_result;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    //授权
    public void doAuth(final Context context, final String authParams, AliAuthCallback authCallback) {
        mAuthCallback = authCallback;

        new Thread(new Runnable() {
            @Override
            public void run() {
                AuthTask authTask = new AuthTask((Activity) context);
                Map<String, String> result = authTask.authV2(authParams, true);
                Message msg = new Message();
                msg.what = SDK_AUTH_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        }).start();
    }
}
