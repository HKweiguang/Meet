package com.imooc.framework.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.imooc.framework.utils.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileHelper {

    // 相机
    public static final int CAMERA_REQUEST_CODE = 1004;
    // 相册
    public static final int ALBUM_REQUEST_CODE = 1005;

    private static volatile FileHelper mInstance = null;

    private File tempFile = null;

    private Uri imageUri;

    private FileHelper() {
    }

    public static FileHelper getInstance() {
        if (mInstance == null) {
            synchronized (FileHelper.class) {
                if (mInstance == null) {
                    mInstance = new FileHelper();
                }
            }
        }

        return mInstance;
    }

    /**
     * 相机
     */
    @SuppressLint("SimpleDateFormat")
    public void toCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String fileName = simpleDateFormat.format(new Date()) + "jpg";
        tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);

        // 兼容Android 7.0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            imageUri = Uri.fromFile(tempFile);
        } else {
            // 利用 FileProvider
            imageUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", tempFile);
            // 添加权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        LogUtils.i("iamgeUri:" + imageUri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /**
     * 跳转到相册
     */
    public void toAlbum(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, ALBUM_REQUEST_CODE);
    }

    /**
     * 通过Uri去系统查询真实地址
     */
    public String getRealPathFromURI(Context context, Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(context, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
    }

    public File getTempFile() {
        return tempFile;
    }
}
