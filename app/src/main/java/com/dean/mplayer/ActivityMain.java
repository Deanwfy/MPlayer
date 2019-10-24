package com.dean.mplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dean.mplayer.base.BaseActivity;
import com.dean.mplayer.data.PrefDataSource_;
import com.dean.mplayer.search.ActivityMusicOnline_;
import com.dean.mplayer.util.LogUtils;
import com.dean.mplayer.view.common.ControlPanel;
import com.dean.mplayer.view.common.MToolbar;
import com.google.android.material.navigation.NavigationView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@EActivity(R.layout.activity_main)
public class ActivityMain extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Pref
    PrefDataSource_ prefDataSource;

    @ViewById(R.id.main_toolbar)
    MToolbar toolbar;

    @ViewById(R.id.music_control_panel)
    ControlPanel controlPanel;

    //睡眠定时计时器
    private Timer clockTimer;
    private boolean playFull;
    public static boolean needToStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 读取配置文件
        playFull = prefDataSource.isPlayFull().get();
    }

    @AfterViews
    void initViews(){
        // 状态栏透明
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);

        // 抽屉
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawertoggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(actionBarDrawertoggle);
        actionBarDrawertoggle.syncState();
        // 抽屉菜单
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setDrawerLeftEdgeSize(this, drawer);

        toolbar.setLeftItem(R.drawable.ic_drawer, view -> drawer.openDrawer(GravityCompat.START))
                .setRightItem(R.drawable.ic_search, view -> ActivityMusicOnline_.intent(this).start())
                .build();

        controlPanel.build(this);

        Intent intentPlayService = new Intent(this, PlayService.class);
        startService(intentPlayService);
    }

    //通过反射更改DrawerLayout默认滑动响应范围
    private void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout) {
        if (activity == null || drawerLayout == null) return;
        try {
            // 找到 ViewDragHelper 并设置 Accessible 为true
            Field leftDraggerField =
                    drawerLayout.getClass().getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);

            // 找到 edgeSizeField 并设置 Accessible 为true
            assert leftDragger != null;
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);

            // 设置新的边缘大小
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (displaySize.x *
                    (float) 0.1))); /*在这里调整*/
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            LogUtils.e("ActivityMain", e);
        }
    }

    // 退出同时结束后台服务
    @Override
    protected void onDestroy() {
        controlPanel.stopConnection();
        Intent intentPlayService = new Intent(this, PlayService.class);
        stopService(intentPlayService);
        super.onDestroy();
    }

    // 返回退回到桌面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            } else {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 抽屉菜单点击事件
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ic_menu_clock) {
            setClock();
        } else if (id == R.id.ic_menu_theme) {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isNightMode = currentNightMode != Configuration.UI_MODE_NIGHT_YES;
            prefDataSource.isNightMode().put(isNightMode);
            AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        } else if (id == R.id.ic_menu_settings) {
            Toast.makeText(this, "开发中...", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.ic_menu_exit) {
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // 睡眠定时弹窗
    @SuppressLint("InflateParams")
    private void setClock() {
        // 创建AlertDialog构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // AlertDialog数据
        List<String> items = new ArrayList<>();
        items.add("不设置定时");
        items.add("15分钟");
        items.add("30分钟");
        items.add("45分钟");
        items.add("60分钟");
        items.add("自定义");
        // 自定义布局
        View menuClock = LayoutInflater.from(ActivityMain.this).inflate(R.layout.drawer_menu_clock, null);
        // 设置AlertDialog参数，加载自定义布局
        builder.setTitle("睡眠定时").setView(menuClock);
        // AlertDialog对象
        final AlertDialog alertDialog = builder.create();
        // 自定义布局RecyclerLayout适配实现
        RecyclerView menuClockRecycler = menuClock.findViewById(R.id.menuClockRecycler);
        LinearLayoutManager menuClockRecyclerLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;   //不允许滑动
            }
        };
        menuClockRecycler.setLayoutManager(menuClockRecyclerLayoutManager);
        MenuClockRecyclerAdapter menuClockRecyclerAdapter = new MenuClockRecyclerAdapter(items);
        menuClockRecyclerAdapter.setOnItemClickListener((view, position) -> {
            switch (position) {
                case 0:
                    if (clockTimer != null) {
                        cancelClock();
                        Toast.makeText(ActivityMain.this, "已取消定时停止播放", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    runClock(900000);
                    break;
                case 2:
                    runClock(1800000);
                    break;
                case 3:
                    runClock(2700000);
                    break;
                case 4:
                    runClock(3600000);
                    break;
                case 5:
                    View customClock = LayoutInflater.from(ActivityMain.this).inflate(R.layout.drawer_menu_clock_custom, null);
                    final AlertDialog.Builder customBuilder = new AlertDialog.Builder(ActivityMain.this);
                    final NumberPicker customNumberPickerHour = customClock.findViewById(R.id.pickerHour);
                    final NumberPicker customNumberPickerMin = customClock.findViewById(R.id.pickerMin);
                    customNumberPickerHour.setMinValue(0);
                    customNumberPickerHour.setMaxValue(23);
                    customNumberPickerHour.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);   //不可编辑
                    customNumberPickerHour.setWrapSelectorWheel(false);    //不循环
                    customNumberPickerMin.setMinValue(0);
                    customNumberPickerMin.setMaxValue(59);
                    customNumberPickerMin.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);    //不可编辑
                    customNumberPickerMin.setWrapSelectorWheel(false);    //不循环
                    customBuilder.setTitle("自定义睡眠定时").setView(customClock)
                            .setPositiveButton("确定", (dialog, which) -> runClock(customNumberPickerHour.getValue() * 3600000 + customNumberPickerMin.getValue() * 60000))
                            .setNegativeButton("取消", (dialog, which) -> {
                            });
                    customBuilder.create().show();
                    break;
            }
            // 自定义布局需手动调用dismiss使AlertDialog消失
            alertDialog.dismiss();
        });
        menuClockRecycler.setAdapter(menuClockRecyclerAdapter);
        // 当前歌曲结束后再停止
        CheckBox playFullCheckBox = menuClock.findViewById(R.id.playFull);
        playFullCheckBox.setChecked(playFull);
        playFullCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playFull = isChecked;
            prefDataSource.isPlayFull().put(playFull);
        });
        // 显示AlertDialog
        alertDialog.show();
    }

    //睡眠定时计时器
    private void runClock(long clockTime) {
        cancelClock();
        if (clockTime / 3600000 > 0) {
            long hour = clockTime / 3600000;
            long min = (clockTime % 3600000) / 60000;
            Toast.makeText(this, "已设置" + hour + "小时" + min + "分钟后停止播放", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "已设置" + clockTime / 60000 + "分钟后停止播放", Toast.LENGTH_SHORT).show();
        }
        clockTimer = new Timer();
        clockTimer.schedule(new TimerTask() {
            public void run() {
                int playBackState = mediaController.getPlaybackState().getState();
                if (!playFull) {
                    if (playBackState == PlaybackStateCompat.STATE_PLAYING || playBackState == PlaybackStateCompat.STATE_PAUSED) {
                        mediaController.getTransportControls().stop();
                    }
                } else {
                    if (playBackState == PlaybackStateCompat.STATE_PAUSED) {
                        mediaController.getTransportControls().stop();
                    } else if (playBackState == PlaybackStateCompat.STATE_PLAYING) {
                        needToStop = true;
                    }
                }
            }
        }, clockTime);
    }

    private void cancelClock() {
        if (clockTimer != null) {
            clockTimer.cancel();
            needToStop = false;
        }
    }

}
