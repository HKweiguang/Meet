package com.imooc.meet.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.imooc.framework.base.BaseBackActivity;
import com.imooc.framework.helper.FileHelper;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.manager.DialogManager;
import com.imooc.framework.view.DialogView;
import com.imooc.framework.view.LoadingView;
import com.imooc.meet.R;

import java.io.File;

import cn.bmob.v3.exception.BmobException;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirstUploadActivity extends BaseBackActivity implements View.OnClickListener {

    public static void startActivity(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, FirstUploadActivity.class), requestCode);
    }

    private CircleImageView iv_photo;
    private EditText et_nickname;
    private Button btn_upload;

    private LoadingView mLoadingView;

    private DialogView photoSelectDialog;
    private TextView mTvCamera;
    private TextView mTvAblum;
    private TextView mTvCancel;

    private File uploadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_upload);

        initView();

        initPhotoView();
    }

    private void initView() {
        mLoadingView = new LoadingView(this);
        mLoadingView.setLoadingText("正在上传头像...");

        iv_photo = findViewById(R.id.iv_photo);
        et_nickname = findViewById(R.id.et_nickname);
        btn_upload = findViewById(R.id.btn_upload);

        iv_photo.setOnClickListener(this);
        btn_upload.setOnClickListener(this);

        btn_upload.setEnabled(false);
        et_nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btn_upload.setEnabled(uploadFile != null);
                } else {
                    btn_upload.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initPhotoView() {
        photoSelectDialog = DialogManager.getInstance().initView(this, R.layout.dialog_select_photo, Gravity.BOTTOM);
        mTvCamera = photoSelectDialog.findViewById(R.id.tv_camera);
        mTvAblum = photoSelectDialog.findViewById(R.id.tv_ablum);
        mTvCancel = photoSelectDialog.findViewById(R.id.tv_cancel);

        mTvCamera.setOnClickListener(this);
        mTvAblum.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo:
                DialogManager.getInstance().show(photoSelectDialog);
                break;
            case R.id.btn_upload:
                uploadFile();
                break;
            case R.id.tv_camera:
                DialogManager.getInstance().hide(photoSelectDialog);
                FileHelper.getInstance().toCamera(this);
                break;
            case R.id.tv_ablum:
                DialogManager.getInstance().hide(photoSelectDialog);
                FileHelper.getInstance().toAlbum(this);
                break;
            case R.id.tv_cancel:
                DialogManager.getInstance().hide(photoSelectDialog);
                break;
        }
    }

    /**
     * 上传头像
     */
    private void uploadFile() {
        String nickName = et_nickname.getText().toString().trim();
        mLoadingView.show();
        BmobManager.getInstance().uploadFirstPhoto(nickName, uploadFile, new BmobManager.OnUploadPhotoListener() {
            @Override
            public void onUpdateDone() {
                mLoadingView.hide();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onUpdateFail(BmobException e) {
                mLoadingView.hide();
                Toast.makeText(FirstUploadActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FileHelper.CAMERA_REQUEST_CODE) {
                uploadFile = FileHelper.getInstance().getTempFile();
            }
            if (requestCode == FileHelper.ALBUM_REQUEST_CODE) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileHelper.getInstance().getRealPathFromURI(this, uri);
                    uploadFile = new File(path);
                }
            }
            if (uploadFile != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(uploadFile.getPath());
                iv_photo.setImageBitmap(bitmap);

                String nickName = et_nickname.getText().toString().trim();
                btn_upload.setEnabled(!TextUtils.isEmpty(nickName));
            }
        }
    }
}
