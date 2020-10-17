package com.pangjie.eyeprotect.view.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.pangjie.eyeprotect.R;
import com.pangjie.eyeprotect.view.activity.SettingActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimeService extends Service {

    private static final int NOTIFY_ID = 2;
    private int start_hour;
    private int start_minute;
    private int end_hour;
    private int end_minute;
    private Timer timer;

    public TimeService() {
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(){
        super.onCreate();
        load_setting();
        startTimeService();
        showNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 开始定时任务
     */
    private void startTimeService(){
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        if(pref.getBoolean("onTimeOpen", false)){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Calendar c = Calendar.getInstance();
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int minute = c.get(Calendar.MINUTE);
                    if (start_hour<end_hour || (start_hour==end_hour && start_minute<end_minute)){
                        if ((hour>start_hour && hour<end_hour) || (hour==start_hour && minute>=start_minute && minute<end_minute) || (hour==end_hour && minute<end_minute && hour>start_hour) || (hour==end_hour && minute<end_minute && hour==start_hour && minute>=start_minute)){
                            if (!isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.EyeProtectService")){
                                startService(new Intent(getApplicationContext(), EyeProtectService.class));
                            }
                        }else{
                            if (isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.EyeProtectService")){
                                stopService(new Intent(getApplicationContext(), EyeProtectService.class));
                            }
                        }
                    }else{
                        if ((hour>start_hour || hour<end_hour) || (hour==start_hour && minute>=start_minute) || (hour==end_hour && minute<end_minute)){
                            if (!isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.EyeProtectService")){
                                startService(new Intent(getApplicationContext(), EyeProtectService.class));
                            }
                        }else{
                            if (isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.EyeProtectService")){
                                stopService(new Intent(getApplicationContext(), EyeProtectService.class));
                            }
                        }
                    }
                }
            }, 0, 10000);
        }
    }

    /**
     * 通知栏显示信息
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("Eye Protect").setContentText("定时任务已开启");
        //创建通知背点击时触发的Intent
        Intent resultIntent = new Intent(this, SettingActivity.class);
        //创建任务栈Builder
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SettingActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //构建通知
        final Notification notification = mBuilder.build();
        //显示通知
        mNotifyMgr.notify(NOTIFY_ID, notification);
        //启动为前台服务
        startForeground(NOTIFY_ID, notification);
    }

    /**
     * 加载设置
     */
    private void load_setting(){
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        start_hour = pref.getInt("StartHour", 23);
        start_minute = pref.getInt("StartMinute", 0);
        end_hour = pref.getInt("EndHour", 6);
        end_minute = pref.getInt("EndMinute", 0);
    }

    /**
     * 判断权限是否正在运行
     * @param mContext 全局环境
     * @param serviceName 服务名
     * @return 布尔类型变量:true-服务正在运行;false-服务没有运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        timer.cancel();
    }
}
