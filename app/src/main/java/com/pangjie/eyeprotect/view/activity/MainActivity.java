package com.pangjie.eyeprotect.view.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import com.pangjie.eyeprotect.R;
import com.pangjie.eyeprotect.base.BaseActivity;
import com.pangjie.eyeprotect.common.AndroidUtil;
import com.pangjie.eyeprotect.common.utils.PermissionUtil;
import com.pangjie.eyeprotect.impl.MainPresenterImpl;
import com.pangjie.eyeprotect.presenter.IMainPresenter;
import com.pangjie.eyeprotect.view.service.EyeProtectService;
import com.pangjie.eyeprotect.view.service.TimeService;
import com.pangjie.eyeprotect.view.widget.ScrollLayout;

import butterknife.BindView;


public class MainActivity extends BaseActivity<IMainPresenter> implements View.OnClickListener {

    @BindView(R.id.iv_play)
    AppCompatImageView ivPlay;
    @BindView(R.id.tv_play)
    TextView tvPlay;
    @BindView(R.id.scroll_down_layout)
    ScrollLayout mScrollLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.sb_brightness)
    SeekBar sbBright;
    @BindView(R.id.select_dim)
    SeekBar sbSelectDim;//透明度
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_subtitle)
    TextView subTitle;
    private boolean openSymbol = false;
    private boolean charOpen;

    //定义和service通信的binder
    private EyeProtectService.MyBinder myBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (EyeProtectService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private ScrollLayout.OnScrollChangedListener mOnScrollChangedListener = new ScrollLayout.OnScrollChangedListener() {
        @Override
        public void onScrollProgressChanged(float currentProgress) {
            if (currentProgress >= 0) {
                float percent = 255 * currentProgress;
                if (percent > 255) {
                    percent = 255;
                } else if (percent < 0) {
                    percent = 0;
                }
                Log.i("test", "pecent:" + (255 - (int) percent));
//                mScrollLayout.getBackground().setAlpha(255 - (int) precent);
                toolbar.getBackground().setAlpha(255 - (int) percent);
                llContent.setBackgroundColor(getResources().getColor(R.color.gray_deep));
                if (percent == 0) {
                    llContent.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
                llContent.getBackground().setAlpha(255 - (int) percent);
                toolbar.getNavigationIcon().setAlpha(255 - (int) percent);
                subTitle.setAlpha((255f - percent) / 255f);
                tvTitle.setAlpha(percent / 255f);
            }
        }

        @Override
        public void onScrollFinished(ScrollLayout.Status currentStatus) {
            if (currentStatus.equals(ScrollLayout.Status.CLOSED)) {
                charOpen = true;
//                finish();
            } else {
                charOpen = false;
            }
            Log.i("test", "1.....");
        }

        @Override
        public void onChildScroll(int top) {
            Log.i("test", "2.....");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!PermissionUtil.hasOverlay(this)) {
            showDialogTipUserRequestPermission();
        }

    }

    @Override
    protected IMainPresenter initInjector() {
        return new MainPresenterImpl();
    }


    @Override
    public void initView() {
//        tfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
//        toolbar.getBackground().setAlpha(0);
//        toolbar.setNavigationIcon(R.mipmap.action_bar_return);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mScrollLayout.setOnScrollChangedListener(mOnScrollChangedListener);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ivPlay.setOnClickListener(this);
        sbBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                AndroidUtil.setAppScreenBrightness(MainActivity.this, seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbSelectDim.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putInt("dim", i);
                editor.apply();
                if (AndroidUtil.isServiceWork(getApplicationContext(), "com.pangjie.eyeprotect.view.service.EyeProtectService")) {
                    myBinder.changeAlpha(i);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        initToolbar();
    }


    @Override
    protected void initData() {
        sbBright.setMax(255);
        sbBright.setProgress(AndroidUtil.getScreenBrightness(this));
        Intent Intent = new Intent(MainActivity.this, EyeProtectService.class);
        bindService(Intent, connection, BIND_AUTO_CREATE);
        setTitleText(getString(R.string.app_name));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //检查服务是否在运行
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        if (pref.getBoolean("onTimeOpen", false)) {
            if (!AndroidUtil.isServiceWork(getApplicationContext(), "com.pangjie.eyeprotect.view.service.TimeService")) {
                startService(new Intent(getApplicationContext(), TimeService.class));
            }
        }

    }

    /**
     * 初始展示toobar的颜色等相关
     */
    private void initToolbar() {
        toolbar.getBackground().setAlpha(0);
        llContent.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        llContent.getBackground().setAlpha(0);
        toolbar.getNavigationIcon().setAlpha(0);
        subTitle.setAlpha(0);
    }

    @Override
    public void onBackPressed() {
        if (charOpen) {
            mScrollLayout.setToOpen();
            mScrollLayout.computeScroll();
            return;
        }
//        super.onBackPressed();
    }

    /**
     * 请求权限弹出框
     */
    private void showDialogTipUserRequestPermission() {
        new AlertDialog.Builder(this)
                .setTitle("权限不可用")
                .setMessage("由于本app需要获取您的悬浮框和系统设置授权；\n否则，您将无法使用本App")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
    }

    /**
     * 开始申请权限
     */
    private void startRequestPermission() {
        getOverlayPermission();
//        getSettingPermission();
    }

    /**
     * 申请悬浮窗权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void getOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 0);
    }

    /**
     * 申请系统设置权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void getSettingPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, 0);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play:
                if (openSymbol) {
//                    //关闭护眼
                    openSymbol = false;
//                    main_layout.setBackgroundResource(R.drawable.image_day);
//                    bt.setImageResource(R.drawable.sun);
                    unbindService(connection);
                    ivPlay.setImageResource(R.drawable.ic_play);
                    tvPlay.setText(getString(R.string.pause));
                } else {
                    //开启护眼
                    openSymbol = true;
                    ivPlay.setImageResource(R.drawable.ic_pause);
                    tvPlay.setText(getString(R.string.anti_blue));
                    if (!AndroidUtil.isServiceWork(getApplicationContext(), "com.pangjie.eyeprotect.view.service.EyeProtectService")) {
                        Intent Intent = new Intent(MainActivity.this, EyeProtectService.class);
                        bindService(Intent, connection, BIND_AUTO_CREATE);
                    }
                }
                break;

        }
    }

    private void setTitleText(CharSequence title) {
        if (tvTitle != null) {
            toolbar.setTitle("");
            tvTitle.setText(title);
        } else {
            toolbar.setTitle(title);
        }
    }
}
