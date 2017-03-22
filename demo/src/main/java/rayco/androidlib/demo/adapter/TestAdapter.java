package rayco.androidlib.demo.adapter;

import android.content.Context;

import java.util.List;

import rayco.androidlib.adapter.CommonAdapter;
import rayco.androidlib.adapter.CommonViewHolder;
import rayco.androidlib.demo.R;
import rayco.androidlib.demo.entity.Bean;

public class TestAdapter extends CommonAdapter<Bean> {

    public TestAdapter(Context context, List<Bean> list) {
        super(context, list, R.layout.item_list);
    }

    /**
     * 实现抽象方法
     *
     * @param viewHolder ViewHolder
     * @param bean       Bean对象
     */
    @Override
    public void convert(CommonViewHolder viewHolder, Bean bean) {
        viewHolder.setText(R.id.tv_title, bean.getTitle())
                .setText(R.id.tv_desc, bean.getDesc())
                .setText(R.id.tv_time, bean.getTime())
                .setText(R.id.tv_phone, bean.getPhone());
    }

    // TODO, 添加交互事件的处理
}
