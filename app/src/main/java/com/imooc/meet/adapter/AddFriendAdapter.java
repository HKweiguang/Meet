package com.imooc.meet.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imooc.framework.helper.GlideHelper;
import com.imooc.meet.R;
import com.imooc.meet.model.AddFriendModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_TITLE = 0;
    public static final int TYPE_CONTENT = 1;

    private final Context mContext;
    private final ArrayList<AddFriendModel> mList;
    private final LayoutInflater inflater;

    private OnClickListener onClickListener;

    public AddFriendAdapter(Context mContext, ArrayList<AddFriendModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TITLE) {
            return new TitleViewHolder(inflater.inflate(R.layout.layout_search_title_item, null));
        } else {
            return new ContentViewHolder(inflater.inflate(R.layout.layout_search_user_item, null));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AddFriendModel model = mList.get(position);

        if (model.getType() == TYPE_TITLE) {
            ((TitleViewHolder) holder).tv_title.setText(model.getTitle());
        } else if (model.getType() == TYPE_CONTENT) {
            // 设置头像
            GlideHelper.loadUrl(mContext, model.getPhoto(), ((ContentViewHolder) holder).iv_photo);
            // 设置性别
            ((ContentViewHolder) holder).iv_sex.setImageResource(model.isSex() ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
            // 设置昵称
            ((ContentViewHolder) holder).tv_nickname.setText(model.getNickName());
            ((ContentViewHolder) holder).tv_age.setText(model.getAge() + "岁");
            ((ContentViewHolder) holder).tv_desc.setText(model.getDesc());

            // 通讯录
            if (model.isContact()) {
                ((ContentViewHolder) holder).ll_contact_info.setVisibility(View.VISIBLE);
                ((ContentViewHolder) holder).tv_contact_name.setText(model.getContactName());
                ((ContentViewHolder) holder).tv_contact_phone.setText(model.getContactPhone());
            } else {
                ((ContentViewHolder) holder).ll_contact_info.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getType();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_title;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView iv_photo;
        private final ImageView iv_sex;
        private final TextView tv_nickname;
        private final TextView tv_age;
        private final TextView tv_desc;

        private final LinearLayout ll_contact_info;
        private final TextView tv_contact_name;
        private final TextView tv_contact_phone;

        public ContentViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_photo = itemView.findViewById(R.id.iv_photo);
            iv_sex = itemView.findViewById(R.id.iv_sex);
            tv_nickname = itemView.findViewById(R.id.tv_nickname);
            tv_age = itemView.findViewById(R.id.tv_age);
            tv_desc = itemView.findViewById(R.id.tv_desc);

            ll_contact_info = itemView.findViewById(R.id.ll_contact_info);
            tv_contact_name = itemView.findViewById(R.id.tv_contact_name);
            tv_contact_phone = itemView.findViewById(R.id.tv_contact_phone);
        }
    }

    public interface OnClickListener {
        void onClick(int position);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
