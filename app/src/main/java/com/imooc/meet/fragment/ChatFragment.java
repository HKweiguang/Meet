package com.imooc.meet.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.imooc.framework.base.BaseFragment;
import com.imooc.meet.R;
import com.imooc.meet.fragment.chat.AllFriendFragment;
import com.imooc.meet.fragment.chat.CallRecordFragment;
import com.imooc.meet.fragment.chat.ChatRecordFragment;

import java.util.ArrayList;
import java.util.Objects;

public class ChatFragment extends BaseFragment {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private String[] mTitle;

    private final ArrayList<Fragment> fragmentList = new ArrayList<>();
    private AllFriendFragment allFriendFragment;
    private CallRecordFragment callRecordFragment;
    private ChatRecordFragment chatRecordFragment;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView(inflater.inflate(R.layout.fragment_chat, null));
    }

    private View initView(View view) {
        mTitle = new String[]{
                getString(R.string.text_chat_tab_title_1),
                getString(R.string.text_chat_tab_title_2),
                getString(R.string.text_chat_tab_title_3)
        };

        chatRecordFragment = new ChatRecordFragment();
        callRecordFragment = new CallRecordFragment();
        allFriendFragment = new AllFriendFragment();
        fragmentList.add(chatRecordFragment);
        fragmentList.add(callRecordFragment);
        fragmentList.add(allFriendFragment);

        mTabLayout = view.findViewById(R.id.mTabLayout);
        mViewPager = view.findViewById(R.id.mViewPager);

        for (String title : mTitle) {
            mTabLayout.addTab(mTabLayout.newTab().setText(title));
        }

        mViewPager.setOffscreenPageLimit(mTitle.length);
        mViewPager.setAdapter(new ChatPagerAdapter(getFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                defTabStyle(tab, 20);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setCustomView(null);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        defTabStyle(Objects.requireNonNull(mTabLayout.getTabAt(0)), 20);
        return view;
    }

    private void defTabStyle(TabLayout.Tab tab, int size) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_tab_text, null);
        TextView tv_tab = view.findViewById(R.id.tv_tab);
        tv_tab.setText(tab.getText());
        tv_tab.setTextColor(Color.WHITE);
        tv_tab.setTextSize(size);
        tab.setCustomView(tv_tab);
    }

    class ChatPagerAdapter extends FragmentStatePagerAdapter {

        public ChatPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mTitle.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mTitle[position];
        }
    }
}
