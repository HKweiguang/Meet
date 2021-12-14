package com.imooc.framework.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.imooc.framework.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void i(String text) {
        if (BuildConfig.LOG_DEBUG) {
            if (!TextUtils.isEmpty(text)) {
                Log.i(BuildConfig.LOG_TAG, text);
                writeToFile(text);
            }
        }
    }

    public static void e(String text) {
        if (BuildConfig.LOG_DEBUG) {
            if (!TextUtils.isEmpty(text)) {
                Log.e(BuildConfig.LOG_TAG, text);
                writeToFile(text);
            }
        }
    }

    @SuppressLint("SdCardPath")
    private static void writeToFile(String text) {
        // 文件路径
        String fileName = "/sdcard/Meet/Meet.log";
        // 时间 内容
        String log = mSimpleDateFormat.format(new Date()) + " " + text + "\n";
        File fileGroup = new File("/sdcard/Meet/");
        if (!fileGroup.exists()) {
            boolean mk = fileGroup.mkdirs();
            if (!mk) {
                return;
            }
        }

        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = new FileOutputStream(fileName, true);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, Charset.forName("gbk")));
            bufferedWriter.write(log);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
