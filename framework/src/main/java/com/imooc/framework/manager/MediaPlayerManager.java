package com.imooc.framework.manager;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.imooc.framework.utils.LogUtils;

import java.io.IOException;

public class MediaPlayerManager {

    // 播放
    public static final int MEDIA_STATUS_PLAY = 0;

    // 暂停
    public static final int MEDIA_STATUS_PAUSE = 1;

    // 停止
    public static final int MEDIA_STATUS_STOP = 2;

    public int MEDIA_STATUS = MEDIA_STATUS_STOP;

    private final MediaPlayer mMediaPlayer;

    private static final int H_PROGRESS = 1000;

    private OnMusicProgressListener musicProgressListener;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == H_PROGRESS) {
                if (musicProgressListener != null) {
                    int currentPosition = getCurrentPosition();
                    int p = (int) ((float) currentPosition / (float) getDuration() * 100);
                    musicProgressListener.onProgress(currentPosition, p);
                    mHandler.sendEmptyMessageDelayed(H_PROGRESS, 1000);
                }
            }
            return false;
        }
    });

    public MediaPlayerManager() {
        mMediaPlayer = new MediaPlayer();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    /**
     * 开始播放
     *
     * @param path 文件路径
     */
    public void startPlay(AssetFileDescriptor path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path.getFileDescriptor(), path.getStartOffset(), path.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            MEDIA_STATUS = MEDIA_STATUS_PLAY;
            mHandler.sendEmptyMessage(H_PROGRESS);
        } catch (IOException e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     *
     * @param path 文件路径
     */
    public void startPlay(String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            MEDIA_STATUS = MEDIA_STATUS_PLAY;
            mHandler.sendEmptyMessage(H_PROGRESS);
        } catch (IOException e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if (isPlaying()) {
            mMediaPlayer.pause();

            MEDIA_STATUS = MEDIA_STATUS_PAUSE;
            mHandler.removeMessages(H_PROGRESS);
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay() {
        mMediaPlayer.start();

        MEDIA_STATUS = MEDIA_STATUS_PLAY;
        mHandler.sendEmptyMessage(H_PROGRESS);
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        mMediaPlayer.stop();

        MEDIA_STATUS = MEDIA_STATUS_STOP;
        mHandler.removeMessages(H_PROGRESS);
    }

    /**
     * 获取当前位置
     *
     * @return 当前位置
     */
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 获取总时长
     *
     * @return 总时长
     */
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    /**
     * 是否循环
     *
     * @param isLooping true-是  false-否
     */
    public void setLooping(boolean isLooping) {
        mMediaPlayer.setLooping(isLooping);
    }

    /**
     * 跳转位置
     */
    public void seekTo(int ms) {
        mMediaPlayer.seekTo(ms);
    }

    /**
     * 播放结束
     */
    public void setOnComplteionListener(MediaPlayer.OnCompletionListener listener) {
        mMediaPlayer.setOnCompletionListener(listener);
    }

    /**
     * 播放错误
     */
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        mMediaPlayer.setOnErrorListener(listener);
    }

    /**
     * 播放进度
     */
    public void setOnProgressListener(OnMusicProgressListener listener) {
        musicProgressListener = listener;
    }

    public interface OnMusicProgressListener {
        void onProgress(int progress, int pos);
    }

}
