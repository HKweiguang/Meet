package com.imooc.meet.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imooc.framework.base.BaseFragment;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.helper.GlideHelper;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.meet.R;
import com.imooc.meet.ui.NewFriendActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class MeFragment extends BaseFragment implements View.OnClickListener {

    private CircleImageView iv_me_photo;
    private TextView tv_nickname;
    private LinearLayout ll_me_info;
    private LinearLayout ll_new_friend;
    private LinearLayout ll_private_set;
    private LinearLayout ll_share;
    private LinearLayout ll_setting;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        iv_me_photo = view.findViewById(R.id.iv_me_photo);
        tv_nickname = view.findViewById(R.id.tv_nickname);
        ll_me_info = view.findViewById(R.id.ll_me_info);
        ll_new_friend = view.findViewById(R.id.ll_new_friend);
        ll_private_set = view.findViewById(R.id.ll_private_set);
        ll_share = view.findViewById(R.id.ll_share);
        ll_setting = view.findViewById(R.id.ll_setting);

        ll_me_info.setOnClickListener(this);
        ll_new_friend.setOnClickListener(this);
        ll_private_set.setOnClickListener(this);
        ll_share.setOnClickListener(this);
        ll_setting.setOnClickListener(this);

        loadMeInfo();
    }

    /**
     * 加载我的个人信息
     */
    private void loadMeInfo() {
        IMUser imUser = BmobManager.getInstance().getUser();
        GlideHelper.loadUrl(getActivity(), imUser.getPhoto(), iv_me_photo);

        tv_nickname.setText(imUser.getNickName());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 个人信息
            case R.id.ll_me_info:

                break;
            // 新朋友
            case R.id.ll_new_friend:
                startActivity(new Intent(getActivity(), NewFriendActivity.class));
                break;
            // 隐私设置
            case R.id.ll_private_set:

                break;
            // 分享
            case R.id.ll_share:

                break;
            //设置
            case R.id.ll_setting:

                break;
        }
    }
}
