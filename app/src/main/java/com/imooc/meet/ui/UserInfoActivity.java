package com.imooc.meet.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imooc.framework.adapter.CommonAdapter;
import com.imooc.framework.adapter.CommonViewHolder;
import com.imooc.framework.base.BaseUIActivity;
import com.imooc.framework.bmob.Friend;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.cloud.CloudManager;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.helper.GlideHelper;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.manager.DialogManager;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.framework.utils.LogUtils;
import com.imooc.framework.view.DialogView;
import com.imooc.meet.R;
import com.imooc.meet.model.UserInfoModel;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;
import io.rong.calllib.RongCallCommon;

public class UserInfoActivity extends BaseUIActivity implements View.OnClickListener {

    public static void startActivity(Context context, String userId) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID, userId);
        context.startActivity(intent);
    }

    private RelativeLayout ll_back;
    private CircleImageView iv_user_photo;
    private TextView tv_nickname;
    private TextView tv_desc;

    private RecyclerView mUserInfoView;
    private CommonAdapter<UserInfoModel> mUserInfoAdapter;
    private ArrayList<UserInfoModel> mList = new ArrayList<>();

    private Button btn_add_friend;
    private Button btn_chat;
    private Button btn_audio_chat;
    private Button btn_video_chat;
    private LinearLayout ll_is_friend;

    //个人信息颜色
    private int[] mColor = {0x881E90FF, 0x8800FF7F, 0x88FFD700, 0x88FF6347, 0x88F08080, 0x8840E0D0};

    private String userId = "";

    private IMUser imUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initView();
    }

    private void initView() {
        userId = getIntent().getStringExtra(Constants.INTENT_USER_ID);
        LogUtils.i("userId: " + userId);

        ll_back = this.findViewById(R.id.ll_back);
        iv_user_photo = this.findViewById(R.id.iv_user_photo);
        tv_nickname = this.findViewById(R.id.tv_nickname);
        tv_desc = this.findViewById(R.id.tv_desc);
        mUserInfoView = this.findViewById(R.id.mUserInfoView);
        btn_add_friend = this.findViewById(R.id.btn_add_friend);
        btn_chat = this.findViewById(R.id.btn_chat);
        btn_audio_chat = this.findViewById(R.id.btn_audio_chat);
        btn_video_chat = this.findViewById(R.id.btn_video_chat);
        ll_is_friend = this.findViewById(R.id.ll_is_friend);

        ll_back.setOnClickListener(this);
        btn_add_friend.setOnClickListener(this);
        btn_chat.setOnClickListener(this);
        btn_audio_chat.setOnClickListener(this);
        btn_video_chat.setOnClickListener(this);

        mUserInfoAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<UserInfoModel>() {
            @Override
            public void onBindViewHolder(UserInfoModel model, CommonViewHolder holder, int position, int type) {
                holder.getView(R.id.ll_bg).setBackgroundColor(model.getBgColor());
                holder.setText(R.id.tv_type, model.getTitle());
                holder.setText(R.id.tv_content, model.getContent());
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_user_info_item;
            }
        });
        mUserInfoView.setLayoutManager(new GridLayoutManager(this, 3));
        mUserInfoView.setAdapter(mUserInfoAdapter);

        queryUserInfo();

        initAddFriendDialog();
    }

    private DialogView addFriendDialog;
    private EditText et_msg;
    private TextView tv_cancel;
    private TextView tv_add_friend;

    /**
     * 添加好友提示框
     */
    public void initAddFriendDialog() {
        addFriendDialog = DialogManager.getInstance().initView(this, R.layout.dialog_send_friend);
        et_msg = addFriendDialog.findViewById(R.id.et_msg);
        tv_cancel = addFriendDialog.findViewById(R.id.tv_cancel);
        tv_add_friend = addFriendDialog.findViewById(R.id.tv_add_friend);

        tv_cancel.setOnClickListener(this);
        tv_add_friend.setOnClickListener(this);
    }

    /**
     * 查询用户信息
     */
    private void queryUserInfo() {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        BmobManager.getInstance().queryObjectIdUser(userId, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isNotEmpty(list)) {
                        imUser = list.get(0);
                        updateUserInfo(imUser);
                    }
                }
            }
        });

        BmobManager.getInstance().queryMyFriend(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isNotEmpty(list)) {
                        for (int i = 0; i < list.size(); i++) {
                            Friend friend = list.get(i);
                            if (friend.getFriendUser().getObjectId().equals(userId)) {
                                btn_add_friend.setVisibility(View.GONE);
                                ll_is_friend.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 更新用户信息
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateUserInfo(IMUser imUser) {
        // 设置基本属性
        GlideHelper.loadUrl(UserInfoActivity.this, imUser.getPhoto(), iv_user_photo);
        tv_nickname.setText(imUser.getNickName());
        tv_desc.setText(imUser.getDesc());

        addUserInfoModel(mColor[0], getString(R.string.text_me_info_sex), imUser.isSex() ? getString(R.string.text_me_info_boy) : getString(R.string.text_me_info_girl));
        addUserInfoModel(mColor[1], getString(R.string.text_me_info_age), imUser.getAge() + getString(R.string.text_search_age));
        addUserInfoModel(mColor[2], getString(R.string.text_me_info_birthday), imUser.getBirthday());
        addUserInfoModel(mColor[3], getString(R.string.text_me_info_constellation), imUser.getConstellation());
        addUserInfoModel(mColor[4], getString(R.string.text_me_info_hobby), imUser.getHobby());
        addUserInfoModel(mColor[5], getString(R.string.text_me_info_status), imUser.getStatus());

        mUserInfoAdapter.notifyDataSetChanged();
    }

    /**
     * 添加数据
     */
    public void addUserInfoModel(int color, String title, String content) {
        UserInfoModel model = new UserInfoModel();
        model.setBgColor(color);
        model.setTitle(title);
        model.setContent(content);

        mList.add(model);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                break;
            case R.id.btn_add_friend:
                DialogManager.getInstance().show(addFriendDialog);
                break;
            case R.id.btn_chat:
                ChatActivity.startActivity(this, imUser.getObjectId(), imUser.getNickName(), imUser.getPhoto());
                break;
            case R.id.btn_audio_chat:
                CloudManager.getInstance().startAudioCall(userId);
                break;
            case R.id.btn_video_chat:
                CloudManager.getInstance().startVideoCall(userId);
                break;
            case R.id.tv_cancel:
                DialogManager.getInstance().hide(addFriendDialog);
                break;
            case R.id.tv_add_friend:
                String msg = et_msg.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.text_user_info_add_friend);
                }
                CloudManager.getInstance().sendTextMessage(msg, CloudManager.TYPE_ADD_FRIEND, userId);
                DialogManager.getInstance().hide(addFriendDialog);
                Toast.makeText(this, getString(R.string.text_user_resuest_succeed), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
