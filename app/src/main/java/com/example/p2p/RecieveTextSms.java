package com.example.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by Rizwan on 5/13/2017.
 */
public class RecieveTextSms extends BroadcastReceiver {
    String action;
    public static String from;
    String message;
    public static String[] info;
    public static String[] ipProb;
    public String[] Call;
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        action = intent.getAction();

        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;


        if (null != bundle) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];

            byte[] data = null;

            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                from = msgs[i].getDisplayOriginatingAddress();
                data = msgs[i].getUserData();

                for (int index = 0; index < data.length; ++index) {
                    message += Character.toString((char) data[index]);
                }

            }

        }

              Call=message.split(";");
        Log.d(Call[0],".........................");
        Log.d("calling port......",Call[1]);
        if (Call[0].equalsIgnoreCase("nullp2pCall920")) {
            Intent call = new Intent(context, CallreceiveActivity.class);
            call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            call.putExtra("Calling", Call[1]);
            context.startActivity(call);
        } else {

            info = message.split(";");
            ipProb = info[0].split("/");
            if (!info[2].equalsIgnoreCase("RIZI920")) {
                Intent showMessage = new Intent(context, ShowRecievedTextSms.class);
                showMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                showMessage.putExtra("from12", from);
                showMessage.putExtra("ip", ipProb[1]);
                showMessage.putExtra("port", info[1]);
                showMessage.putExtra("name1", info[2]);
                showMessage.putExtra("phoneNum1", info[3]);
                context.startActivity(showMessage);
                Log.d("ip.........", ipProb[1]);
                Log.d("port.......", info[1]);
            } else {
                Log.d("ip.....", ipProb[1]);
                Log.d("port.....", info[1]);
            }
        }
    }
}
