package com.imooc.meet.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.imooc.framework.adapter.CommonAdapter;
import com.imooc.framework.adapter.CommonViewHolder;
import com.imooc.framework.base.BaseBackActivity;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.cloud.CloudManager;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.event.EventManager;
import com.imooc.framework.event.MessageEvent;
import com.imooc.framework.gson.TextBean;
import com.imooc.framework.gson.VoiceBean;
import com.imooc.framework.helper.FileHelper;
import com.imooc.framework.manager.MapManager;
import com.imooc.framework.manager.VoiceManager;
import com.imooc.framework.utils.CommonUtils;
import com.imooc.framework.utils.LogUtils;
import com.imooc.meet.R;
import com.imooc.meet.model.ChatModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

public class ChatActivity extends BaseBackActivity implements View.OnClickListener {

    // 左边文本
    public static final int TYPE_LEFT_TEXT = 0;
    public static final int TYPE_LEFT_IMAGE = 1;
    public static final int TYPE_LEFT_LOCATION = 2;

    // 右边文本
    public static final int TYPE_RIGHT_TEXT = 3;
    public static final int TYPE_RIGHT_IMAGE = 4;
    public static final int TYPE_RIGHT_LOCATION = 5;

    private static final int LOCATION_REQUEST_CODE = 1888;

