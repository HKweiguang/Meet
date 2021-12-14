package com.imooc.meet.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imooc.framework.adapter.CommonAdapter;
import com.imooc.framework.adapter.CommonViewHolder;
import com.imooc.framework.base.BaseBackActivity;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.meet.R;
import com.imooc.meet.adapter.AddFriendAdapter;
import com.imooc.meet.model.AddFriendModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ContactFriendActivity extends BaseBackActivity {

    private RecyclerView mContactView;
    private final HashMap<String, String> mContactMap = new HashMap<>();

    private CommonAdapter<AddFriendModel> addFriendAdapter;
    private final ArrayList<AddFriendModel> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_friend);

        initView();
    }

    private void initView() {
        addFriendAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<AddFriendModel>() {
            @Override
            public void onBindViewHolder(AddFriendModel model, CommonViewHolder holder, int position, int type) {
                //设置头像
                holder.setImageUrl(ContactFriendActivity.this, R.id.iv_photo, model.getPhoto());
                //设置性别
                holder.setImageResource(R.id.iv_sex,
                        model.isSex() ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                //设置昵称
                holder.setText(R.id.tv_nickname, model.getNickName());
                //年龄
                holder.setText(R.id.tv_age, model.getAge() + getString(R.string.text_search_age));
                //设置描述
                holder.setText(R.id.tv_desc, model.getDesc());

                if (model.isContact()) {
                    holder.getView(R.id.ll_contact_info).setVisibility(View.VISIBLE);
                    holder.setText(R.id.tv_contact_name, model.getContactName());
                    holder.setText(R.id.tv_contact_phone, model.getContactPhone());
                }

                holder.itemView.setOnClickListener(v -> UserInfoActivity.startActivity(ContactFriendActivity.this,
                        model.getUserId()));
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_search_user_item;
            }
        });

        mContactView = findViewById(R.id.mContactView);
        mContactView.setLayoutManager(new LinearLayoutManager(this));
        mContactView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mContactView.setAdapter(addFriendAdapter);

        loadContact();
        loadUser();
    }

    /**
     * 加载用户
     */
    private void loadUser() {
        if (mContactMap.size() > 0) {
            for (Map.Entry<String, String> entry : mContactMap.entrySet()) {
                BmobManager.getInstance().queryPhoneUser(entry.getValue(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if (e == null) {
                            if (CommonUtils.isNotEmpty(list)) {
                                IMUser imUser = list.get(0);

                                addContent(imUser, entry.getKey(), entry.getValue());
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 加载联系人
     */
    private void loadContact() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String name;
        String phone;
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "").replace("-", "");

            mContactMap.put(name, phone);
        }

        cursor.close();
    }

    /**
     * 添加内容
     */
    @SuppressLint("NotifyDataSetChanged")
    private void addContent(IMUser imUser, String name, String phone) {
        AddFriendModel model = new AddFriendModel();
        model.setType(AddFriendAdapter.TYPE_CONTENT);
        model.setUserId(imUser.getObjectId());
        model.setPhoto(imUser.getPhoto());
        model.setSex(imUser.isSex());
        model.setAge(imUser.getAge());
        model.setNickName(imUser.getNickName());
        model.setDesc(imUser.getDesc());

        model.setContact(true);
        model.setContactName(name);
        model.setContactPhone(phone);

        mList.add(model);
        addFriendAdapter.notifyDataSetChanged();
    }
}
