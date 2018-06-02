package com.example.p2p;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class ShowRecievedTextSms extends AppCompatActivity {
    public static String localip;
    private static final String SMS_DELIVERED = "SMS_DELIVERED";
    private static final String SMS_SENT = "SMS_SENT";
    private static final int MAX_SMS_MESSAGE_LENGTH = 160;
    private static final int SMS_PORT = 8902;
    private static final int Connection_port = 2222;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recieved_text_sms);


        Intent in = getIntent();
        final String from=in.getStringExtra("from12");
        final String MessageIp= in.getStringExtra("ip");
        String MessagePort= in.getStringExtra("port");
        final String Name = in.getStringExtra("name1");
        final String phoneNum= in.getStringExtra("phoneNum1");
        TextView t=(TextView)findViewById(R.id.invitation);
        t.setText("<<<:::::Message:::::>>>\n"+MessageIp+"\n"+MessagePort+"\n"+Name+"\n"+phoneNum);
        TextView t2=(TextView)findViewById(R.id.from);
        t2.setText("From: "+from);
        try{
            localip= getLocalAddress().toString();
        }catch(Exception x){
            // Toast.makeText(Main2Activity.this, "Exeption in getting ip", Toast.LENGTH_SHORT).show();
        }
        Button b=(Button)findViewById(R.id.accept);
        final Button b1=(Button)findViewById(R.id.chatB);
        b1.setVisibility(View.INVISIBLE);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("ipppppppppppp",MessageIp);
                MessageIp.replaceAll(" ","");
                ClientAndServerThread obj = new ClientAndServerThread();
                obj.runclient(MessageIp);
                b1.setVisibility(View.VISIBLE);
                SendSms(from,localip+";"+SMS_PORT+";"+"RIZI920"+";"+from);
                b1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent n = new Intent(ShowRecievedTextSms.this, ChatBox.class);
                        startActivity(n);



                    }


                });



            }

        });
    }


    private void SendSms(String phonenumber,String message) {


        SmsManager manager = SmsManager.getDefault();
        PendingIntent piSend = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

        byte[] data = new byte[message.length()];

        for(int index=0; index<message.length() && index < MAX_SMS_MESSAGE_LENGTH; ++index)
        {
            data[index] = (byte)message.charAt(index);
        }

        manager.sendDataMessage(phonenumber, null, (short) SMS_PORT, data,piSend, piDelivered);
        final String sentMsg = "Invite SMS Replied!" ;
        ShowRecievedTextSms.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(ShowRecievedTextSms.this,
                        sentMsg,
                        Toast.LENGTH_LONG).show();
            }});


    }

    public InetAddress getLocalAddress()
    {
        InetAddress localAddress;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress ip = enumIpAddr.nextElement();
                    if (!ip.isLoopbackAddress()) {
                        if (ip.isSiteLocalAddress()) {
                            return ip;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(ShowRecievedTextSms.this, "Exception at get local Address "+e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
        return null;
    }
}
