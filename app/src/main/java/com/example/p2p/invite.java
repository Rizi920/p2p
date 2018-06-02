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

public class invite extends AppCompatActivity {
    TextView nameT;
    TextView NumT;
    public static String personalIp;
    TextView progress;
    Button connectB;
    String phonenum;
    String name;
    Button chat;
    public static InetAddress localip1;;
    private String localip;
    private static final String SMS_DELIVERED = "SMS_DELIVERED";
    private static final String SMS_SENT = "SMS_SENT";
    private static final int MAX_SMS_MESSAGE_LENGTH = 160;
    private static final int SMS_PORT = 8901;
    private static final int Connection_port = 2222;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        localip1=getLocalAddress();
        personalIp=getLocalAddress().toString();
        Bundle in = getIntent().getExtras();
        //Long id=in.getString("id", 0);
        name= in.getString("Name");
        phonenum= in.getString("No");
        nameT = (TextView) findViewById(R.id.nameTextView);
        NumT = (TextView) findViewById(R.id.phoneNumTextview);

        nameT.setText("Name: "+name);
        NumT.setText("Contact number: "+phonenum);

        try{
            localip= getLocalAddress().toString();
        }catch(Exception x){
            // Toast.makeText(Main2Activity.this, "Exeption in getting ip", Toast.LENGTH_SHORT).show();
        }
        connectB = (Button) findViewById(R.id.connect);
        chat=(Button) findViewById(R.id.chatB);
        chat.setVisibility(View.INVISIBLE);
        connectB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ClientAndServerThread obj=new ClientAndServerThread();
                obj.runserver();
                phonenum.replaceAll(" ","");
                SendSms(phonenum,localip+";"+SMS_PORT+";"+name+";"+phonenum);
                chat.setVisibility(View.VISIBLE);


            }

        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClientAndServerThread.connection==1){
                    Intent n = new Intent(invite.this, ChatBox.class);
                    startActivity(n);
                }else{
                    final String senttMsg = "waiting for his/her response. please wait..." ;
                    invite.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(invite.this,
                                    senttMsg,
                                    Toast.LENGTH_LONG).show();
                        }});
                }
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
        final String sentMsg = "Invite SMS Sent!" ;
        invite.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(invite.this,
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
            Toast.makeText(invite.this, "Exception at get local Address "+e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
        return null;
    }
}
