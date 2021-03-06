// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.brad.wifidirectstart;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class CommunicationService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_MSG = "com.example.android.wifidirect.SEND_MSG";
    public static final String ACTION_TOAST = "com.example.android.wifidirect.TOAST";

    public static final String EXTRAS_MSG = "Message";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    private PrintWriter communicationWriter;

    public CommunicationService(String name) {
        super(name);
    }

    public CommunicationService() {
        super("CommunicationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction().equals(ACTION_SEND_MSG) || intent.getAction().equals(ACTION_TOAST)) {
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            String msg = intent.getExtras().getString(EXTRAS_MSG);

            Log.e("CommunicationService", "msg => " + msg);
            try {
                Log.d(CommunicateActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                communicationWriter = new PrintWriter(socket.getOutputStream());
                communicationWriter.println(intent.getAction() + "/" + msg);
                communicationWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
