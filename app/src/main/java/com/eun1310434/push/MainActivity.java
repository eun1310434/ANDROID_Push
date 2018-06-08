/*==================================================================================================
□ INFORMATION
  ○ Data : Thursday - 07/06/18
  ○ Mail : eun1310434@naver.com
  ○ WebPage : https://eun1310434.github.io/
  ○ Reference
     - Do it android app Programming : Day45 - 1 ~ 7

□ Function
   ○ Process
      - Send
         01) MainActivity : onCreate() -> MainActivity : getRegistrationId()
         02) MainActivity : getRegistrationId() -> MainActivity : printSysLog()
         03) MainActivity : send() -> MainActivity : sendData()
         04) MainActivity : sendData() -> MainActivity : MainActivity.PushResponseListener
         05) MainActivity : MainActivity.PushResponseListener -> MainActivity.PushResponseListener : onRequestStarted()
                                                              -> MainActivity.PushResponseListener : onRequestCompleted()
                                                              -> MainActivity.PushResponseListener : onRequestWithError(VolleyError error)
         06) MainActivity.PushResponseListener : onRequestStarted() -> MainActivity : printSysLog()
         07) MainActivity.PushResponseListener : onRequestCompleted() -> MainActivity : printSysLog()
         08) MainActivity.PushResponseListener : onRequestWithError(VolleyError error) -> MainActivity : printSysLog()

      - Receive
         01) MyFirebaseMessagingService : onMessageReceived() -> MyFirebaseMessagingService : sendToActivity()
         02) MyFirebaseMessagingService : sendToActivity() -> MainActivity : onNewIntent()
         03) MainActivity : onNewIntent() -> MainActivity : processIntent()
         04) MainActivity : processIntent() ->  MainActivity : printSysLog()

   ○ Unit
      - public class MainActivity extends AppCompatActivity
        01) protected void onCreate(Bundle savedInstanceState)
        02) public void getRegistrationId()
        03) public void send(final String input)
        04) public void sendData(JSONObject requestData, final PushResponseListener listener)
        05) protected void onNewIntent(Intent intent)
        06) private void processIntent(Intent intent)
        07) public void println(String _from ,String _data)

      - public interface PushResponseListener
        01) void onRequestStarted() :
        02) void onRequestCompleted() : 정상적인 송수신
        03) void onRequestWithError(VolleyError error) : 에러발생 송수신

      - public class MyFirebaseMessagingService extends FirebaseMessagingService
        01) public void onMessageReceived(RemoteMessage remoteMessage)
        02) private void sendToActivity(Context context, String from, String contents)

      - public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
        01) public void onTokenRefresh()


□ Study
    ○ 메시징 서비스의 구분
         01) 단말기 ↔ 단말기
               : 단말기 간 메시지 송수신(SMS/MMS)
         02) 단말기 ↔ 서버(XMPP/SIP) ↔ 단말기
               : 서버를 통한 단말 간 메시지 송수신
               : XMPP는 카톡과 같은 통신을 위한 포멧
               : SIP는 인터넷 전화와 같은 포멧
         03) 단말기 ↔ SNS(twitter/facebook ) ↔ 단말기
               : 기존에 서버가 있는 SNS를 통한 단말 간 메시지 공유
         04) 단말기 ↔ 구글서버(푸시 서비스, FCM) ↔ 단말기
               : 푸시 서비스를 통한 메시지 전송

    ○푸시 서비스를 활용한 통신
        - 구글의 클라우드 서버를 활용하여 효율적인 푸시 메시지 전송 가능
        - 체제변형 : C2DM -> (상용화, Commercialization) -> GCM -> (Library, Cloud) -> FCM
           01) C2DM : 기존의 상용화 되기 전 제공된 버젼
           02) GCM : 상용화가 되고나서 제공된 버젼
           03) FCM : 좀더 단순한 원리로 제공되어진 버젼, 라이브러리화, 클라우드 시스템 적용
        - 구성
           01) 구글 클라우드 서버에 푸쉬를 받을 단말기(A) , 단말기(B) 등록
           02) 애플리케이션 서버에 단말기(A) , 단말기(B) 의 ID 등록
           03) 단말기(A)가 단말기(B)에 메시지를 전송하고자 애플리케이션 서버에 메시지 전송
           04) 애플리케이션 서버는 단말기(B)에 메시지를 전송하고자 구글 클라우드 서버에 단말기(B)에 메시지를 전송할 것을 요청
           05) 구글 클라우드 서버에서는 등록된 단말기(B)에 메시지 송


=====================================================================*/
package com.eun1310434.push;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView send_log; // 전송 관련 로그
    private TextView receive_log; // 수신 관련 로그
    private EditText msg_et; // 메세지 작성
    private Button msg_btn; // 메세지 버튼
    private String regId; // 등록ID
    private RequestQueue queue; // Volly는 RequestQueue를 사용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send_log = (TextView) findViewById(R.id.send_log);
        send_log.setText("");

        receive_log = (TextView) findViewById(R.id.receive_log);
        receive_log.setText("");

        msg_et = (EditText) findViewById(R.id.msg_et);

        msg_btn = (Button) findViewById(R.id.msg_btn);
        msg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = msg_et.getText().toString().trim();
                send(input);
            }
        });

        // Volly는 RequestQueue를 사용
        queue = Volley.newRequestQueue(getApplicationContext());

        // 등록 ID 정보 발급을 요청
        getRegistrationId();

        // Intent
        //Intent intent = getIntent();
        //processIntent(intent);
    }

    // 등록 ID 정보 발급을 요청
    public void getRegistrationId() {
        regId = FirebaseInstanceId.getInstance().getToken(); //등록ID
        printSysLog(1,"getRegistrationId()","regId : " + regId);
    }

    // 메세지 전송
    public void send(final String input) {
        //데이터 전송 시 Volly를 사용하면 간단하게 전송이 가능하나 현재 이곳에서는 Json으로 설정
        //JSON을 문법을 활용한 데이터 전송

        JSONObject requestData = new JSONObject();

        try {
            //우선순위를 높게 하면 누락되지 않고 데이터 받음
            requestData.put("priority", "high");//우선순위를 high로 설정

            //contents - JSONObject() : msg 입력
            JSONObject dataObj = new JSONObject();
            dataObj.put("contents", input); // 이름 설정
            requestData.put("data", dataObj); // 데이터 설정

            //registration_ids - JSONArray() : 수신자의 ID 입력
            JSONArray idArray = new JSONArray();
            idArray.put(0, regId);
            requestData.put("registration_ids", idArray);

        } catch(Exception e) {
            e.printStackTrace();
        }


        //Volly를 활용하여 전송
        sendData(requestData,
                new PushResponseListener() {
                    @Override
                    public void onRequestStarted() {
                        printSysLog(0,"send : " + input, "PushResponseListener : onRequestStarted()");
                    }

                    @Override
                    public void onRequestCompleted() {
                        printSysLog(0,"send : " + input, "PushResponseListener : onRequestCompleted()");
                    }

                    @Override
                    public void onRequestWithError(VolleyError error) {
                        printSysLog(0,"send : " + input, "PushResponseListener : onRequestWithError()");
                    }
        });
    }

    // 인터페이스 생성
    // 전송한 것을 처리하는 Listener를 생성
    public interface PushResponseListener {
        void onRequestStarted();
        void onRequestCompleted();//정상적인 송수신
        void onRequestWithError(VolleyError error);//에러발생 송수신
    }


    //Volly를 활용하여 전송
    public void sendData(
            JSONObject requestData,// 전송한 것을 처리하는 Listener를 생성
            final PushResponseListener listener) {



        // Volly를 활용하여 요청
        // Volly는 Request라는 전송객체를 만든 다음에 RequestQueue queue를 만들면 알아서 전송하게 됨.
        JsonObjectRequest request = new JsonObjectRequest(
                // 01) 포스트 방식으로 전송
                Request.Method.POST,

                // 02) 기본적인 웹서버 주소
                "https://fcm.googleapis.com/fcm/send",

                // 03) JSON
                requestData,

                // 04) Listener
                //정상적인 송수신시 요청에 대한 응답이 오면 실행
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onRequestCompleted();
                    }
                },

                // 05) 에러발생 송수신
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onRequestWithError(error);
                    }
                }
        ) {
            // JsonObjectRequest
            //InnerMethod를 통한 재정의
            @Override
            public String getBodyContentType() {
                return "application/json"; // json 타입으로 주고 받겠다.
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put(
                        "Authorization",
                        "");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // 웹으로 요청을 할 때 요청 파라미터를 전달
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };

        // 한번 요청한 것을 저장 설정(false) : false를 해놓으면 매번 갱신 됨
        request.setShouldCache(false);

        // 요청이 감.
        listener.onRequestStarted();

        // Queue가 알아서 전송 됨
        queue.add(request);
    }

    // 데이터를 받음
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        printSysLog(1,"onNewIntent(Intent intent)","called");
        processIntent(intent);
    }

    // 데이터를 받음 처리
    private void processIntent(Intent intent) {
        if (intent != null) {
            printSysLog(1, "processIntent(Intent intent)","called");

            String from = intent.getStringExtra("from");
            if (from == null) {
                printSysLog(1,"processIntent(Intent intent)","from is null.");
                return;
            }

            String contents = intent.getStringExtra("contents");
            printSysLog(1,"processIntent(Intent intent)","from/msg : "+from + "/" + contents);
        }

    }

    //화면에 로그 전시
    public void printSysLog(int type, String _from ,String _data) {
        String log = _from +"\n >> "+ _data + "\n\n";
        if(type == 0){ // Send
            send_log.append(log);
        }else if(type == 1){ // Receive
            receive_log.append(log);
        }
    }

}
