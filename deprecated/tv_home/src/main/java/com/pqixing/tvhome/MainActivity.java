package com.pqixing.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    public static final int port = 9081;
    HomeAdapter adapter;
    //    HomeReceiver receiver;
    private SharedPreferences sp;
    int colum = 4;
    Handler handle = new Handler();
    TextView tvTime;
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy年MM月dd日 (E)", Locale.CHINA);
    TextView tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Set<String> categories = getIntent().getCategories();
        if (categories == null || !categories.contains(Intent.CATEGORY_HOME)) {
            startActivity(new Intent(this, ViewActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_m);
        sp = getSharedPreferences("launch", 0);
//        receiver = new HomeReceiver();
//        registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        if (adapter == null) {
            adapter = new HomeAdapter(this, getPackageManager());
            refreshData();
        }
        ((GridView) findViewById(R.id.gv)).setAdapter(adapter);
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        new Thread(scanBrocast).start();
        setBackGroup(sp.getString("backGroup", ""));
    }

    Runnable timeCheck = new Runnable() {
        @Override
        public void run() {
            Date date = new Date();
            tvTime.setText(formatTime.format(date));
            tvDate.setText(formatDate.format(date));
            handle.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handle.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handle.post(timeCheck);
    }

    private void setBackGroup(String bg) {
        if (bg == null || bg.isEmpty()) return;
        final ImageView contentView = findViewById(R.id.bg);
        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;
        if (bg.startsWith("#"))
            contentView.setImageDrawable(new ColorDrawable(Color.parseColor(bg)));
        else if (bg.startsWith("http"))
            Glide.with(this).asBitmap().load(bg).into(new CustomTarget<Bitmap>(w, h) {

                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    contentView.setImageDrawable(new BitmapDrawable(getResources(), resource));
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
    }

    private final Runnable scanBrocast = new Runnable() {
        long lastTime = 0L;

        @Override
        public void run() {
            byte[] buffer = new byte[10240];
            /*在这里同样使用约定好的端口*/
            DatagramSocket server = null;
            try {
                server = new DatagramSocket(port);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (!isFinishing()) {
                    try {
                        server.receive(packet);
                        String content = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                        if (System.currentTimeMillis() - lastTime < 2000L) continue;
                        lastTime = System.currentTimeMillis();

                        Log.d("TAG", "address : " + packet.getAddress() + ", port : " + packet.getPort() + ", content : " + content);
                        if (content.startsWith("pqx:bg//")) {
                            final String bg = new String(Base64.decode(content.substring(8), Base64.DEFAULT));
                            sp.edit().putString("backGroup", bg).apply();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setBackGroup(bg);
                                }
                            });
                        } else if (content.startsWith("pqx:url//")) {
                            String url = new String(Base64.decode(content.substring(9), Base64.DEFAULT));
                            startActionView(url);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } finally {
                if (server != null)
                    server.close();
            }
        }
    };

    private void startActionView(final String url) {
        if (url.startsWith("http")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", url);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                Toast.makeText(getApplicationContext(), "copy:" + url, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(receiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int old = 0;

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    startActivity(adapter.getItem(adapter.selectItem).intent);
                    break;
                //模拟器测试时键盘中的的Enter键，模拟ok键（推荐TV开发中使用蓝叠模拟器）
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    old = adapter.selectItem;
                    adapter.selectItem = Math.min(old + colum, adapter.getCount() - 1);
                    adapter.notifyDataSetChanged();
                    break;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    old = adapter.selectItem;
                    adapter.selectItem = Math.max(old - 1, 0);
                    adapter.notifyDataSetChanged();
                    break;

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    old = adapter.selectItem;
                    adapter.selectItem = Math.min(old + 1, adapter.getCount() - 1);
                    adapter.notifyDataSetChanged();
                    break;

                case KeyEvent.KEYCODE_DPAD_UP:
                    old = adapter.selectItem;
                    adapter.selectItem = Math.max(old - colum, 0);
                    adapter.notifyDataSetChanged();
                    break;
                case KeyEvent.KEYCODE_MENU:
                    showDetailDialog();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    refreshData();
                    break;
            }
//            Toast.makeText(this, "key" + event.getKeyCode(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void showDetailDialog() {
        final Item item = adapter.getItem(adapter.selectItem);
        String[] arrays = new String[]{"详情", "卸载", "设置", "分享"};
        new AlertDialog.Builder(this).setTitle(item.name + ":" + item.info.packageName)
                .setSingleChoiceItems(arrays, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + item.info.packageName)));
                                break;
                            case 1:
                                startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + item.info.packageName)));

                                break;
                            case 2:
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                                break;
                            case 3:
                                startActivity(new Intent(MainActivity.this, ViewActivity.class));
                                break;
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshData();
    }

    private void refreshData() {
//        Toast.makeText(this, "刷新数据", Toast.LENGTH_SHORT).show();
        new Thread() {
            @Override
            public void run() {
                PackageManager pm = getPackageManager();
                String packageName = getPackageName();
                List<ApplicationInfo> infos = pm.getInstalledApplications(0);
                final ArrayList<Item> items = new ArrayList<>();
                for (ApplicationInfo i : infos) {
                    if (packageName.equals(i.packageName)) continue;
                    Intent intent = pm.getLaunchIntentForPackage(i.packageName);
                    if (intent == null) continue;
                    Item item = new Item();
                    item.info = i;
                    item.icon = i.loadIcon(pm);
                    item.intent = intent;
                    item.name = i.loadLabel(pm).toString();
                    items.add(item);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.infos = items;
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }
}