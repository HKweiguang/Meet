package com.imooc.framework.adapter;

import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CommonAdapter<T> extends RecyclerView.Adapter<CommonViewHolder> {

    private ArrayList<T> mList;

    private final OnBindDataListener<T> onBindDataListener;

    private OnMoreBindDataListener<T> onMoreBindDataListener;

    public CommonAdapter(ArrayList<T> mList, OnBindDataListener<T> onBindDataListener) {
        this.mList = mList;
        this.onBindDataListener = onBindDataListener;
    }

    public CommonAdapter(ArrayList<T> mList, OnMoreBindDataListener<T> onMoreBindDataListener) {
        this.mList = mList;
        this.onBindDataListener = onMoreBindDataListener;
        this.onMoreBindDataListener = onMoreBindDataListener;
    }

    /**
     * 绑定数据
     */
    public interface OnBindDataListener<T> {
        void onBindViewHolder(T model, CommonViewHolder holder, int position, int type);

        @LayoutRes
        int getLayoutId(int type);
    }

    /**
     * 绑定多类型的数据
     */
    public interface OnMoreBindDataListener<T> extends OnBindDataListener<T> {
        @LayoutRes
        int getItemType(int position);
    }

    @Override
    public int getItemViewType(int position) {
        if (onMoreBindDataListener != null) {
            return onMoreBindDataListener.getItemType(position);
        }
        return 0;
    }

    @NonNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = onBindDataListener.getLayoutId(viewType);
        CommonViewHolder viewHolder = CommonViewHolder.getViewHolder(parent, layoutId);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
        onBindDataListener.onBindViewHolder(mList.get(position), holder, position, holder.getItemViewType());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }
}
