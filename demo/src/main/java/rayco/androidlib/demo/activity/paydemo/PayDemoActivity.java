package rayco.androidlib.demo.activity.paydemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import rayco.androidlib.demo.R;
import rayco.androidlib.demo.activity.AppBaseActivity;
import rayco.androidlib.pay.PayUtils;
import rayco.androidlib.pay.alipay.AliPay;
import rayco.androidlib.pay.alipay.util.OrderInfoUtil;
import rayco.androidlib.pay.weixin.WXPay;

public class PayDemoActivity extends AppBaseActivity {

    /**************************************支付宝****************************************/
    /**
     * 支付宝支付业务：入参app_id
     */
    public static final String APP_ID = ""; //替换为自己的APP_ID

    /**
     * 支付宝账户登录授权业务：入参pid值
     */
    public static final String PID = ""; //替换为自己的PID

    /**
     * 支付宝账户登录授权业务：入参target_id值
     */
    public static final String TARGET_ID = ""; //替换为自己的TARGET_ID

    /**
     * 商户私钥，pkcs8格式
     */
    public static final String RSA_PRIVATE = "";

    /**************************************
     * 微信
     ****************************************/
    public static final String WX_APP_ID = ""; //替换为自己的APP_ID

    private Button btnWXPay, btnAliPay, btnAliAuth, btnGetIp;

    @Override
    protected void initVariables() {}

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_pay_demo);

        btnAliPay = (Button) findViewById(R.id.btnAliPay);
        btnWXPay = (Button) findViewById(R.id.btnWXPay);
        btnAliAuth = (Button) findViewById(R.id.btnAliAuth);
        btnGetIp = (Button) findViewById(R.id.btnGetIp);

        btnAliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAliPay();
            }
        });
        btnWXPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wxPay();
            }
        });
        btnAliAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAliAuth();
            }
        });
        btnGetIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getIp();
            }
        });
    }

    @Override
    protected void loadData() {}

    /**
     * 支付宝支付
     */
    private void doAliPay() {
        if (TextUtils.isEmpty(APP_ID) || TextUtils.isEmpty(RSA_PRIVATE)) {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            finish();
                        }
                    }).show();
            return;
        }

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
        Map<String, String> paramsMap = OrderInfoUtil.buildOrderParamMap(APP_ID);
        String orderParam = OrderInfoUtil.buildOrderParam(paramsMap);
        String sign = OrderInfoUtil.getSign(paramsMap, RSA_PRIVATE);
        String orderInfo = orderParam + "&" + sign;

        AliPay.getInstance().doPay(this, orderInfo, new AliPay.AliPayCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplication(), "支付成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDealing() {
                Toast.makeText(getApplication(), "支付处理中...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int errorCode) {
                switch (errorCode) {
                    case AliPay.PAY_ERROR_RESULT:
                        Toast.makeText(getApplication(), "支付失败:支付结果解析错误", Toast.LENGTH_SHORT).show();
                        break;
                    case AliPay.PAY_ERROR_NETWORK:
                        Toast.makeText(getApplication(), "支付失败:网络连接错误", Toast.LENGTH_SHORT).show();
                        break;
                    case AliPay.PAY_ERROR_PAY:
                        Toast.makeText(getApplication(), "支付错误:支付码支付失败", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplication(), "支付错误", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplication(), "支付取消", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 支付宝授权
     */
    private void doAliAuth() {
        if (TextUtils.isEmpty(PID) || TextUtils.isEmpty(APP_ID) || TextUtils.isEmpty(RSA_PRIVATE)
                || TextUtils.isEmpty(TARGET_ID)) {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER |APP_ID| RSA_PRIVATE| TARGET_ID")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                        }
                    }).show();
            return;
        }

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * authInfo的获取必须来自服务端；
         */
        Map<String, String> authInfoMap = OrderInfoUtil.buildAuthInfoMap(PID, APP_ID, TARGET_ID);
        String info = OrderInfoUtil.buildOrderParam(authInfoMap);
        String sign = OrderInfoUtil.getSign(authInfoMap, RSA_PRIVATE);
        final String authInfo = info + "&" + sign;

        AliPay.getInstance().doAuth(this, authInfo, new AliPay.AliAuthCallback() {
            @Override
            public void onSuccess(String authCode) {
                Toast.makeText(getApplication(), "授权成功: " + authCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int errorCode, String authCode) {
                Toast.makeText(getApplication(), "授权失败，errorCode:" + errorCode + ", authCode:" + authCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void wxPay() {
        Map<String, String> map = new HashMap<>();
        map.put("appid", WX_APP_ID);
        map.put("partnerid", "xxxxxxxxxx");
        map.put("prepayid", "xxxxxxxxxx");
        map.put("package", "xxxxxxxxxx");
        map.put("noncestr", "xxxxxxxxxx");
        map.put("timestamp", "xxxxxxxxxx");
        map.put("sign", "xxxxxxxxxx");
        JSONObject params = new JSONObject(map);
        doWXPay(params.toString());
    }

    /**
     * 微信支付
     *
     * @param payParam 支付服务生成的支付参数
     */
    private void doWXPay(String payParam) {
        // 微信支付初始化，要在支付前调用
        WXPay.init(getApplicationContext(), WX_APP_ID);
        // 支付
        WXPay.getInstance().doPay(payParam, new WXPay.WXPayCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplication(), "支付成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int errorCode) {
                switch (errorCode) {
                    case WXPay.NO_OR_LOW_WX:
                        Toast.makeText(getApplication(), "未安装微信或微信版本过低", Toast.LENGTH_SHORT).show();
                        break;

                    case WXPay.ERROR_PAY_PARAM:
                        Toast.makeText(getApplication(), "参数错误", Toast.LENGTH_SHORT).show();
                        break;

                    case WXPay.ERROR_PAY:
                        Toast.makeText(getApplication(), "支付失败", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplication(), "支付取消", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getIp() {
        String ip = PayUtils.getIpAddress();
        if (ip != null) {
            Toast.makeText(getApplication(), ip, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplication(), "获取ip失败", Toast.LENGTH_SHORT).show();
        }
    }
}
