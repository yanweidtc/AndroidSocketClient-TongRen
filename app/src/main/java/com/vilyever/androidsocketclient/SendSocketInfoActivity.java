package com.vilyever.androidsocketclient;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Administrator on 2017/7/24.
 */

public class SendSocketInfoActivity extends Activity {
    public static String TAG = "SendSocketInfoActivity";
    private static String IpAddress = "192.168.36.108";
    private static int Port = 20001;
    private static Socket socket = null;

    Button bt_patient = null;
    Button bt_voice = null;
    Button bt_notify = null;

    EditText et_ip = null;
    EditText et_port = null;

    private static String msginfo  = "msg=dataupdate&areaid=queueinfo&areatype=queue&dataid=38251&data=<br/><br/><br/><br/><br/><br/><br/>刘海超<br/>黄谦<br/>&areaid=windowinfo&areatype=queue&dataid=38251&data=<br/>03诊室";
    private static String msginfo1 = "msg=dataupdate&areaid=notify&areatype=queue&dataid=38251&data=请到01诊室就诊&areaid=l&areatype=lua&dataid=38251&action=addnew&data=<areadata areaid='l' areatype='lua'><script language='lua'>VoiceContent=\"\"请到01诊室就诊\"\";PatInfo=\"\"36号 伊文荟\"\";</script></areadata>\n";
    private static String msginfo2 = "msg=callnumber&count=2&url=/vis/voice/1589887ID192168185175.wav&session_id=19216818517538251";
    private static String msginfo4 = "msg=dataupdate&areaid=queueinfo&areatype=queue&dataid=38251&data=<br/><br/><br/><br/><br/><br/><br/>阎巍<br/>黄谦<br/>&areaid=windowinfo&areatype=queue&dataid=38251&data=<br/>03诊室";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        bt_patient = (Button) findViewById(R.id.bt_patient);
        bt_voice = (Button) findViewById(R.id.bt_voice);
        bt_notify = (Button) findViewById(R.id.bt_notify);

        et_ip = (EditText) findViewById(R.id.edit_ip);
        et_port = (EditText) findViewById(R.id.edit_port);

        setListener();
    }

    private void setListener(){
        bt_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        sendMsg(msginfo);
                        super.run();
                    }
                }.start();
            }
        });

        bt_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        sendMsg(msginfo2);
                        super.run();
                    }
                }.start();
            }
        });

        bt_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        sendMsg(msginfo1);
                        super.run();
                    }
                }.start();
            }
        });
    }

    private void sendMsg(String msg){
        try {
            socket = new Socket(IpAddress,Port);
            PrintWriter out =  new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),true);
            out.println(msg);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startThread(){
        new Thread(){
            @Override
            public void run() {
                super.run();
            }
        }.start();
    }

}
