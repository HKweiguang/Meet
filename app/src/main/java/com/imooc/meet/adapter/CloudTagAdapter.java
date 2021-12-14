package com.imooc.meet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.imooc.meet.R;
import com.moxun.tagcloudlib.view.TagsAdapter;

import java.util.ArrayList;

public class CloudTagAdapter extends TagsAdapter {

    private final Context context;
    private final ArrayList<String> list;
    private final LayoutInflater inflater;

    private final ArrayList<View> viewlist = new ArrayList<>();

    public CloudTagAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < this.list.size(); i++) {
            viewlist.add(inflater.inflate(R.layout.layout_star_view_item, null));
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        View mView = null;
        ViewHolder viewHolder;
        if (mView == null) {
            viewHolder = new ViewHolder();
            mView = inflater.inflate(R.layout.layout_star_view_item, null);
            viewHolder.iv_star_icon = mView.findViewById(R.id.iv_star_icon);
            viewHolder.tv_star_name = mView.findViewById(R.id.tv_star_name);
            mView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) mView.getTag();
        }

        viewHolder.tv_star_name.setText(list.get(position));
        viewHolder.iv_star_icon.setImageResource(R.drawable.img_star_icon_3);

        return mView;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getPopularity(int position) {
        return 7;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }

    class ViewHolder {
        private ImageView iv_star_icon;
        private TextView tv_star_name;
    }
}
