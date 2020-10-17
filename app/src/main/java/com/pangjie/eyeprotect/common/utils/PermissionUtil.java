package com.pangjie.eyeprotect.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

/**
 * 权限申请
 */
public class PermissionUtil {
    /**
     * 检查是否有悬浮权限
     *
     * @return
     */
    public static boolean hasOverlay(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager pm = context.getPackageManager();
//            if (!Settings.System.canWrite(context) || !Settings.canDrawOverlays(context)) {
            if (!Settings.canDrawOverlays(context)) {
                //未被授予权限
                return false;
            }
        }
        return true;
    }
}
