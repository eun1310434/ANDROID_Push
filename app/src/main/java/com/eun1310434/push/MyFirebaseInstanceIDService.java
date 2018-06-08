package com.eun1310434.push;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyIID";

    //FCM에 등록 ID가 내부 사정으로 갱신 되었을 때 호출(단말의 ID가 교체)
    @Override
    public void onTokenRefresh() {
        Log.d(TAG, "onTokenRefresh() 호출됨.");

        //새롭게 갱신이 되면 서버로 갱신된 정보(refreshedToken)를 전송해야 됨.
        //현재 서버를 만들지 않고 단말 안에서 송신과 수신을 진행하기에 별도의 진행은 없음.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "Refreshed Token : " + refreshedToken);
    }

}
