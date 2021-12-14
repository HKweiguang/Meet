package com.imooc.framework.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imooc.framework.helper.GlideHelper;

import java.io.File;

public class CommonViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViews;
    private View mContentView;

    public CommonViewHolder(@NonNull View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
        mContentView = itemView;
    }

    public static CommonViewHolder getViewHolder(ViewGroup parent, @LayoutRes int layoutId) {
        return new CommonViewHolder(View.inflate(parent.getContext(), layoutId, null));
    }

    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mContentView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public CommonViewHolder setText(@IdRes int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    public CommonViewHolder setImageUrl(Context mContext, @IdRes int viewId, String url) {
        ImageView iv = getView(viewId);
        GlideHelper.loadUrl(mContext, url, iv);
        return this;
    }

    public CommonViewHolder setImageFile(Context mContext, @IdRes int viewId, File file) {
        ImageView iv = getView(viewId);
        GlideHelper.loadFile(mContext, file, iv);
        return this;
    }

    public CommonViewHolder setImageResource(@IdRes int viewId, @DrawableRes int resId) {
        ImageView iv = getView(viewId);
        iv.setImageResource(resId);
        return this;
    }

    public CommonViewHolder setVisibility(@IdRes int viewId, int visibility) {
        getView(viewId).setVisibility(visibility);
        return this;
    }
}
