package com.thundersoft.demo_nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private String TAG = "demo_nfc";
    private AppBarConfiguration appBarConfiguration;
    private NfcAdapter nfcAdapter = null;
    private PendingIntent pi;
    public Boolean flag = false;
    private int IdCard=0;
    private int number_shanqu=0;
    private int number_kuai=0;
    private int size=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate->" + "get nfc adapter");
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Log.i(TAG, "onCreate->" + "set PendingIntent");
        pi = PendingIntent.getActivity(MainActivity.this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);

        ImageButton imageButton = findViewById(R.id.image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nfcAdapter == null) {
                    Toast.makeText(MainActivity.this, "设备不支持NFC", Toast.LENGTH_SHORT).show();
                } else if (!nfcAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this, "请先打开NFC功能", Toast.LENGTH_SHORT).show();
                } else {
                    if (flag) {
                        imageButton.setImageResource(R.drawable.nfc_close);
                        flag = false;
                        Toast.makeText(MainActivity.this,"NFC扫描已关闭",Toast.LENGTH_SHORT).show();
                    } else {
                        imageButton.setImageResource(R.drawable.nfc_start);
                        flag = true;
                        Toast.makeText(MainActivity.this,"NFC扫描已打开",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        Button button=findViewById(R.id.button);
        TextView textView1=findViewById(R.id.text1);
        TextView textView2=findViewById(R.id.text2);
        TextView textView3=findViewById(R.id.text3);
        TextView textView4=findViewById(R.id.text4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag){
                    textView1.setText("卡号:"+IdCard);
                    textView2.setText("扇区:"+number_shanqu);
                    textView3.setText("块数:"+number_kuai);
                    textView4.setText("大小:"+size);
                }
                else {
                    Toast.makeText(MainActivity.this,"请先打开NFC扫描",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    //获取数据
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 当前app正在前端界面运行，这个时候有intent发送过来，那么系统就会调用onNewIntent回调方法，将intent传送过来
        // 我们只需要在这里检验这个intent是否是NFC相关的intent，如果是，就调用处理方法
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Log.i(TAG, "onNewIntent->" + "get intent");
            processIntent(intent);
        }
    }

    private String bytesToString(byte[] src) {
        String s = new String(src);
        String removeStr = "abcdefghijkl";
        return s.replace(removeStr, "");
    }

    public String readTag(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        for (String tech : tag.getTechList()) {
            System.out.println(tech);
        }
        boolean auth = false;
        //读取TAG
        try {
            String metaInfo = "";
            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()
                    + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_NFC_FORUM);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            return metaInfo;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (mfc != null) {
                try {
                    mfc.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return null;
    }


    //启动
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume->" + "start NFC");
        nfcAdapter.enableForegroundDispatch(this, pi, null, null);
    }

    //解析
    private void processIntent(Intent intent) {
        //取出封装在intent中的TAG
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String CardId = ByteArrayToHexString(tagFromIntent.getId());
        IdCard=Integer.parseInt(CardId,16);
        MifareClassic mifareClassic = MifareClassic.get(tagFromIntent);

        number_shanqu = mifareClassic.getSectorCount();
        number_kuai = mifareClassic.getBlockCount();
        size = mifareClassic.getSize();

        Log.i(TAG, "processIntent->" + "tagFromIntent" + tagFromIntent.toString());
        Log.i(TAG, "processIntent->" + "mifareClassic" + mifareClassic);
        Log.i(TAG, "processIntent->" + "number_shanqu" + number_shanqu);
        Log.i(TAG, "processIntent->" + "number_kuai" + number_kuai);
        Log.i(TAG, "processIntent->" + "size" + size);
        Log.i(TAG, "processIntent->" + "CarId" + Integer.parseInt(CardId, 16));


    }

    //转为16进制字符串
    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";

        Log.i(TAG, "processIntent->" + "To 16");
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }
}