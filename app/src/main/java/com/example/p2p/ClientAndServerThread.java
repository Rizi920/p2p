package com.example.p2p;

import android.content.Context;
import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Rizwan on 5/17/2017.
 */
public class ClientAndServerThread {
    protected ServerSocket serverSocket;
    protected ServerSocket serversocketftp;
    protected ServerSocket serversocketCall;
    public static Socket socketftp;
    public static Socket callSocket;
    public static Socket socket = null;
    public static int connection;


    public void runserver(){
        new Thread(new ServerThread()).start();

    }

    public void runclient(String ip1){
        new Thread(new ClientThread(ip1)).start();
    }



    public class ServerThread extends Thread {

        @Override
        public void run() {
            try {
                Log.d("Server", " Listening....");
                serverSocket = new ServerSocket(33821);
                serversocketftp = new ServerSocket(33822);
                serversocketCall = new ServerSocket(45866);
                callSocket=serversocketCall.accept();
                socketftp= serversocketftp.accept();
                socket = serverSocket.accept();
                callSocket.setKeepAlive(true);
                socket.setKeepAlive(true);
                socketftp.setKeepAlive(true);
                Log.d("Server", " Connected");
                connection = 1;

            } catch (Exception x) {
                Log.d("Server", "Not Connected");
                connection = 0;
            }


        }
    }


    public class ClientThread extends Thread {
        String dstIP;


        public ClientThread(String IP) {
            dstIP = IP;

        }

        @Override
        public void run() {
            if (!dstIP.equalsIgnoreCase("")) {
                try {

                    Log.d("Client", "Started......");
                    socket = new Socket(dstIP, 33821);
                    socketftp= new Socket(dstIP, 33822);
                    callSocket=new Socket(dstIP,45866);
                    socketftp.setKeepAlive(true);
                    socket.setKeepAlive(true);
                    callSocket.setKeepAlive(true);
                    connection = 1;
                    Log.d("Client", "Connected......" + socket.getInetAddress());
                } catch (Exception x) {
                    Log.d("Client", " not Connected......");
                    connection = 0;
                }

            } else {
                Log.d("NoIP", "please check connection");

            }
        }

    }
}