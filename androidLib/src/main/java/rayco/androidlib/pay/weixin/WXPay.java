package rayco.androidlib.pay.weixin;

import android.content.Context;
import android.text.TextUtils;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

public class WXPay {
    public static final int NO_OR_LOW_WX = 1;    //未安装微信或微信版本过低
    public static final int ERROR_PAY_PARAM = 2; //支付参数错误
    public static final int ERROR_PAY = 3;       //支付失败

    private static WXPay mWXPay;

    private IWXAPI mWXApi;
    private String mPayParam;
    private WXPayCallback mCallback;

    /**
     * 支付回调接口
     */
    public interface WXPayCallback {
        /**
         * 支付成功
         */
        void onSuccess();

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

    private WXPay(Context context, String wxAppId) {
        mWXApi = WXAPIFactory.createWXAPI(context, null);
        mWXApi.registerApp(wxAppId);
    }

    public static void init(Context context, String wxAppId) {
        if (mWXPay == null) {
            mWXPay = new WXPay(context, wxAppId);
        }
    }

    public static WXPay getInstance() {
        return mWXPay;
    }

    IWXAPI getWXApi() {
        return mWXApi;
    }

    /**
     * 发起微信支付
     */
    public void doPay(String payParam, WXPayCallback callback) {
        mPayParam = payParam;
        mCallback = callback;

        if (!check()) {
            if (mCallback != null) {
                mCallback.onError(NO_OR_LOW_WX);
            }
            return;
        }

        JSONObject param = null;
        try {
            param = new JSONObject(mPayParam);
        } catch (JSONException e) {
            e.printStackTrace();
            if (mCallback != null) {
                mCallback.onError(ERROR_PAY_PARAM);
            }
            return;
        }
        if (TextUtils.isEmpty(param.optString("appid")) || TextUtils.isEmpty(param.optString("partnerid"))
                || TextUtils.isEmpty(param.optString("prepayid")) || TextUtils.isEmpty(param.optString("package")) ||
                TextUtils.isEmpty(param.optString("noncestr")) || TextUtils.isEmpty(param.optString("timestamp")) ||
                TextUtils.isEmpty(param.optString("sign"))) {
            if (mCallback != null) {
                mCallback.onError(ERROR_PAY_PARAM);
            }
            return;
        }

        PayReq req = new PayReq();
        req.appId = param.optString("appid");
        req.partnerId = param.optString("partnerid");
        req.prepayId = param.optString("prepayid");
        req.packageValue = param.optString("package");
        req.nonceStr = param.optString("noncestr");
        req.timeStamp = param.optString("timestamp");
        req.sign = param.optString("sign");

        mWXApi.sendReq(req);
    }

    //支付回调响应
    void onResp(int errorCode) {
        if (mCallback == null) {
            return;
        }

        if (errorCode == 0) { //成功
            mCallback.onSuccess();
        } else if (errorCode == -1) { //错误
            mCallback.onError(ERROR_PAY);
        } else if (errorCode == -2) { //取消
            mCallback.onCancel();
        }

        mCallback = null;
    }

    //检测是否支持微信支付
    private boolean check() {
        return mWXApi.isWXAppInstalled() && mWXApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
    }
}
