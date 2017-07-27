package com.vilyever.androidsocketclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vilyever.androidsocketclient.entity.NotifyInfo;
import com.vilyever.androidsocketclient.entity.PatientQueue;
import com.vilyever.androidsocketclient.entity.VoiceQueue;
import com.vilyever.logger.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Administrator on 2017/7/18.
 */

public class TestDemoActivity extends Activity{
    public static  String TAG = "TESTDEMO";
    public static  int Port = 20001;
    public static  final String CODE_FORMAT = "utf-8";
    public   int   InfoType = -1;
    private static final int     ERROR_TYPE              =     -1;
    private static final int     VOICE_TYPE              =      0;
    private static final int     PATIENT_TYPE            =      1;
    private static final int     NOTIFY_TYPE             =      2;

    public  static boolean       isLoadingProgram        =      true;
    public  static boolean       isLoadingNotify         =      true;
    public  static boolean       isLoadingVoice          =      true;

    public static ServerSocket serversocket = null;
    public static TestDemoActivity Instance = null;

    public static DatagramSocket socket = null;
    private static String msginfo  = "msg=dataupdate&areaid=queueinfo&areatype=queue&dataid=38251&data=<br/><br/><br/><br/><br/><br/><br/>刘海超<br/>黄谦<br/>&areaid=windowinfo&areatype=queue&dataid=38251&data=<br/>03诊室";
    private static String msginfo1 = "msg=dataupdate&areaid=notify&areatype=queue&dataid=38251&data=请到01诊室就诊&areaid=l&areatype=lua&dataid=38251&action=addnew&data=<areadata areaid='l' areatype='lua'><script language='lua'>VoiceContent=\"\"请到01诊室就诊\"\";PatInfo=\"\"36号 伊文荟\"\";</script></areadata>\n";
    private static String msginfo2 = "msg=callnumber&count=2&url=/vis/voice/1589887ID192168185175.wav&session_id=19216818517538251";
    private HashMap<String,String> infoHashMap = null;

    public ArrayList<PatientQueue> patientQueueArrayList = new ArrayList<>();
    public ArrayList<VoiceQueue>   voiceQueueArrayList   = new ArrayList<>();
    public ArrayList<NotifyInfo>   notifyInfoArrayList   = new ArrayList<>();

    // Define message Id
    private static final int     EVENT_CHANGE_PATIENT    =         0x8001;
    private static final int     EVENT_CHANGE_NOTIFY     =         0x8002;
    private static final int     EVENT_CHANGE_VOICE      =         0x8003;

    public TextView tv_mysocketinfo = null;
    public TextView tv_mysocketinfo1 = null;
    public TextView tv_mysocketinfo2 = null;

    public Button bt_action = null;
    public Button bt_action1 = null;
    public Button bt_action2 = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Log.d("TestDemoActivityCreate=","createSocketServer");
        Instance = this;

        ServerThread serverThread = new ServerThread();
        serverThread.start();

        LoadInfoThread loadInfoThread = new LoadInfoThread();
        loadInfoThread.start();

        LoadInfo1Thread loadInfo1Thread = new LoadInfo1Thread();
        loadInfo1Thread.start();

        LoadInfo2Thread loadInfo2Thread = new LoadInfo2Thread();
        loadInfo2Thread.start();

        tv_mysocketinfo = (TextView) findViewById(R.id.tv_info);
        tv_mysocketinfo1 = (TextView) findViewById(R.id.tv_info1);
        tv_mysocketinfo2 =  (TextView) findViewById(R.id.tv_info2);

