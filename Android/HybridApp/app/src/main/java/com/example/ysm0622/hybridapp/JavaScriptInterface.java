package com.example.ysm0622.hybridapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.widget.LinearLayoutCompat;
import android.telephony.SmsManager;
import android.text.util.Linkify;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by ysm0622 on 2016-05-09.
 */
public class JavaScriptInterface {
    Context mContext;
    String mNum;

    public JavaScriptInterface(Context c) {
        mContext = c;
        mNum = "";
    }

    @JavascriptInterface
    public void receiveValue(String v) { // html파일로 부터 데이터 받기
        if (v.equals("<")) {             // 지우기를 눌렀을 때
            mNum = mNum.substring(0, mNum.length() - 1);
        } else {                         // 숫자나 별을 눌렀을 때
            mNum = mNum.concat(v);
        }
    }

    @JavascriptInterface
    public String getNum() { // Mobile Number getMethod ( call by html javascript )
        return mNum;
    }

    @JavascriptInterface
    public void submit(String msg) { // 전송버튼 눌렀을 때 ( call by html javascript )
        PendingIntent sentIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("SMS_DELIVERED_ACTION"), 0);
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                switch (this.getResultCode()) {
                    case -1:
                        Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
                    case 0:
                    default:
                        break;
                    case 1:
                        Toast.makeText(mContext, "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                }

            }
        }, new IntentFilter("SMS_SENT_ACTION"));
        mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                switch (this.getResultCode()) {
                    case -1:
                        Toast.makeText(mContext, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        Toast.makeText(mContext, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));
        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(mNum, (String) null, msg, sentIntent, deliveredIntent); // 메세지 전송
    }
}
