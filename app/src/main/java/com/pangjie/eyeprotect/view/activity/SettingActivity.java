package com.pangjie.eyeprotect.view.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pangjie.eyeprotect.R;
import com.pangjie.eyeprotect.view.service.EyeProtectService;
import com.pangjie.eyeprotect.view.service.TimeService;

import java.util.List;


public class SettingActivity extends AppCompatActivity {

    //声明控件
    private Switch open_ontime, modify_brightness;
    private LinearLayout view_open_ontime;
    private LinearLayout view_modify_brightness;
    private TextView start_time, end_time;
    private SeekBar select_brightness, select_dim;

    //声明变量
    private int start_hour, start_minute;
    private int end_hour, end_minute;
    private int dim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //绑定控件
        open_ontime = (Switch) findViewById(R.id.switch_open_ontime);
        view_open_ontime = (LinearLayout)findViewById(R.id.eye_protect_ontime);
        modify_brightness = (Switch) findViewById(R.id.switch_modify_brightness);
        view_modify_brightness = (LinearLayout) findViewById(R.id.brightness_setting);
        ImageButton start_time_picker = (ImageButton) findViewById(R.id.select_start_time);
        ImageButton end_time_picker = (ImageButton) findViewById(R.id.select_end_time);
        start_time = (TextView)findViewById(R.id.start_time);
        end_time = (TextView)findViewById(R.id.end_time);
        select_brightness = (SeekBar) findViewById(R.id.select_brightness);
        select_dim = (SeekBar)findViewById(R.id.select_dim);

        //初始设置
        load_state();
        init_view();
        select_dim.setProgress(dim);

        //设置监听
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        open_ontime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    view_open_ontime.setVisibility(View.VISIBLE);
                    start_time.setText(start_hour+":"+start_minute);
                    end_time.setText(end_hour+":"+end_minute);
                    if (!isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.TimeService")){
                        save_state();
                        startService(new Intent(getApplicationContext(), TimeService.class));
                    }
                }else{
                    view_open_ontime.setVisibility(View.GONE);
                    save_state();
                    if (isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.TimeService")){
                        stopService(new Intent(getApplicationContext(), TimeService.class));
                        if (isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.EyeProtectService")){
                            stopService(new Intent(getApplicationContext(), EyeProtectService.class));
                        }
                    }
                }
            }
        });

        modify_brightness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this);
                    dialog.setTitle("提示");
                    dialog.setMessage("该功能会关闭系统的自动亮度调节，请问是否确定打开？");
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            modify_brightness.setChecked(false);
                            view_modify_brightness.setVisibility(View.GONE);
                        }
                    });
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            view_modify_brightness.setVisibility(View.VISIBLE);
                            setBrightness();
                        }
                    });
                    dialog.show();
                }else{
                    view_modify_brightness.setVisibility(View.GONE);
                }
            }
        });

        start_time_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTimePicker(23,0,1);
            }
        });
        end_time_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTimePicker(6,0,2);
            }
        });

        select_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try{
                    //设置当前屏幕亮度
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, i);
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                }
                catch (Exception localException){
                    Log.d("set brightness error", localException.toString());
                    Toast.makeText(SettingActivity.this, "set brightness error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        select_dim.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                dim = i;
                save_state();
                if (isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.EyeProtectService")){
                    stopService(new Intent(getApplicationContext(), EyeProtectService.class));
                    startService(new Intent(getApplicationContext(), EyeProtectService.class));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    /**
     * 选择时间
     * @param hour 初始小时
     * @param minute 初始分钟
     * @param symbol 开始时间还是结束时间标志
     */
    private void setTimePicker(int hour, int minute, final int symbol){
        TimePickerDialog timePickerDialog = new TimePickerDialog(SettingActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                switch (symbol){
                    case 1:
                        start_time.setText(i+":"+i1);
                        start_hour = i;
                        start_minute = i1;
                        if (!isServiceWork(SettingActivity.this, "cn.lhmachine.eyeprotect.TimeService")){
                            startService(new Intent(getApplicationContext(), TimeService.class));
                        }else{
                            stopService(new Intent(getApplicationContext(), TimeService.class));
                            save_state();
                            startService(new Intent(getApplicationContext(), TimeService.class));
                        }
                        break;
                    case 2:
                        end_hour = i;
                        end_minute = i1;
                        end_time.setText(String.valueOf(end_hour)+":"+String.valueOf(end_minute));
                        if (!isServiceWork(getApplicationContext(), "cn.lhmachine.eyeprotect.TimeService")){
                            startService(new Intent(getApplicationContext(), TimeService.class));
                        }else{
                            stopService(new Intent(getApplicationContext(), TimeService.class));
                            save_state();
                            startService(new Intent(getApplicationContext(), TimeService.class));
                        }
                        break;
                }
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    /**
     * 初始化view
     */
    private void init_view(){
        if (open_ontime.isChecked()){
            view_open_ontime.setVisibility(View.VISIBLE);
            start_time.setText(start_hour+":"+start_minute);
            end_time.setText(end_hour+":"+end_minute);
        }else{
            view_open_ontime.setVisibility(View.GONE);
        }

        if (modify_brightness.isChecked()){
            view_modify_brightness.setVisibility(View.VISIBLE);
            setBrightness();
        }else{
            view_modify_brightness.setVisibility(View.GONE);
        }
    }

    /**
     * 设置系统亮度
     */
    private void setBrightness(){
        int screenBrightness;
        try{
            screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            select_brightness.setProgress(screenBrightness);
        }
        catch (Exception localException){
            Log.d("get brightness error", localException.toString());
            Toast.makeText(SettingActivity.this, "get brightness error", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 加载设置
     */
    private void load_state(){
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        open_ontime.setChecked(pref.getBoolean("onTimeOpen", false));
        modify_brightness.setChecked(pref.getBoolean("BrightnessOpen", false));
        start_hour = pref.getInt("StartHour", 23);
        start_minute = pref.getInt("StartMinute", 0);
        end_hour = pref.getInt("EndHour", 6);
        end_minute = pref.getInt("EndMinute", 0);
        dim = pref.getInt("dim", 50);
    }

    /**
     * 保存设置
     */
    private void save_state(){
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putBoolean("onTimeOpen", open_ontime.isChecked());
        editor.putBoolean("BrightnessOpen", modify_brightness.isChecked());
        editor.putInt("StartHour", start_hour);
        editor.putInt("StartMinute", start_minute);
        editor.putInt("EndHour", end_hour);
        editor.putInt("EndMinute", end_minute);
        editor.putInt("dim", dim);
        editor.apply();
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
    protected void onPause(){
        super.onPause();
        save_state();
    }
}
