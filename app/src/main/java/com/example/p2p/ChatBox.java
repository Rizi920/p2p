package com.example.p2p;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Image;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import static com.example.p2p.R.drawable.rounded_corner1;

public class ChatBox extends AppCompatActivity {
   public ListView leftListView,rightListView;
   public LinearLayout rightLayout;
    public LinearLayout leftLayout;
    public ImageButton send;
    public TextView RecievedmsgT,whiteSpaceTxt,newRecieveTxt;
    public TextView rightText;
    public TextView leftText;
    public TextView whiteSpaceTextView;
    public EditText messagetext;
    public ScrollView scrollView;
    AudioStream audioStream;
    AudioGroup audioGroup;
    RecieveFileThread recieveFileThread;
    communicationThread communicationthread1;
    public View v;
    String localip;
    String RecievedfileName;
    Handler handler;
    public String callingport;
    public int fileflag;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);
        File direct = new File(Environment.getExternalStorageDirectory()+"/p2p");
        handler = new Handler();
        fileflag=0;
        if(!direct.exists()) {
            if(direct.mkdir()); //directory is created;
        }
        recieveFileThread = new RecieveFileThread();
        recieveFileThread.start();
        //RecievedmsgT=(TextView)findViewById(R.id.recievedmsgTxtView);
        rightLayout = (LinearLayout) findViewById(R.id.layout4);
        leftLayout = (LinearLayout) findViewById(R.id.layout2);

        send = (ImageButton) findViewById(R.id.send);
        messagetext= (EditText) findViewById(R.id.messageEditText);
        scrollView = (ScrollView) findViewById(R.id.sv);

        communicationthread1 = new communicationThread();
        communicationthread1.start();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messagetext.getText().toString().isEmpty()){
                    return;
                }else {
                    sendMessage(messagetext.getText().toString());

                    rightText = new TextView(ChatBox.this);
                    whiteSpaceTextView = new TextView(ChatBox.this);

                    rightText.setBackgroundResource(R.drawable.rounded_corner1); // to set the layout design and color
                    rightText.setTextColor(Color.BLACK);
                    rightText.setText(messagetext.getText().toString());
                    rightText.setPadding(20, 20, 20, 20);

                    whiteSpaceTextView.setText(" ");
                    scrollView.pageScroll(View.FOCUS_DOWN);

                    rightText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    rightLayout.addView(rightText);
                    rightLayout.addView(whiteSpaceTextView);
                    messagetext.setText("");

                }

            }
        });

    }







    public void sendMessage(String mesg){
        try{

            String msg=mesg;
            BufferedWriter out = new BufferedWriter(new PrintWriter(ClientAndServerThread.socket.getOutputStream()));
            String message = mesg;
            message+="\n";
            out.write(message);
            Log.d("message sent client",msg);
            //out.newLine();
            out.flush();
            //out.close();
        }
        catch (Exception e){
                Log.d("Exception in ","Sending message");
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatboxmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.call){
            Toast.makeText(ChatBox.this, "Dialing..", Toast.LENGTH_SHORT).show();
        Intent i= new Intent(ChatBox.this, VoiceCall.class);
            ChatBox.this.startActivity(i);

            return true;
        }else if(item.getItemId()==R.id.Ftransfer){

            Toast.makeText(ChatBox.this, "choose file", Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(ChatBox.this, FilePickerActivity.class);
            ChatBox.this.startActivity(myIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    class communicationThread extends Thread{
        BufferedReader input;
        String message;




        @Override
        public void run() {
            //while (socket.isConnected()) {

            try {
                input = new BufferedReader(new InputStreamReader(ClientAndServerThread.socket.getInputStream()));
                //while (!input.ready());
                while ((message=input.readLine())!=null) {
                    Log.d("msgggggggggggg recevd",message);
                    handler.post(new updateUi(message));
                   // RecievedmsgT.setText(message);

                }
            } catch (Exception e) {
                e.printStackTrace();
               Log.d("Exception in recieving","message");
            }
        }




    }

    class updateUi implements Runnable
    {
        String msg;
        public updateUi (String message)
        {
            msg = message;
        }

        @Override
        public void run() {

            TextView leftText = new TextView(ChatBox.this);
            TextView whiteTextView = new TextView(ChatBox.this);

            String[] ClickableFileName=msg.split(";");
            if(ClickableFileName[0].equalsIgnoreCase("File Received")){
            leftText.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        fileflag=1;
        final String sentMsg = "downloading file...";
        ChatBox.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(ChatBox.this,
                        sentMsg,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
});
            }
            leftText.setBackgroundResource(R.drawable.rounded_corner); // to set the layout design and color
            leftText.setTextColor(Color.BLACK);
            leftText.setText(msg);
            leftText.setPadding(20, 20, 20, 20);

            whiteTextView.setText(" ");

            leftText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            rightLayout.addView(leftText);
            rightLayout.addView(whiteTextView);
            scrollView.pageScroll(View.FOCUS_DOWN);
            //messagetext.setText("");

        }
    }





    public class RecieveFileThread extends Thread {

        @Override
        public void run() {
            byte[] bytes;
            FileOutputStream fos = null;
            ObjectInputStream ois = null;
            File file = null;

            while (true) {
                try {
                    ois = new ObjectInputStream(ClientAndServerThread.socketftp.getInputStream());
                    DataInputStream d = new DataInputStream(ois);
                    RecievedfileName = d.readUTF();
                    file = new File(
                            Environment.getExternalStorageDirectory() + "/p2p",
                            RecievedfileName);
                    handler.post(new updateUi("File Received; " + "'" + RecievedfileName + "'"));


                } catch (Exception x) {
                    x.printStackTrace();
                }

                while (true) {
                    try {
                        if (fileflag == 1) {
                            bytes = (byte[]) ois.readObject();
                            fos = new FileOutputStream(file);
                            fos.write(bytes);
                            System.out.println("file Received successfully!");
                            final String fsentMsg = "File Received Successfully";
                            ChatBox.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(ChatBox.this,
                                            fsentMsg,
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            fileflag = 0;
                            break;
                        } else {

                        }

                    } catch (Exception x) {
                        System.out.println("file receiving failed");
                    }
                }
            }
        }


    }


}






