package com.imooc.framework.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.imooc.framework.event.EventManager;
import com.imooc.framework.event.MessageEvent;
import com.imooc.framework.utils.LanguaueUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BaseFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventManager.register(this);
    }

    /**
     * 判断窗口权限
     *
     * @return tyue-false
     */
    protected boolean checkWindowPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(getActivity());
        }
        return true;
    }

    /**
     * 请求窗口权限
     */
    protected void requestWindowPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(getActivity(), "申请窗口权限，暂时没做UI交互", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                    , Uri.parse("package:" + requireActivity().getPackageName()));
            startActivityForResult(intent, BaseActivity.PERMISSION_WINDOW_REQUEST_CODE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventManager.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.getType() == EventManager.EVENT_RUPDATE_LANGUAUE) {
            LanguaueUtils.updateLanguaue(requireActivity());
            requireActivity().recreate();
        }
    }
}
