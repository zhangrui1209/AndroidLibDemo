package rayco.androidlib.demo.activity.adapterdemo;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import rayco.androidlib.adapter.CommonAdapter;
import rayco.androidlib.adapter.CommonViewHolder;
import rayco.androidlib.demo.R;
import rayco.androidlib.demo.activity.AppBaseActivity;
import rayco.androidlib.demo.adapter.TestAdapter;
import rayco.androidlib.demo.entity.Bean;

public class AdapterDemoActivity extends AppBaseActivity {

    private ListView lvTest;
    private List<Bean> beanList;
    private TestAdapter testAdapter;

    @Override
    protected void initVariables() {
        beanList = new ArrayList<>();

        Bean bean1 = new Bean("第一条数据", "第一条数据的描述", "2017-1-1", "11111");
        beanList.add(bean1);

        Bean bean2 = new Bean("第二条数据", "第二条数据的描述", "2017-1-2", "22222");
        beanList.add(bean2);

        Bean bean3 = new Bean("第三条数据", "第三条数据的描述", "2017-1-3", "33333");
        beanList.add(bean3);

        Bean bean4 = new Bean("第四条数据", "第四条数据的描述", "2017-1-4", "44444");
        beanList.add(bean4);

        Bean bean5 = new Bean("第五条数据", "第五条数据的描述", "2017-1-5", "55555");
        beanList.add(bean5);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_adapter_demo);
        lvTest = (ListView) findViewById(R.id.lv_test);

        // 让TestAdapter继承CommonAdapter(多用于存在交互事件的情况)
        // testAdapter = new TestAdapter(this, beanList);
        // lvTest.setAdapter(testAdapter);

        // 或者直接使用匿名类(多用于没有交互事件,只有数据显示)
        lvTest.setAdapter(new CommonAdapter<Bean>(AdapterDemoActivity.this, beanList, R.layout.item_list) {
            @Override
            public void convert(CommonViewHolder viewHolder, Bean bean) {
                viewHolder.setText(R.id.tv_title, bean.getTitle())
                        .setText(R.id.tv_desc, bean.getDesc())
                        .setText(R.id.tv_time, bean.getTime())
                        .setText(R.id.tv_phone, bean.getPhone());
            }
        });
    }

    @Override
    protected void loadData() {}
}
