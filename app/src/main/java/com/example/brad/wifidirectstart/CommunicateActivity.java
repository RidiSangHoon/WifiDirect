package com.example.brad.wifidirectstart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.brad.wifidirectstart.DeviceDetailFragment.info;

public class CommunicateActivity extends Activity {

    public static final String TAG = "CommunicateActivity";

    private Button toastBtn,sendBtn;
    private EditText inputText;
    private TextView showText;
    private LinearLayout clientRel, ownerRel;
    private PrintWriter socketWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);

        ownerRel = (LinearLayout)findViewById(R.id.ownerRel);
        clientRel = (LinearLayout)findViewById(R.id.clientRel);

        boolean isOwner = getIntent().getBooleanExtra("owner",false);
        if(isOwner) {
            //그룹 오너
            Log.e("CommunicateActivity","Group Owner");
            showText = (TextView)findViewById(R.id.showText);
            new ServerAsyncTask(CommunicateActivity.this,showText).execute();

            ownerRel.setVisibility(View.VISIBLE);
            clientRel.setVisibility(View.GONE);
        } else {
            //그룹 클라이언트
            Log.e("CommunicateActivity","Group Client");
            sendBtn = (Button)findViewById(R.id.sendBtn);
            toastBtn = (Button)findViewById(R.id.toastBtn);
            inputText = (EditText)findViewById(R.id.inputText);

            ownerRel.setVisibility(View.GONE);
            clientRel.setVisibility(View.VISIBLE);

            toastBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent serviceIntent = new Intent(CommunicateActivity.this, CommunicationService.class);
                    serviceIntent.setAction(CommunicationService.ACTION_TOAST);
                    serviceIntent.putExtra(CommunicationService.EXTRAS_GROUP_OWNER_ADDRESS,
                            info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(CommunicationService.EXTRAS_GROUP_OWNER_PORT, 8988);
                    getApplicationContext().startService(serviceIntent);
                }
            });
        }
    }


    public class ServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public ServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(CommunicateActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(CommunicateActivity.TAG, "Server: connection done");
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                Log.e("CommunicateActivity",inputReader.readLine());
                final String printString = inputReader.readLine();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CommunicateActivity.this, printString,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return "";
            } catch (IOException e) {
                Log.e(CommunicateActivity.TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
            }

        }
    }
}
