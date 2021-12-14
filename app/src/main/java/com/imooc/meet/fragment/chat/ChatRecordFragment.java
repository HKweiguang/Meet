package com.imooc.meet.fragment.chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.imooc.framework.adapter.CommonAdapter;
import com.imooc.framework.adapter.CommonViewHolder;
import com.imooc.framework.base.BaseFragment;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.bmob.IMUser;
import com.imooc.framework.cloud.CloudManager;
import com.imooc.framework.gson.TextBean;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.meet.R;
import com.imooc.meet.model.ChatRecordModel;
import com.imooc.meet.ui.ChatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.TextMessage;

public class ChatRecordFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private View item_empty_view;
    private RecyclerView mChatRecordView;
    private SwipeRefreshLayout mChatRecordRefreshLayout;

    private CommonAdapter<ChatRecordModel> chatRecordModelCommonAdapter;
    private final ArrayList<ChatRecordModel> mList = new ArrayList<>();

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView(inflater.inflate(R.layout.fragment_chat_record, null));
    }

    private View initView(View view) {
        item_empty_view = view.findViewById(R.id.item_empty_view);
        mChatRecordView = view.findViewById(R.id.mChatRecordView);
        mChatRecordRefreshLayout = view.findViewById(R.id.mChatRecordRefreshLayout);

        mChatRecordRefreshLayout.setOnRefreshListener(this);

        chatRecordModelCommonAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<ChatRecordModel>() {
            @Override
            public void onBindViewHolder(ChatRecordModel model, CommonViewHolder holder, int position, int type) {
                holder.setImageUrl(getActivity(), R.id.iv_photo, model.getUrl());
                holder.setText(R.id.tv_nickname, model.getNickName());
                holder.setText(R.id.tv_content, model.getEndMsg());
                holder.setText(R.id.tv_time, model.getTime());

                if (model.getUnReadSize() == 0) {
                    holder.getView(R.id.tv_un_read).setVisibility(View.GONE);
                } else {
                    holder.getView(R.id.tv_un_read).setVisibility(View.VISIBLE);
                    holder.setText(R.id.tv_un_read, model.getUnReadSize() + "");
                }

                holder.itemView.setOnClickListener(v -> ChatActivity.startActivity(getActivity(), model.getUserId(), model.getNickName(), model.getUrl()));
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_chat_record_item;
            }
        });
        mChatRecordView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatRecordView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        mChatRecordView.setAdapter(chatRecordModelCommonAdapter);

        queryChatRecord();

        return view;
    }

    /**
     * 查询聊天记录
     */
    private void queryChatRecord() {
        mChatRecordRefreshLayout.setRefreshing(true);
        CloudManager.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<Conversation> conversations) {
                mChatRecordRefreshLayout.setRefreshing(false);
                if (CommonUtils.isNotEmpty(conversations)) {
                    if (mList.size() > 0) {
                        mList.clear();
                    }
                    for (Conversation c : conversations) {
                        String id = c.getTargetId();
                        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
                            @SuppressLint({"SimpleDateFormat", "NotifyDataSetChanged"})
                            @Override
                            public void done(List<IMUser> list, BmobException e) {
                                if (e == null) {
                                    if (CommonUtils.isNotEmpty(list)) {
                                        IMUser imUser = list.get(0);
                                        ChatRecordModel chatRecordModel = new ChatRecordModel();
                                        chatRecordModel.setUserId(imUser.getObjectId());
                                        chatRecordModel.setUrl(imUser.getPhoto());
                                        chatRecordModel.setNickName(imUser.getNickName());
                                        chatRecordModel.setTime(new SimpleDateFormat("HH:mm:ss").format(c.getReceivedTime()));
                                        chatRecordModel.setUnReadSize(c.getUnreadMessageCount());

                                        switch (c.getObjectName()) {
                                            case CloudManager.MSG_TEXT_NAME:
                                                TextMessage textMessage = (TextMessage) c.getLatestMessage();
                                                String content = textMessage.getContent();
                                                TextBean textBean = new Gson().fromJson(content, TextBean.class);
                                                if (textBean.getType().equals(CloudManager.TYPE_TEXT)) {
                                                    chatRecordModel.setEndMsg(textBean.getMsg());
                                                    mList.add(chatRecordModel);
                                                }
                                                break;
                                            case CloudManager.MSG_IMAGE_NAME:
                                                chatRecordModel.setEndMsg(getString(R.string.text_chat_record_img));
                                                mList.add(chatRecordModel);
                                                break;
                                            case CloudManager.MSG_LOCATION_NAME:
                                                chatRecordModel.setEndMsg(getString(R.string.text_chat_record_location));
                                                mList.add(chatRecordModel);
                                                break;
                                        }
                                        chatRecordModelCommonAdapter.notifyDataSetChanged();

                                        if (mList.size() > 0) {
                                            item_empty_view.setVisibility(View.GONE);
                                            mChatRecordView.setVisibility(View.VISIBLE);
                                        } else {
                                            item_empty_view.setVisibility(View.VISIBLE);
                                            mChatRecordView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        });
                    }
                } else {
                    item_empty_view.setVisibility(View.VISIBLE);
                    mChatRecordView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                mChatRecordRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        if (mChatRecordRefreshLayout.isRefreshing()) {
            queryChatRecord();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        queryChatRecord();
    }
}