    public static void startActivity(Context context, String userId, String userName, String userPhone) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID, userId);
        intent.putExtra(Constants.INTENT_USER_NAME, userName);
        intent.putExtra(Constants.INTENT_USER_PHOTO, userPhone);
        context.startActivity(intent);
    }

    //聊天列表
    private RecyclerView mChatView;
    //输入框
    private EditText et_input_msg;
    //发送按钮
    private Button btn_send_msg;
    //语音输入
    private LinearLayout ll_voice;
    //相机
    private LinearLayout ll_camera;
    //图片
    private LinearLayout ll_pic;
    //位置
    private LinearLayout ll_location;

    private String otherUserId;
    private String otherUserName;
    private String otherUserPhoto;

    private String mineUserPhoto;

    private CommonAdapter<ChatModel> chatModelAdapter;
    private final ArrayList<ChatModel> mList = new ArrayList<>();

    // 图片文件
    private File uploadFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();
    }

    private void initView() {
        mChatView = findViewById(R.id.mChatView);
        et_input_msg = findViewById(R.id.et_input_msg);
        btn_send_msg = findViewById(R.id.btn_send_msg);
        ll_voice = findViewById(R.id.ll_voice);
        ll_camera = findViewById(R.id.ll_camera);
        ll_pic = findViewById(R.id.ll_pic);
        ll_location = findViewById(R.id.ll_location);

        btn_send_msg.setOnClickListener(this);
        ll_camera.setOnClickListener(this);
        ll_pic.setOnClickListener(this);
        ll_location.setOnClickListener(this);

        chatModelAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnMoreBindDataListener<ChatModel>() {
            @Override
            public int getItemType(int position) {
                return mList.get(position).getType();
            }

            @Override
            public void onBindViewHolder(ChatModel model, CommonViewHolder holder, int position, int type) {
                switch (type) {
                    case TYPE_LEFT_TEXT:
                        holder.setText(R.id.tv_left_text, model.getText());
                        holder.setImageUrl(ChatActivity.this, R.id.iv_left_photo, otherUserPhoto);
                        break;
                    case TYPE_LEFT_IMAGE:
                        holder.setImageUrl(ChatActivity.this, R.id.iv_left_photo, otherUserPhoto);
                        holder.setImageUrl(ChatActivity.this, R.id.iv_left_img, model.getImgUrl());

                        holder.getView(R.id.iv_left_img).setOnClickListener(v -> ImagePreviewActivity.startActivity(ChatActivity.this, true, model.getImgUrl()));
                        break;
                    case TYPE_LEFT_LOCATION:
                        holder.setImageUrl(ChatActivity.this, R.id.iv_left_photo, otherUserPhoto);
                        holder.setImageUrl(ChatActivity.this, R.id.iv_left_location_img, model.getMapUrl());
                        holder.setText(R.id.tv_left_address, model.getAddress());
                        holder.itemView.setOnClickListener(v -> LocationActivity.startActivity(ChatActivity.this, false, model.getLa(), model.getLo(), model.getAddress(), LOCATION_REQUEST_CODE));
                        break;
                    case TYPE_RIGHT_TEXT:
                        holder.setText(R.id.tv_right_text, model.getText());
                        holder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, mineUserPhoto);
                        break;
                    case TYPE_RIGHT_IMAGE:
                        holder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, mineUserPhoto);
                        if (!TextUtils.isEmpty(model.getImgUrl())) {
                            holder.setImageUrl(ChatActivity.this, R.id.iv_right_img, model.getImgUrl());

                            holder.getView(R.id.iv_right_img).setOnClickListener(v -> ImagePreviewActivity.startActivity(ChatActivity.this, true, model.getImgUrl()));
                        } else {
                            if (model.getLocalFile() != null) {
                                holder.setImageFile(ChatActivity.this, R.id.iv_right_img, model.getLocalFile());

                                holder.getView(R.id.iv_right_img).setOnClickListener(v -> ImagePreviewActivity.startActivity(ChatActivity.this, false, model.getLocalFile().getPath()));
                            }
                        }
                        break;
                    case TYPE_RIGHT_LOCATION:
                        holder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, mineUserPhoto);
                        holder.setImageUrl(ChatActivity.this, R.id.iv_right_location_img, model.getMapUrl());
                        holder.setText(R.id.tv_right_address, model.getAddress());
                        holder.itemView.setOnClickListener(v -> LocationActivity.startActivity(ChatActivity.this, false, model.getLa(), model.getLo(), model.getAddress(), LOCATION_REQUEST_CODE));
                        break;
                }
            }

            @Override
            public int getLayoutId(int type) {
                switch (type) {
                    case TYPE_LEFT_TEXT:
                        return R.layout.layout_chat_left_text;
                    case TYPE_LEFT_IMAGE:
                        return R.layout.layout_chat_left_img;
                    case TYPE_LEFT_LOCATION:
                        return R.layout.layout_chat_left_location;
                    case TYPE_RIGHT_TEXT:
                        return R.layout.layout_chat_right_text;
                    case TYPE_RIGHT_IMAGE:
                        return R.layout.layout_chat_right_img;
                    case TYPE_RIGHT_LOCATION:
                        return R.layout.layout_chat_right_location;
                    default:
                        return R.layout.layout_chat_right_text;
                }
            }
        });
        mChatView.setLayoutManager(new LinearLayoutManager(this));
        mChatView.setAdapter(chatModelAdapter);

        loadMeInfo();

        queryMessage();
    }

    private void queryMessage() {
        CloudManager.getInstance().getHistoryMessages(otherUserId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (CommonUtils.isNotEmpty(messages)) {
                    parsingListMessage(messages);
                } else {
                    queryRemoteMessage();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("ErrorCode: " + errorCode);
            }
        });
    }

    /**
     * 解析历史记录
     *
     * @param messages 消息列表
     */
    private void parsingListMessage(List<Message> messages) {
        // 倒序
        Collections.reverse(messages);
        // 遍历
        for (Message m : messages) {
            switch (m.getObjectName()) {
                case CloudManager.MSG_TEXT_NAME:
                    TextMessage textMessage = (TextMessage) m.getContent();
                    String msg = textMessage.getContent();
                    try {
                        TextBean textBean = new Gson().fromJson(msg, TextBean.class);
                        if (textBean.getType().equals(CloudManager.TYPE_TEXT)) {
                            if (m.getSenderUserId().equals(otherUserId)) {
                                addText(0, textBean.getMsg());
                            } else {
                                addText(1, textBean.getMsg());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case CloudManager.MSG_IMAGE_NAME:
                    ImageMessage imageMessage = (ImageMessage) m.getContent();
                    String url = imageMessage.getRemoteUri().toString();
                    if (!TextUtils.isEmpty(url)) {
                        if (m.getSenderUserId().equals(otherUserId)) {
                            addImage(0, url);
                        } else {
                            addImage(1, url);
                        }
                    }
                    break;
                case CloudManager.MSG_LOCATION_NAME:
                    LocationMessage locationMessage = (LocationMessage) m.getContent();
                    if (m.getSenderUserId().equals(otherUserId)) {
                        addLocation(0, locationMessage.getLat(), locationMessage.getLng(), locationMessage.getPoi());
                    } else {
                        addLocation(1, locationMessage.getLat(), locationMessage.getLng(), locationMessage.getPoi());
                    }
                    break;
            }
        }
    }

    private void queryRemoteMessage() {
        CloudManager.getInstance().getRemoteHistoryMessages(otherUserId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (CommonUtils.isNotEmpty(messages)) {
                    try {
                        parsingListMessage(messages);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("ErrorCode: " + errorCode);
            }
        });
    }

    /**
     * 加载自我信息
     */
    private void loadMeInfo() {
        Intent intent = getIntent();
        otherUserId = intent.getStringExtra(Constants.INTENT_USER_ID);
        otherUserName = intent.getStringExtra(Constants.INTENT_USER_NAME);
        otherUserPhoto = intent.getStringExtra(Constants.INTENT_USER_PHOTO);

        mineUserPhoto = BmobManager.getInstance().getUser().getPhoto();

        if (!TextUtils.isEmpty(otherUserId)) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(otherUserName);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void baseAddItem(ChatModel model) {
        mList.add(model);
        chatModelAdapter.notifyDataSetChanged();

        mChatView.scrollToPosition(mList.size() - 1);
    }

    /**
     * 添加文字
     *
     * @param index 0:左边    1:右边
     */
    private void addText(int index, String text) {
        ChatModel model = new ChatModel();
        if (index == 0) {
            model.setType(TYPE_LEFT_TEXT);
        } else {
            model.setType(TYPE_RIGHT_TEXT);
        }
        model.setText(text);
        baseAddItem(model);
    }

    /**
     * 添加图片
     *
     * @param index 0:左边    1:右边
     */
    private void addImage(int index, String url) {
        ChatModel model = new ChatModel();
        if (index == 0) {
            model.setType(TYPE_LEFT_IMAGE);
        } else {
            model.setType(TYPE_RIGHT_IMAGE);
        }
        model.setImgUrl(url);
        baseAddItem(model);
    }

    /**
     * 添加图片
     *
     * @param index 0:左边    1:右边
     */
    private void addImage(int index, File file) {
        ChatModel model = new ChatModel();
        if (index == 0) {
            model.setType(TYPE_LEFT_IMAGE);
        } else {
            model.setType(TYPE_RIGHT_IMAGE);
        }
        model.setLocalFile(file);
        baseAddItem(model);
    }

    /**
     * 添加地址
     *
     * @param index   0:左边    1:右边
     * @param la      经度
     * @param lo      维度
     * @param address 地址
     */
    private void addLocation(int index, double la, double lo, String address) {
        ChatModel model = new ChatModel();
        if (index == 0) {
            model.setType(TYPE_LEFT_LOCATION);
        } else {
            model.setType(TYPE_RIGHT_LOCATION);
        }
        model.setLa(la);
        model.setLo(lo);
        model.setAddress(address);
        model.setMapUrl(MapManager.getInstance().getMapUrl(la, lo));
        baseAddItem(model);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_msg:
                String inputText = et_input_msg.getText().toString().trim();
                if (TextUtils.isEmpty(inputText)) {
                    return;
                }

                CloudManager.getInstance().sendTextMessage(inputText, CloudManager.TYPE_TEXT, otherUserId);
                addText(1, inputText);
                et_input_msg.setText("");
                break;
            case R.id.ll_voice:
                VoiceManager.getInstance(this).startSpeak(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        String result = recognizerResult.getResultString();
                        if (!TextUtils.isEmpty(result)) {
                            LogUtils.i("result:" + result);
                            VoiceBean voiceBean = new Gson().fromJson(result, VoiceBean.class);
                            if (voiceBean.isLs()) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < voiceBean.getWs().size(); i++) {
                                    VoiceBean.WsBean wsBean = voiceBean.getWs().get(i);
                                    String sResult = wsBean.getCw().get(0).getW();
                                    sb.append(sResult);
                                }
                                LogUtils.i("result:" + sb.toString());
                                et_input_msg.setText(sb.toString());
                            }
                        }
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        LogUtils.e("SpeechError:" + speechError.toString());
                    }
                });
                break;
            case R.id.ll_camera:
                FileHelper.getInstance().toCamera(this);
                break;
            case R.id.ll_pic:
                FileHelper.getInstance().toAlbum(this);
                break;
            case R.id.ll_location:
                LocationActivity.startActivity(this, true, 0, 0, "", LOCATION_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case FileHelper.CAMERA_REQUEST_CODE:
                    uploadFile = FileHelper.getInstance().getTempFile();
                    break;
                case FileHelper.ALBUM_REQUEST_CODE:
                    Uri uri = data.getData();
                    if (uri != null) {
                        String path = FileHelper.getInstance().getRealPathFromURI(this, uri);
                        uploadFile = new File(path);
                    }
                    break;
                case LOCATION_REQUEST_CODE:
                    double la = data.getDoubleExtra("la", 0);
                    double lo = data.getDoubleExtra("lo", 0);
                    String address = data.getStringExtra("address");

                    CloudManager.getInstance().sendLocationMessage(otherUserId, la, lo, address);

                    addLocation(1, la, lo, address);
                    break;
            }

            if (uploadFile != null) {
                CloudManager.getInstance().sendImageMessage(uploadFile, otherUserId);
                addImage(1, uploadFile);
                uploadFile = null;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.getUserId().equals(otherUserId)) {
            switch (event.getType()) {
                case EventManager.FLAG_SEND_TEXT:
                    LogUtils.i("event: " + event);
                    addText(0, event.getText());
                    break;
                case EventManager.FLAG_SEND_IMAGE:
                    LogUtils.i("event: " + event);
                    addImage(0, event.getImgUrl());
                    break;
                case EventManager.FLAG_SEND_LOCATION:
                    addLocation(0, event.getLa(), event.getLo(), event.getAddress());
                    break;
            }
        }
    }
}
