package com.pangjie.eyeprotect.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

public class AndroidUtil {
    /**
     * 获得当前屏幕亮度值  0--255
     */
    public static int getScreenBrightness(Context context) {
        int screenBrightness = 255;
        try {
            screenBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception localException) {

        }
        return screenBrightness;
    }


    /**
     * 2.设置 APP界面屏幕亮度值方法
     * 0-255
     **/
    public static void setAppScreenBrightness(Activity activity, int birghtessValue) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = birghtessValue / 255.0f;
        window.setAttributes(lp);

    }

    /**
     * 判断权限是否正在运行
     *
     * @param mContext    全局环境
     * @param serviceName 服务名
     * @return 布尔类型变量:true-服务正在运行;false-服务没有运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