        bt_action = (Button) findViewById(R.id.bt_press);
        bt_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoadingProgram = false;
            }
        });

        bt_action1 = (Button) findViewById(R.id.bt_press1);
        bt_action1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoadingNotify = false;
            }
        });
        bt_action2 = (Button) findViewById(R.id.bt_press2);
        bt_action2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoadingVoice = false;
            }
        });
    }

    private void ConvertMsg(String info){
        String[] rawInfo = info.split("&");
        for(int i = 0; i < rawInfo.length;i++){
            Log.d(TAG,rawInfo[i]);
        }
    }

    //将socket传输的数据转换为HashMap格式
    private HashMap<String,String> ConvertRaw2HashMap(String[] raw){
        HashMap<String,String>  hmInfo = new HashMap<>();
        for(int i = 0; i < raw.length; i++){
            String[] convertInfo = raw[i].split("=");
            //判断Key值是否已经存在
            if(hmInfo.containsKey(convertInfo[0])){
                hmInfo.put(convertInfo[0]+1,convertInfo[1]);
            }else {
                hmInfo.put(convertInfo[0],convertInfo[1]);
            }
        }
        return hmInfo;
    }



    private int getDataType(String info){
        String[] rawInfo = info.split("&");
        int rawLength = rawInfo.length;
        if (rawLength == 4){
            return VOICE_TYPE;
        }else if (rawLength == 9){
            return PATIENT_TYPE;
        }else if (rawLength == 10){
            return NOTIFY_TYPE;
        }else {
            return ERROR_TYPE;
        }
    }

    private void ConvertNotifyInfoQueue(String info){
        String[] rawInfo = info.split("&");
        infoHashMap = new HashMap<>();
        NotifyInfo notifyInfo = new NotifyInfo();

        infoHashMap = ConvertRaw2HashMap(rawInfo);

        notifyInfo.msg = infoHashMap.get("msg");
        notifyInfo.areaId = infoHashMap.get("areaid");
        notifyInfo.areaType = infoHashMap.get("areatype");
        notifyInfo.dataId = infoHashMap.get("dataid");
        notifyInfo.notifyData = infoHashMap.get("data");
        notifyInfo.areaId1 = infoHashMap.get("areaid1");
        notifyInfo.areaType1 = infoHashMap.get("areatype1");
        notifyInfo.dataId1 = infoHashMap.get("dataid1");
        notifyInfo.action = infoHashMap.get("action");
        notifyInfo.notifyData1 = infoHashMap.get("data1");

        if (notifyInfo != null){
            infoHashMap = null;
        }

        notifyInfoArrayList.add(notifyInfo);

    }

    private void ConvertVoiceQueue(String info){
        String[] rawInfo = info.split("&");
        infoHashMap = new HashMap<>();
        VoiceQueue voiceQueue = new VoiceQueue();

        infoHashMap = ConvertRaw2HashMap(rawInfo);

        //HashMap 转成VoiceQueue实体类
        voiceQueue.msg = infoHashMap.get("msg");
        voiceQueue.count = infoHashMap.get("count");
        voiceQueue.url = infoHashMap.get("url");
        voiceQueue.session_id = infoHashMap.get("session_id");

        if(voiceQueue != null){
            Log.d(TAG,infoHashMap.size()+"HashMap size");
            Log.d(TAG,voiceQueue.url+"");
            infoHashMap = null;
        }
        voiceQueueArrayList.add(voiceQueue);
    }



    private void ConvertPatientQueueMsg(String info){
        String[] rawInfo = info.split("&");
        infoHashMap = new HashMap<>();
        PatientQueue patientQueue = new PatientQueue();

        infoHashMap = ConvertRaw2HashMap(rawInfo);

        //HashMap 转成PatientQueue实体类
        patientQueue.msg = infoHashMap.get("msg");
        patientQueue.areaid = infoHashMap.get("areaid");
        patientQueue.areatype = infoHashMap.get("areatype");
        patientQueue.dataid = infoHashMap.get("dataid");
        patientQueue.data = infoHashMap.get("data");
        patientQueue.areaid1 = infoHashMap.get("areaid1");
        patientQueue.areatype1 = infoHashMap.get("areatype1");
        patientQueue.dataid1 = infoHashMap.get("dataid1");
        patientQueue.data1 = infoHashMap.get("data1");

        if(patientQueue != null){
            Log.d(TAG,infoHashMap.size()+"HashMap size");
            Log.d(TAG,patientQueue.msg+"");
            infoHashMap = null;
        }
        patientQueueArrayList.add(patientQueue);
    }

    private int getMsgLength(String info){
        String[] rawInfo = info.split("&");
        return rawInfo.length;
    }

    public class ServerThread extends Thread{
         private int count = 0;
         @Override
         public void run() {

             try{
                 Log.d(TAG,"Create ServerThread");
                 socket = new DatagramSocket(Port);

                 serversocket = new ServerSocket(Port);
                 while (true){
                     Log.d(TAG,"Running count"+count++);

                     byte data[] = new byte[4*1024];
                     DatagramPacket packet = new DatagramPacket(data,data.length);
                     socket.receive(packet);
                     String msg = new String (packet.getData(),packet.getOffset(),packet.getLength(),"gb2312");
                     //msg = new String(packet.getData(),"utf-8");
                     //DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.getData(),packet.getOffset(),packet.getLength()));
                     //String msg = inputStream.readUTF();
                     Log.d("George",""+msg);
/*                     Socket socket = serversocket.accept();
                     BufferedReader buffer = new BufferedReader(
                             new InputStreamReader(socket.getInputStream())
                     );
                     String msg = buffer.readLine();
                     Log.d("TextDemo", "my msg"+msg);*/
                     ConvertMsg(msg);
                     Log.d(TAG," Get Message Length"+getMsgLength(msg));
                     InfoType = getDataType(msg);
                     switch (InfoType){
                         case VOICE_TYPE:
                             Log.d(TAG,"This type RawInfo from Socket code" + VOICE_TYPE + "VOICE_TYPE");
                             ConvertVoiceQueue(msg);
                             break;
                         case PATIENT_TYPE:
                             Log.d(TAG,"This type RawInfo from Socket code" + PATIENT_TYPE + "PATIENT_TYPE");
                             ConvertPatientQueueMsg(msg);
                             break;
                         case NOTIFY_TYPE:
                             Log.d(TAG,"This type RawInfo from Socket code" + NOTIFY_TYPE + "NOTIFY_TYPE");
                             ConvertNotifyInfoQueue(msg);
                             break;
                         case ERROR_TYPE:
                             Log.d(TAG,"This type RawInfo from Socket is error code" + ERROR_TYPE);
                             continue;
                     }
                     //ConvertPatientQueueMsg(msg);
                 }

             }catch (IOException e){
                 e.printStackTrace();
             }
         }
     }

     public class LoadInfoThread extends Thread{

         @Override
         public void run() {
             while (true){
                 try {
                     if (isLoadingProgram) {
                         Log.d(TAG,"is Loading Program ; wait for the socket info");
                         Thread.sleep(1000);
                         continue;
                     }
                     if (patientQueueArrayList.size()> 0){
                         isLoadingProgram =true;
                         mHandler.sendEmptyMessage(EVENT_CHANGE_PATIENT);
                     }else {
                         Log.d(TAG,"All list is null ; wait for the socket info");
                         Thread.sleep(1000*1);
                         isLoadingProgram = true;
                         continue;
                     }

                 }catch (Exception e){
                     e.printStackTrace();
                 }

             }
         }
     }

    public class LoadInfo1Thread extends Thread{

        @Override
        public void run() {
            while (true){
                try {
                    if (isLoadingNotify) {
                        Log.d(TAG,"is Loading Notify ; wait for the socket info");
                        Thread.sleep(1000);
                        continue;
                    }
                    if (notifyInfoArrayList.size()> 0){
                        isLoadingNotify =true;
                        mHandler.sendEmptyMessage(EVENT_CHANGE_NOTIFY);;
                    }else {
                        Log.d(TAG,"Notify list is null ; wait for the socket info");
                        Thread.sleep(1000*1);
                        isLoadingNotify = true;
                        continue;
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    public class LoadInfo2Thread extends Thread{

        @Override
        public void run() {
            while (true){
                try {
                    if (isLoadingVoice) {
                        Log.d(TAG,"is Loading Voice ; wait for the socket info");
                        Thread.sleep(1000);
                        continue;
                    }
                    if (voiceQueueArrayList.size()> 0){
                        isLoadingVoice =true;
                        mHandler.sendEmptyMessage(EVENT_CHANGE_VOICE);
                    }else {
                        Log.d(TAG,"Voice list is null ; wait for the socket info");
                        Thread.sleep(1000*1);
                        isLoadingVoice = true;
                        continue;
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

     @SuppressLint("HandlerLeak")
    final  Handler mHandler = new Handler(){
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what){
                 case EVENT_CHANGE_PATIENT:
                     Log.d(TAG,"GET CURRENT PATIENT LIST SIZE"+ patientQueueArrayList.size());
                     tv_mysocketinfo.setText(Html.fromHtml(patientQueueArrayList.get(0).data));
                     patientQueueArrayList.remove(0);
                     //isLoadingProgram = false;
                     break;
                 case EVENT_CHANGE_NOTIFY:
                     Log.d(TAG, "Get Current Notify List Size"+ notifyInfoArrayList.size());
                     notifyInfoArrayList.remove(0);
                     tv_mysocketinfo1.setText(notifyInfoArrayList.get(0).notifyData);
                     break;
                 case EVENT_CHANGE_VOICE:
                     Log.d(TAG, "Get Current Voice List Size" + voiceQueueArrayList.size());
                     tv_mysocketinfo2.setText(voiceQueueArrayList.get(0).url);
                     voiceQueueArrayList.remove(0);
                     break;
                 default:
                     break;
             }
             super.handleMessage(msg);
         }
     };

}
