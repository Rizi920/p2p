package com.example.p2p;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CallreceiveActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */

    AudioStream audioStream;
    AudioGroup audioGroup;
    String localip;
    public static String callingPort;
    Handler handler;
    String friendPort;
    Button AcceptCallbtn;
    Button RejectCallbtn;
    public int callGoingOn=0;

    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callreceive);


        RejectCallbtn=(Button)findViewById(R.id.rejectcallB);
        AcceptCallbtn=(Button)findViewById(R.id.AcceptcallB);


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        handler = new Handler();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        int line = 0;
        try {

            AudioManager audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);

            if (audiomanager.isMicrophoneMute()) {
                Toast.makeText(CallreceiveActivity.this, "Microphone is mute", Toast.LENGTH_LONG).show();
            }

            audioGroup = new AudioGroup();
            audioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);
            localip = getLocalAddress().toString();


            audioStream = new AudioStream(getLocalAddress());
            callingPort=String.valueOf(audioStream.getLocalPort());
            audioStream.setCodec(AudioCodec.PCMA);
            audioStream.setMode(RtpStream.MODE_NORMAL);

            //Toast.makeText(MainActivity.this, "line = 10", Toast.LENGTH_SHORT).show();
        } catch (SocketException se) {
            Toast.makeText(CallreceiveActivity.this, se.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(CallreceiveActivity.this, "line = " + line + e.getMessage() + "\n" + localip, Toast.LENGTH_LONG).show();
            Log.e("-------", e.toString());
            e.printStackTrace();
        }
        Intent in = getIntent();
        friendPort=in.getStringExtra("Calling");


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////






        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.AcceptcallB).setOnTouchListener(mDelayHideTouchListener);
       // Intent i= getIntent();
        //String portOfCall=i.getStringExtra("from12");
        //Toast.makeText(CallreceiveActivity.this, portOfCall, Toast.LENGTH_SHORT).show();
           // ChatBox.call(RecieveTextSms.ipProb[1], portOfCall);



        RejectCallbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(callGoingOn==0) {
                   sendMessage("CallRejected");
                   audioGroup.clear();
                   audioStream.release();
                   finish();
                   onBackPressed();
               }else{
                   final String sentMsg = "Call Ended.";
                   CallreceiveActivity.this.runOnUiThread(new Runnable() {

                       @Override
                       public void run() {
                           Toast.makeText(CallreceiveActivity.this,
                                   sentMsg,
                                   Toast.LENGTH_LONG).show();
                       }
                   });
                   audioGroup.clear();
                   audioStream.release();

                   finish();
                   onBackPressed();
               }
            }
        });



        AcceptCallbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(callingPort);
                call(RecieveTextSms.ipProb[1],friendPort);

            }
        });
    }



    public void sendMessage(String mesg){
        try{

            String msg=mesg;
            BufferedWriter out = new BufferedWriter(new PrintWriter(ClientAndServerThread.callSocket.getOutputStream()));
            String message = mesg;
            message+="\n";
            out.write(message);
            Log.d("message sent for call",msg);
            //out.newLine();
            out.flush();
            //out.close();
        }
        catch (Exception e){
            Log.d("Exception in ","Sending message");
        }
    }

    public void call(String callip, String callport)
    {
        try {
            //Toast.makeText(MainActivity.this, remote_ip_editText.getText().toString(), Toast.LENGTH_SHORT).show();
            InetAddress remoteIp = InetAddress.getByName(callip);
            Integer remotePort= Integer.parseInt(callport);
            if(remoteIp == null)
                Toast.makeText(CallreceiveActivity.this, "remoteIp is null", Toast.LENGTH_SHORT).show();
            if(audioStream==null)
                Toast.makeText(CallreceiveActivity.this, "audioStream is null", Toast.LENGTH_SHORT).show();
            if(remotePort == null)
                Toast.makeText(CallreceiveActivity.this, "remotePort is null", Toast.LENGTH_SHORT).show();
            if(audioGroup== null)
                Toast.makeText(CallreceiveActivity.this, "audioGroup is null", Toast.LENGTH_SHORT).show();
            audioStream.associate(remoteIp, remotePort);
            audioStream.join(audioGroup);
            callGoingOn=1;
            Toast.makeText(CallreceiveActivity.this, "call Success", Toast.LENGTH_SHORT).show();
        }
        catch (UnknownHostException ex){
            Toast.makeText(CallreceiveActivity.this, "Unkown host Exception "+ ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(CallreceiveActivity.this, "Exception "+ e, Toast.LENGTH_SHORT).show();
        }

    }




    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
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
            Toast.makeText(CallreceiveActivity.this, "Exception at get local Address"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }



}
