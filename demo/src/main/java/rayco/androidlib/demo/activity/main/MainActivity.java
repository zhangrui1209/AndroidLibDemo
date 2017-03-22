package rayco.androidlib.demo.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import rayco.androidlib.demo.R;
import rayco.androidlib.demo.activity.AppBaseActivity;
import rayco.androidlib.demo.activity.adapterdemo.AdapterDemoActivity;
import rayco.androidlib.demo.activity.paydemo.PayDemoActivity;

public class MainActivity extends AppBaseActivity {

    private Button btnAdapterDemo, btnPayDemo;

    @Override
    protected void initVariables() {}

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        btnAdapterDemo = (Button) findViewById(R.id.btn_adapter_demo);
        btnAdapterDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAdapterDemo();
            }
        });
        btnPayDemo = (Button) findViewById(R.id.btn_pay_demo);
        btnPayDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPayDemo();
            }
        });
    }

    @Override
    protected void loadData() {}

    private void gotoAdapterDemo() {
        Intent intent;
        intent = new Intent(this, AdapterDemoActivity.class);
        startActivity(intent);
    }

    private void gotoPayDemo() {
        Intent intent;
        intent = new Intent(this, PayDemoActivity.class);
        startActivity(intent);
    }
}
