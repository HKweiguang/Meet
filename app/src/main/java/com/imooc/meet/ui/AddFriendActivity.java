package com.imooc.meet.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imooc.framework.adapter.CommonAdapter;
import com.imooc.framework.adapter.CommonViewHolder;
import com.imooc.framework.base.BaseBackActivity;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.framework.utils.LogUtils;
import com.imooc.meet.R;
import com.imooc.meet.adapter.AddFriendAdapter;
import com.imooc.meet.model.AddFriendModel;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class AddFriendActivity extends BaseBackActivity implements View.OnClickListener {

    private LinearLayout mLlToContact;
    private EditText mEtPhone;
    private ImageView mIvSearch;
    private RecyclerView mMSearchResultView;

    private View include_empty_view;

    private CommonAdapter<AddFriendModel> mAddFriendAdapter;
    private final ArrayList<AddFriendModel> mList = new ArrayList<>();

    public static final int TYPE_TITLE = 0;
    public static final int TYPE_CONTENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        initView();
    }

    private void initView() {
        include_empty_view = findViewById(R.id.include_empty_view);

        mLlToContact = findViewById(R.id.ll_to_contact);
        mEtPhone = findViewById(R.id.et_phone);
        mIvSearch = findViewById(R.id.iv_search);
        mMSearchResultView = findViewById(R.id.mSearchResultView);

        mLlToContact.setOnClickListener(this);
        mIvSearch.setOnClickListener(this);

        mAddFriendAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnMoreBindDataListener<AddFriendModel>() {
            @Override
            public int getItemType(int position) {
                return mList.get(position).getType();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(AddFriendModel model, CommonViewHolder holder, int position, int type) {
                if (model.getType() == TYPE_TITLE) {
                    holder.setText(R.id.tv_title, model.getTitle());
                } else if (model.getType() == TYPE_CONTENT) {
                    // 设置头像
                    holder.setImageUrl(AddFriendActivity.this, R.id.iv_photo, model.getPhoto())
                            // 设置性别
                            .setImageResource(R.id.iv_sex, model.isSex() ? R.drawable.img_boy_icon : R.drawable.img_girl_icon)
                            // 设置昵称
                            .setText(R.id.tv_nickname, model.getNickName())
                            .setText(R.id.tv_age, model.getAge() + "岁")
                            .setText(R.id.tv_desc, model.getDesc());

                    // 通讯录
                    if (model.isContact()) {
                        holder.setVisibility(R.id.ll_contact_info, View.VISIBLE)
                                .setText(R.id.tv_contact_name, model.getContactName())
                                .setText(R.id.tv_contact_phone, model.getContactPhone());
                    } else {
                        holder.setVisibility(R.id.ll_contact_info, View.GONE);
                    }
                }

                holder.itemView.setOnClickListener(v -> UserInfoActivity.startActivity(AddFriendActivity.this, model.getUserId()));
            }

            @Override
            public int getLayoutId(int type) {
                if (type == TYPE_TITLE) {
                    return R.layout.layout_search_title_item;
                } else if (type == TYPE_CONTENT) {
                    return R.layout.layout_search_user_item;
                }
                return 0;
            }
        });
        mMSearchResultView.setLayoutManager(new LinearLayoutManager(this));
        mMSearchResultView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mMSearchResultView.setAdapter(mAddFriendAdapter);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_to_contact:
                if (checkPermission(Manifest.permission.READ_CONTACTS)) {
                    startActivity(new Intent(this, ContactFriendActivity.class));
                } else {
                    requestPermission(new String[]{Manifest.permission.READ_CONTACTS});
                }
                break;
            case R.id.iv_search:
                queryPhoneUser();
                break;
        }
    }

    /**
     * 通过电话号码查询
     */
    private void queryPhoneUser() {
        String phone = mEtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getString(R.string.text_login_phone_null), Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = BmobManager.getInstance().getUser().getMobilePhoneNumber();
        if (phone.equals(phoneNumber)) {
            Toast.makeText(this, "不能查自己", Toast.LENGTH_SHORT).show();
            return;
        }

        BmobManager.getInstance().queryPhoneUser(phone, new FindListener<IMUser>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e != null) return;

                if (CommonUtils.isNotEmpty(list)) {
                    IMUser imUser = list.get(0);
                    LogUtils.i(imUser.toString());
                    include_empty_view.setVisibility(View.GONE);
                    mMSearchResultView.setVisibility(View.VISIBLE);

                    mList.clear();

                    addTitle("查询结果");
                    addContent(imUser);
                    mAddFriendAdapter.notifyDataSetChanged();

                    pushUser();
                } else {
                    include_empty_view.setVisibility(View.VISIBLE);
                    mMSearchResultView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 推荐好友
     */
    private void pushUser() {
        BmobManager.getInstance().queryAllUser(new FindListener<IMUser>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isNotEmpty(list)) {
                        addTitle("推荐好友");

                        String phoneNumber = BmobManager.getInstance().getUser().getMobilePhoneNumber();
                        int num = Math.min(list.size(), 100);
                        for (int i = 0; i < num; i++) {
                            if (!list.get(i).getMobilePhoneNumber().equals(phoneNumber)) {
                                addContent(list.get(i));
                            }
                        }
                        mAddFriendAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * 添加头部
     */
    private void addTitle(String title) {
        AddFriendModel model = new AddFriendModel();
        model.setType(AddFriendAdapter.TYPE_TITLE);
        model.setTitle(title);

        mList.add(model);
    }

    /**
     * 添加内容
     */
    private void addContent(IMUser imUser) {
        AddFriendModel model = new AddFriendModel();
        model.setType(AddFriendAdapter.TYPE_CONTENT);
        model.setUserId(imUser.getObjectId());
        model.setPhoto(imUser.getPhoto());
        model.setSex(imUser.isSex());
        model.setAge(imUser.getAge());
        model.setNickName(imUser.getNickName());
        model.setDesc(imUser.getDesc());

        mList.add(model);
    }
}
