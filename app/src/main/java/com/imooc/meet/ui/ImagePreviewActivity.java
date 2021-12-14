package com.imooc.meet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.imooc.framework.base.BaseUIActivity;
import com.imooc.framework.bmob.BmobManager;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.helper.GlideHelper;
import com.imooc.framework.utils.SpUtils;
import com.imooc.meet.R;

import org.litepal.util.Const;

import java.io.File;

public class ImagePreviewActivity extends BaseUIActivity {

    public static void startActivity(Context context, boolean type, String url) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(Constants.INTENT_IMAGE_TYPE, type);
        intent.putExtra(Constants.INTENT_IMAGE_URL, url);
        context.startActivity(intent);
    }

    private PhotoView photo_view;
    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        initView();
    }

    private void initView() {
        photo_view = findViewById(R.id.photo_view);
        iv_back = findViewById(R.id.iv_back);

        Intent intent = getIntent();
        boolean type = intent.getBooleanExtra(Constants.INTENT_IMAGE_TYPE, false);
        String url = intent.getStringExtra(Constants.INTENT_IMAGE_URL);

        if (type) {
            GlideHelper.loadUrl(this, url, photo_view);
        } else {
            GlideHelper.loadFile(this, new File(url), photo_view);
        }

        iv_back.setOnClickListener(v -> finish());
    }

}
