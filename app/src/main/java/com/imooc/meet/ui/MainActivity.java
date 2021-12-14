package com.imooc.meet.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.imooc.framework.base.BaseUIActivity;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.gson.TokenBean;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.manager.DialogManager;
import com.imooc.framework.manager.HttpManager;
import com.imooc.framework.utils.LogUtils;
import com.imooc.framework.utils.SpUtils;
import com.imooc.framework.view.DialogView;
import com.imooc.meet.R;
import com.imooc.meet.fragment.ChatFragment;
import com.imooc.meet.fragment.MeFragment;
import com.imooc.meet.fragment.SquareFragment;
import com.imooc.meet.fragment.StarFragment;
import com.imooc.meet.service.CloudService;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseUIActivity implements View.OnClickListener {

    private ImageView iv_star;
    private TextView tv_star;
    private LinearLayout ll_star;
    private StarFragment starFragment;
    private FragmentTransaction starTransaction;

    private ImageView iv_square;
    private TextView tv_square;
    private LinearLayout ll_square;
    private SquareFragment squareFragment;
    private FragmentTransaction squareTransaction;

    private ImageView iv_chat;
    private TextView tv_chat;
    private LinearLayout ll_chat;
    private ChatFragment chatFragment;
    private FragmentTransaction chatTransaction;

    private ImageView iv_me;
    private TextView tv_me;
    private LinearLayout ll_me;
    private MeFragment meFragment;
    private FragmentTransaction meTransaction;

    private Disposable disposable;

    /**
     * 跳转上传头像回调请求参数
     */
    public static final int UPLOAD_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermiss();
        initView();
    }

    /**
     * 请求权限
     */
    private void requestPermiss() {
        request(PERMISSION_REQUEST_CODE, new OnPermissionResult() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFail(ArrayList<String> noPermissions) {
            }
        });
    }

    private void initView() {
        iv_star = findViewById(R.id.iv_star);
        tv_star = findViewById(R.id.tv_star);
        ll_star = findViewById(R.id.ll_star);
        iv_square = findViewById(R.id.iv_square);
        tv_square = findViewById(R.id.tv_square);
        ll_square = findViewById(R.id.ll_square);
        iv_chat = findViewById(R.id.iv_chat);
        tv_chat = findViewById(R.id.tv_chat);
        ll_chat = findViewById(R.id.ll_chat);
        iv_me = findViewById(R.id.iv_me);
        tv_me = findViewById(R.id.tv_me);
        ll_me = findViewById(R.id.ll_me);

        ll_star.setOnClickListener(this);
        ll_square.setOnClickListener(this);
        ll_chat.setOnClickListener(this);
        ll_me.setOnClickListener(this);

        tv_star.setText(getString(R.string.text_main_star));
        tv_square.setText(getString(R.string.text_main_square));
        tv_chat.setText(getString(R.string.text_main_chat));
        tv_me.setText(getString(R.string.text_main_me));

        initFragment();

        checkMainTab(0);

        checkToken();
    }

    /**
     * 检查Token
     */
    private void checkToken() {
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        if (!TextUtils.isEmpty(token)) {
            startCloudService();
        } else {
            String tokenPhoto = BmobManager.getInstance().getUser().getTokenPhoto();
            String tokenNickName = BmobManager.getInstance().getUser().getTokenNickName();
            if (!TextUtils.isEmpty(tokenPhoto) && !TextUtils.isEmpty(tokenNickName)) {
                createToken();
            } else {
                createUploadDialog();
            }
        }
    }

    private void startCloudService() {
        startService(new Intent(this, CloudService.class));
    }

    /**
     * 创建Token
     */
    private void createToken() {
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", BmobManager.getInstance().getUser().getObjectId());
        map.put("name", BmobManager.getInstance().getUser().getTokenNickName());
        map.put("portraitUri", BmobManager.getInstance().getUser().getTokenPhoto());

        disposable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            String json = HttpManager.getInstance().postCloudToken(map);
            emitter.onNext(json);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
                    LogUtils.i("s:" + s);
                    parsingCloudToken(s);
                });
    }

    /**
     * 解析Token
     */
    private void parsingCloudToken(String s) {
        TokenBean tokenBean = new Gson().fromJson(s, TokenBean.class);
        if (tokenBean.getCode() == 200) {
            if (!TextUtils.isEmpty(tokenBean.getToken())) {
                SpUtils.getInstance().putString(Constants.SP_TOKEN, tokenBean.getToken());
                startCloudService();
            }
        }
    }

    /**
     * 创建上传提示框
     */
    private void createUploadDialog() {
        final DialogView uploadView = DialogManager.getInstance().initView(this, R.layout.dialog_first_upload);
        uploadView.setCancelable(false);
        ImageView iv_go_upload = uploadView.findViewById(R.id.iv_go_upload);
        iv_go_upload.setOnClickListener(v -> {
            DialogManager.getInstance().hide(uploadView);
            FirstUploadActivity.startActivity(MainActivity.this, UPLOAD_REQUEST_CODE);
        });
        DialogManager.getInstance().show(uploadView);
    }

    private void initFragment() {
        if (starFragment == null) {
            starFragment = new StarFragment();
            starTransaction = getSupportFragmentManager().beginTransaction();
            starTransaction.add(R.id.mMainLayout, starFragment);
            starTransaction.commit();
        }

        if (squareFragment == null) {
            squareFragment = new SquareFragment();
            squareTransaction = getSupportFragmentManager().beginTransaction();
            squareTransaction.add(R.id.mMainLayout, squareFragment);
            squareTransaction.commit();
        }

        if (chatFragment == null) {
            chatFragment = new ChatFragment();
            chatTransaction = getSupportFragmentManager().beginTransaction();
            chatTransaction.add(R.id.mMainLayout, chatFragment);
            chatTransaction.commit();
        }

        if (meFragment == null) {
            meFragment = new MeFragment();
            meTransaction = getSupportFragmentManager().beginTransaction();
            meTransaction.add(R.id.mMainLayout, meFragment);
            meTransaction.commit();
        }
    }

    /**
     * 显示Fragment
     *
     * @param fragment 页面
     */
    private void showFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 隐藏所有Fragment
     *
     * @param transaction 控制器
     */
    private void hideAllFragment(FragmentTransaction transaction) {
        if (starFragment != null) {
            transaction.hide(starFragment);
        }
        if (squareFragment != null) {
            transaction.hide(squareFragment);
        }
        if (chatFragment != null) {
            transaction.hide(chatFragment);
        }
        if (meFragment != null) {
            transaction.hide(meFragment);
        }
    }

    /**
     * 切换主页选项卡
     *
     * @param index 位置
     */
    private void checkMainTab(int index) {
        switch (index) {
            case 0:
                showFragment(starFragment);
                iv_star.setImageResource(R.drawable.img_star_p);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me);

                tv_star.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);
                break;
            case 1:
                showFragment(squareFragment);
                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square_p);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me);

                tv_square.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_star.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);
                break;
            case 2:
                showFragment(chatFragment);
                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat_p);
                iv_me.setImageResource(R.drawable.img_me);

                tv_chat.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);
                break;
            case 3:
                showFragment(meFragment);
                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me_p);

                tv_me.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                break;
        }
    }

    /**
     * 防止重叠
     * 当应用的内存紧张的时候，系统会回收掉Fragment对象
     * 再一次进入的时候会重新创建Fragment
     * 非原来对象，我们无法控制，导致重叠
     */
    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (starFragment != null && fragment instanceof StarFragment) {
            starFragment = (StarFragment) fragment;
        }
        if (squareFragment != null && fragment instanceof SquareFragment) {
            squareFragment = (SquareFragment) fragment;
        }
        if (chatFragment != null && fragment instanceof ChatFragment) {
            chatFragment = (ChatFragment) fragment;
        }
        if (meFragment != null && fragment instanceof MeFragment) {
            meFragment = (MeFragment) fragment;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_star:
                checkMainTab(0);
                break;
            case R.id.ll_square:
                checkMainTab(1);
                break;
            case R.id.ll_chat:
                checkMainTab(2);
                break;
            case R.id.ll_me:
                checkMainTab(3);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == UPLOAD_REQUEST_CODE) {
                checkToken();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
