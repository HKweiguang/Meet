package com.imooc.meet.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imooc.framework.base.BaseFragment;
import com.imooc.meet.R;
import com.imooc.meet.adapter.CloudTagAdapter;
import com.imooc.meet.ui.AddFriendActivity;
import com.moxun.tagcloudlib.view.TagCloudView;

import java.util.ArrayList;
import java.util.List;

public class StarFragment extends BaseFragment implements View.OnClickListener {

    private ImageView iv_camera;
    private ImageView iv_add;
    private TagCloudView mCloudView;
    private LinearLayout ll_random;
    private LinearLayout ll_soul;
    private LinearLayout ll_fate;
    private LinearLayout ll_love;

    private CloudTagAdapter cloudTagAdapter;
    private ArrayList<String> starList = new ArrayList<>();

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_star, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        iv_camera = view.findViewById(R.id.iv_camera);
        iv_add = view.findViewById(R.id.iv_add);
        mCloudView = view.findViewById(R.id.mCloudView);
        ll_random = view.findViewById(R.id.ll_random);
        ll_soul = view.findViewById(R.id.ll_soul);
        ll_fate = view.findViewById(R.id.ll_fate);
        ll_love = view.findViewById(R.id.ll_love);

        iv_camera.setOnClickListener(this);
        iv_add.setOnClickListener(this);
        ll_random.setOnClickListener(this);
        ll_soul.setOnClickListener(this);
        ll_fate.setOnClickListener(this);
        ll_love.setOnClickListener(this);

        for (int i = 0; i < 50; i++) {
            starList.add("Star:" + i);
        }

        cloudTagAdapter = new CloudTagAdapter(requireActivity(), starList);
        mCloudView.setAdapter(cloudTagAdapter);
        
        mCloudView.setOnTagClickListener((parent, view1, position) -> Toast.makeText(getContext(), "position: " + position, Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_camera:
                // 扫描
                break;
            case R.id.iv_add:
                // 添加好友
                startActivity(new Intent(getContext(), AddFriendActivity.class));
                break;
            case R.id.ll_random:
                // 随机匹配
                break;
            case R.id.ll_soul:
                // 灵魂匹配
                break;
            case R.id.ll_fate:
                // 缘分匹配
                break;
            case R.id.ll_love:
                // 恋爱匹配
                break;
        }
    }
}
