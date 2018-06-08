package com.eun1310434.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


//build.gradle에서 외부 라이브러리를 사용하겠다 설정하였기에 FirebaseMessagingService를 extends 할 수 있음.
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyMS"; //테그가 길면 오류 발생

    //메시지를 받기 위한 서비스
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived() 호출됨.");


        //어디서 보냈는지 확인 가능
        String from = remoteMessage.getFrom();

        //메시지 내용 확인
        Map<String, String> data = remoteMessage.getData();
        String contents = data.get("contents");

        Log.v(TAG, "from : " + from + ", contents : " + contents);

        sendToActivity(getApplicationContext(), from, contents);
    }

    // 받아온 데이터를 intent를 활요하여 MainActivity로 전송
    private void sendToActivity(Context context, String from, String contents) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("contents", contents);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

}
