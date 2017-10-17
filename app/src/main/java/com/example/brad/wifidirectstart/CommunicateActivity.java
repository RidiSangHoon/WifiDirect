package com.example.brad.wifidirectstart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.brad.wifidirectstart.DeviceDetailFragment.info;

public class CommunicateActivity extends Activity {

    public static final String TAG = "CommunicateActivity";

    private Button toastBtn, sendBtn;
    private EditText inputText;
    private TextView showText;
    private LinearLayout clientRel, ownerRel;
    private ServerThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);

        ownerRel = (LinearLayout) findViewById(R.id.ownerRel);
        clientRel = (LinearLayout) findViewById(R.id.clientRel);
        serverThread = new ServerThread();

        boolean isOwner = getIntent().getBooleanExtra("owner", false);
        if (isOwner) {
            //그룹 오너
            Log.e("CommunicateActivity", "Group Owner");
            showText = (TextView) findViewById(R.id.showText);
            serverThread.start();
            ownerRel.setVisibility(View.VISIBLE);
            clientRel.setVisibility(View.GONE);
        } else {
            //그룹 클라이언트
            Log.e("CommunicateActivity", "Group Client");
            sendBtn = (Button) findViewById(R.id.sendBtn);
            toastBtn = (Button) findViewById(R.id.toastBtn);
            inputText = (EditText) findViewById(R.id.inputText);

            ownerRel.setVisibility(View.GONE);
            clientRel.setVisibility(View.VISIBLE);

            toastBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent serviceIntent =
                            new Intent(CommunicateActivity.this, CommunicationService.class);
                    serviceIntent.putExtra(CommunicationService.EXTRAS_GROUP_OWNER_ADDRESS,
                            info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(CommunicationService.EXTRAS_MSG,
                            inputText.getText().toString());
                    serviceIntent.putExtra(CommunicationService.EXTRAS_GROUP_OWNER_PORT, 8988);
                    serviceIntent.setAction(CommunicationService.ACTION_TOAST);
                    getApplicationContext().startService(serviceIntent);
                    inputText.setText("");
                }
            });

            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent serviceIntent =
                            new Intent(CommunicateActivity.this, CommunicationService.class);
                    serviceIntent.putExtra(CommunicationService.EXTRAS_GROUP_OWNER_ADDRESS,
                            info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(CommunicationService.EXTRAS_MSG,
                            inputText.getText().toString());
                    serviceIntent.putExtra(CommunicationService.EXTRAS_GROUP_OWNER_PORT, 8988);
                    serviceIntent.setAction(CommunicationService.ACTION_SEND_MSG);
                    getApplicationContext().startService(serviceIntent);
                    inputText.setText("");
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serverThread.interrupt();
    }

    public class ServerThread extends Thread {
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(CommunicateActivity.TAG, "Server: Socket opened");
                for (; ; ) {
                    Thread.sleep(300);
                    Socket client = serverSocket.accept();
                    Log.d(CommunicateActivity.TAG, "Server: connection done");
                    BufferedReader inputReader =
                            new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String msg = inputReader.readLine();
                    if (msg != null) {
                        if (msg.startsWith(CommunicationService.ACTION_TOAST)) {
                            final String toastMsg =
                                    msg.replace(CommunicationService.ACTION_TOAST + "/", "");
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CommunicateActivity.this, toastMsg,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            final String sendMsg =
                                    msg.replace(CommunicationService.ACTION_SEND_MSG + "/", "");
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    showText.setText(
                                            showText.getText().toString() + "\n" + sendMsg);
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(CommunicateActivity.TAG, e.getMessage());
            }
        }
    }
}
