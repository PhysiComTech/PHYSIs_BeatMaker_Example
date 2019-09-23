package com.physicomtech.kit.physis_beatmaker_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.physicomtech.kit.physis_beatmaker_app.helper.SoundEffect;
import com.physicomtech.kit.physislibrary.PHYSIsBLEActivity;
import com.physicomtech.kit.physislibrary.ble.BluetoothLEManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetupActivity extends PHYSIsBLEActivity {

    // region Check Bluetooth Permission
    private static final int REQ_APP_PERMISSION = 1000;
    private static final List<String> appPermissions
            = Collections.singletonList(Manifest.permission.ACCESS_COARSE_LOCATION);

    /*
        # 애플리케이션의 정상 동작을 위한 권한 체크
        - 안드로이드 마시멜로우 버전 이상에서는 일부 권한에 대한 사용자의 허용이 필요
        - 권한을 허용하지 않을 경우, 관련 기능의 정상 동작을 보장하지 않음.
        - 권한 정보 URL : https://developer.android.com/guide/topics/security/permissions?hl=ko
        - PHYSIs Maker Kit에서는 블루투스 사용을 위한 위치 권한이 필요.
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> reqPermissions = new ArrayList<>();
            for(String permission : appPermissions){
                if(checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                    reqPermissions.add(permission);
                }
            }
            if(reqPermissions.size() != 0){
                requestPermissions(reqPermissions.toArray(new String[reqPermissions.size()]), REQ_APP_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQ_APP_PERMISSION){
            boolean accessStatus = true;
            for(int grantResult : grantResults){
                if(grantResult == PackageManager.PERMISSION_DENIED)
                    accessStatus = false;
            }
            if(!accessStatus){
                Toast.makeText(getApplicationContext(), "위치 권한 거부로 인해 애플리케이션을 종료합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    // endregion

    private final String SERIAL_NUMBER = "XXXXXXXXXXXX";    // PHYSIs Maker Kit 시리얼번호

    Button btnConnect, btnDisconnect;                       // 액티비티 위젯
    Spinner spBeat1, spBeat2, spBeat3, spBeat4;
    ProgressBar pgbConnect;

    private int TOUCH1_SOUND = 0;                           // 터치 센서별 효과음 인덱스
    private int TOUCH2_SOUND = 0;
    private int TOUCH3_SOUND = 0;
    private int TOUCH4_SOUND = 0;

    private SoundEffect soundEffect = null;                 // 효과음 출력 클래스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        if(!BluetoothLEManager.getInstance(getApplicationContext()).getEnable()){
            Toast.makeText(getApplicationContext(), "블루투스 활성화 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
        }

        checkPermissions();                 // 앱 권한 체크 함수 호출
        setSoundList();                     // 출력 사운드 설정 함수 호출
        initWidget();                       // 위젯 생성 및 초기화 함수 호출
        setEventListener();                 // 이벤트 리스너 설정 함수 호출
    }

    /*
        # 출력 사운드 설정
     */
    private void setSoundList() {
        soundEffect = new SoundEffect(getApplicationContext());       // SoundEffect 객체 생성

        soundEffect.addItem("음계-도", R.raw.scale_c1);        // 효과음 추가 및 항목 설정
        soundEffect.addItem("음계-레", R.raw.scale_d1);
        soundEffect.addItem("음계-미", R.raw.scale_e1);
        soundEffect.addItem("음계-파", R.raw.scale_f1);
        soundEffect.addItem("음계-솔", R.raw.scale_g1);
        soundEffect.addItem("음계-라", R.raw.scale_a2);
        soundEffect.addItem("음계-시", R.raw.scale_b2);
        soundEffect.addItem("동물-강아지", R.raw.dog);
        soundEffect.addItem("동물-염소", R.raw.goat);
        soundEffect.addItem("동물-오리", R.raw.duck);
        soundEffect.addItem("동물-닭", R.raw.chicken);

        soundEffect.createSoundPool();                                  // 설정 효과음에 대한 SoundPool 생성
    }

    /*
        # 위젯 생성 및 초기화
     */
    private void initWidget() {
        btnConnect = findViewById(R.id.btn_connect);                                    // 버튼 생성
        btnDisconnect = findViewById(R.id.btn_disconnect);
        pgbConnect = findViewById(R.id.pgb_connect);                                    // 프로그래스 생성

        spBeat1 = findViewById(R.id.sp_beat_1);                                         // 스피너 생성
        spBeat2 = findViewById(R.id.sp_beat_2);
        spBeat3 = findViewById(R.id.sp_beat_3);
        spBeat4 = findViewById(R.id.sp_beat_4);

        // 설정된 효과음 리스트에 대한 스피너 항목 설정
        ArrayAdapter<String> beatAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_spinner, soundEffect.getSoundItems());
        spBeat1.setAdapter(beatAdapter);
        spBeat2.setAdapter(beatAdapter);
        spBeat3.setAdapter(beatAdapter);
        spBeat4.setAdapter(beatAdapter);
    }

    /*
        # 뷰 (버튼, 스피너) 이벤트 리스너 설정
     */
    private void setEventListener() {
        btnConnect.setOnClickListener(new View.OnClickListener() {                              // 연결 버튼
            @Override
            public void onClick(View v) {                   // 버튼 클릭 시 호출
                btnConnect.setEnabled(false);                       // 연결 버튼 비활성화 설정
                pgbConnect.setVisibility(View.VISIBLE);             // 연결 프로그래스 가시화 설정
                connectDevice(SERIAL_NUMBER);                       // PHYSIs Maker Kit BLE 연결 시도
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {                           // 연결 종료 버튼
            @Override
            public void onClick(View v) {
                disconnectDevice();                                 // PHYSIs Maker Kit BLE 연결 종료
            }
        });

        spBeat1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {             // 터치 센서1 스피너
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 스피너 항목이 변경(선택)되었을 경우 호출
                // 선택된 항목의 position 값을 해당 터치 센서의 효과음 인덱스로 설정
                TOUCH1_SOUND = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spBeat2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TOUCH2_SOUND = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spBeat3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TOUCH3_SOUND = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spBeat4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TOUCH4_SOUND = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /*
       # BLE 연결 결과 수신
       - 블루투스 연결에 따른 결과를 전달받을 때 호출 (BLE 연결 상태가 변경됐을 경우)
       - 연결 결과 : CONNECTED(연결 성공), DISCONNECTED(연결 종료/실패), NO_DISCOVERY(디바이스 X)
     */
    @Override
    protected void onBLEConnectedStatus(int result) {
        super.onBLEConnectedStatus(result);
        setConnectedResult(result);                             // BLE 연결 결과 처리 함수 호출
    }

    /*
        # BLE 연결 결과 처리
     */
    private void setConnectedResult(int result){
        pgbConnect.setVisibility(View.INVISIBLE);               // 연결 프로그래스 비가시화 설정
        boolean isConnected = result == CONNECTED;              // 연결 결과 확인

        String toastMsg;                                        // 연결 결과에 따른 Toast 메시지 출력
        if(result == CONNECTED){
            toastMsg = "Physi Kit와 연결되었습니다.";
        }else if(result == DISCONNECTED){
            toastMsg = "Physi Kit 연결이 실패/종료되었습니다.";
        }else{
            toastMsg = "연결할 Physi Kit가 존재하지 않습니다.";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();

        btnConnect.setEnabled(!isConnected);                     // 연결 버튼 활성화 상태 설정
        btnDisconnect.setEnabled(isConnected);
    }


    /*
        # BLE 메시지 수신
        - 연결된 PHYSIs Maker Kit로부터 BLE 메시지 수신 시 호출
     */
    @Override
    protected void onBLEReceiveMsg(String msg) {
        super.onBLEReceiveMsg(msg);
        outputSound(msg);                                          // 사운드 출력 함수 호출
    }

    /*
        # 사운드 출력
        - 사운드 출력 메시지 프로토콜에 따른 사운드 출력 ( Data Format : 0 0 0 0 )
        - 수신되는 메시지를 통해 터치 센서의 터치 여부를 판단
        - 1 : 사운드 출력 / 0 : 사운드 출력 X
     */
    private void outputSound(String data){
        if(data.charAt(0) == '1'){
            soundEffect.output(TOUCH1_SOUND);                  // 터치 상태에 따른 효과음 출력
        }
        if(data.charAt(1) == '1'){
            soundEffect.output(TOUCH2_SOUND);
        }
        if(data.charAt(2) == '1'){
            soundEffect.output(TOUCH3_SOUND);
        }
        if(data.charAt(3) == '1'){
            soundEffect.output(TOUCH4_SOUND);
        }
    }
}
