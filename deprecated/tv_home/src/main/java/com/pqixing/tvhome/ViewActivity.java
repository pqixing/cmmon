package com.pqixing.tvhome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static com.pqixing.tvhome.MainActivity.port;

public class ViewActivity extends Activity {
    String initStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            String content = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            initStr = content;
            Log.d("ViewActivity", "onCreate: " + content);
            if (content != null && content.startsWith("http")) {
                shareContent(content, false);
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_v);
        final EditText etShare = ((EditText) findViewById(R.id.etShareContent));
        etShare.setText(initStr);
        findViewById(R.id.btnShareContent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etShare.getText().toString().trim();
                if (content.isEmpty()) return;
                shareContent(content, false);
            }
        });
        final EditText etBg = ((EditText) findViewById(R.id.etBackGroup));
        findViewById(R.id.btnShareBg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etBg.getText().toString().trim();
                if (content.isEmpty() || (!content.startsWith("#")) && !content.startsWith("http"))
                    return;
                shareContent(content, true);
            }
        });
        setBgColor();
    }

    public void setBgColor() {
        final EditText etBg = ((EditText) findViewById(R.id.etBackGroup));
        final SeekBar a = ((SeekBar) findViewById(R.id.sbAlpha));
        final SeekBar r = ((SeekBar) findViewById(R.id.sbRed));
        final SeekBar g = ((SeekBar) findViewById(R.id.sbGreen));
        final SeekBar b = ((SeekBar) findViewById(R.id.sbBlue));
        SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String ah = Integer.toHexString(a.getProgress());
                String rh = Integer.toHexString(r.getProgress());
                String gh = Integer.toHexString(g.getProgress());
                String bh = Integer.toHexString(b.getProgress());
                String argb = "#"
                        + (ah.length() == 1 ? "0" : "") + ah
                        + (rh.length() == 1 ? "0" : "") + rh
                        + (gh.length() == 1 ? "0" : "") + gh
                        + (bh.length() == 1 ? "0" : "") + gh;
                etBg.setText(argb.toUpperCase());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        a.setOnSeekBarChangeListener(l);
        r.setOnSeekBarChangeListener(l);
        g.setOnSeekBarChangeListener(l);
        b.setOnSeekBarChangeListener(l);
    }

    public void shareContent(final String text, final boolean bg) {
        if (text.isEmpty()) return;
        Toast.makeText(this, "share:" + text, Toast.LENGTH_SHORT).show();
        new Thread() {
            @Override
            public void run() {
                String content = "" + (bg ? "pqx:bg//" : "pqx:url//") + new String(Base64.encode(text.getBytes(), 0));

                WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

                /*这里获取了IP地址，获取到的IP地址还是int类型的。*/
                int ip = wifiInfo.getIpAddress();

		/*这里就是将int类型的IP地址通过工具转化成String类型的，便于阅读
        String ips = Formatter.formatIpAddress(ip);
		*/

                /*这一步就是将本机的IP地址转换成xxx.xxx.xxx.255*/
                int broadCastIP = ip | 0xFF000000;

                DatagramSocket theSocket = null;
                try {
                    for (int i = 0; i < 256; i++) {
                        /*通过这个循环就将所有的本段中的所有IP地址都发送一遍了*/
                        InetAddress server = InetAddress.getByName(Formatter.formatIpAddress(broadCastIP | ((0xFF - i) << 24)));
                        theSocket = new DatagramSocket();
                        DatagramPacket theOutput = new DatagramPacket(content.getBytes(), content.length(), server, port);
                        /*这一句就是发送广播了，其实255就代表所有的该网段的IP地址，是由路由器完成的工作*/
                        theSocket.send(theOutput);
                        Log.d("TAG", "send: " + server + port+" : " + content);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (theSocket != null)
                        theSocket.close();
                }
            }
        }.start();
    }
}
