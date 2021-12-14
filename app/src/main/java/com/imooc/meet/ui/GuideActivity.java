package com.imooc.meet.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.viewpager.widget.ViewPager;

import com.imooc.framework.base.BasePageAdapter;
import com.imooc.framework.base.BaseUIActivity;
import com.imooc.framework.manager.MediaPlayerManager;
import com.imooc.framework.utils.AnimUtils;
import com.imooc.meet.R;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends BaseUIActivity implements View.OnClickListener {

    private ImageView iv_music_switch;
    private TextView tv_guide_skip;
    private ImageView iv_guide_point_1;
    private ImageView iv_guide_point_2;
    private ImageView iv_guide_point_3;
    private ViewPager mViewPager;

    private View view1;
    private View view2;
    private View view3;

    private final List mPageList = new ArrayList<View>();

    private BasePageAdapter mBasePageAdapter;

    private ImageView iv_guide_star;
    private ImageView iv_guide_night;
    private ImageView iv_guide_smile;

    private MediaPlayerManager mediaPlayerManager;

    private ObjectAnimator mAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        initView();
    }

    private void initView() {
        iv_music_switch = findViewById(R.id.iv_music_switch);
        tv_guide_skip = findViewById(R.id.tv_guide_skip);
        iv_guide_point_1 = findViewById(R.id.iv_guide_point_1);
        iv_guide_point_2 = findViewById(R.id.iv_guide_point_2);
        iv_guide_point_3 = findViewById(R.id.iv_guide_point_3);
        mViewPager = findViewById(R.id.mViewPager);

        iv_music_switch.setOnClickListener(this);
        tv_guide_skip.setOnClickListener(this);

        view1 = View.inflate(this, R.layout.layout_pager_guide_1, null);
        view2 = View.inflate(this, R.layout.layout_pager_guide_2, null);
        view3 = View.inflate(this, R.layout.layout_pager_guide_3, null);

        mPageList.add(view1);
        mPageList.add(view2);
        mPageList.add(view3);

        mViewPager.setOffscreenPageLimit(mPageList.size());

        mBasePageAdapter = new BasePageAdapter(mPageList);
        mViewPager.setAdapter(mBasePageAdapter);

        iv_guide_star = view1.findViewById(R.id.iv_guide_star);
        iv_guide_night = view2.findViewById(R.id.iv_guide_night);
        iv_guide_smile = view3.findViewById(R.id.iv_guide_smile);

        AnimationDrawable animStar = (AnimationDrawable) iv_guide_star.getBackground();
        AnimationDrawable animNight = (AnimationDrawable) iv_guide_night.getBackground();
        AnimationDrawable animSmile = (AnimationDrawable) iv_guide_smile.getBackground();

        animStar.start();
        animNight.start();
        animSmile.start();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selecePoint(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        startMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayerManager.stopPlay();
    }

    private void startMusic() {
        mediaPlayerManager = new MediaPlayerManager();
        mediaPlayerManager.setLooping(true);
        AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.guide);
        mediaPlayerManager.startPlay(file);

        mediaPlayerManager.setOnComplteionListener(mp -> mediaPlayerManager.startPlay(file));

        mAnimator = AnimUtils.rotation(iv_music_switch);
        mAnimator.start();
    }

    private void selecePoint(int position) {
        switch (position) {
            case 0:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point_p);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point);
                break;
            case 1:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point_p);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point);
                break;
            case 2:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point_p);
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch ((v.getId())) {
            case R.id.iv_music_switch:
                if (mediaPlayerManager.MEDIA_STATUS == MediaPlayerManager.MEDIA_STATUS_PAUSE) {
                    mAnimator.start();
                    mediaPlayerManager.continuePlay();
                    iv_music_switch.setImageResource(R.drawable.img_guide_music);
                } else if (mediaPlayerManager.MEDIA_STATUS == MediaPlayerManager.MEDIA_STATUS_PLAY) {
                    mAnimator.pause();
                    mediaPlayerManager.pausePlay();
                    iv_music_switch.setImageResource(R.drawable.img_guide_music_off);
                }
                break;
            case R.id.tv_guide_skip:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }
}
