package rayco.androidlib.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter {

    private Context context;
    protected List<T> list;
    private int layoutId;

    public CommonAdapter(Context context, List<T> list, int layoutId) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder viewHolder = CommonViewHolder.get(context, convertView, parent, layoutId, position);

        convert(viewHolder, (T) getItem(position));

        return viewHolder.getConvertView();
    }

    /**
     * 子类实现抽象方法，设置控件内容
     *
     * @param viewHolder ViewHolder
     * @param t          数据对象
     */
    public abstract void convert(CommonViewHolder viewHolder, T t);
}
