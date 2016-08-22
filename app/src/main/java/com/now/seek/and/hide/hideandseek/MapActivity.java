package com.now.seek.and.hide.hideandseek;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.games.Games;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import util.MapUtils;

public class MapActivity extends Activity implements View.OnClickListener{

    public static final double enemySpeed = 0.00003;
    public static final int COUNT_TIME = 15, ZOOM = 20;
    public static final double THR = 10.0;

    public static boolean pause = false, startGame = false;

    private GPSTracker mGPS;
    private LatLng myPos, enemyPos, startPos;
    private TextView _timer, _remain, _run;
    private Button _give;
    private CountDownTimer countDownTimer;
    private GoogleMap map;
    private List<LatLng> myPath, enemyPath;
    private Polyline route, enemyRoute;

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.bt_give:
                gameOver();
                break;
            default: break;
        }
    }

    public void toast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        myPath = new ArrayList<>();
        enemyPath = new ArrayList<>();

        _timer = (TextView)findViewById(R.id.textView);
        _run = (TextView)findViewById(R.id.run);

        _give = (Button)findViewById(R.id.bt_give);
        _give.setOnClickListener(this);

        mGPS = new GPSTracker(this);

        enemyPos = new LatLng(mGPS.getLatLng().latitude, mGPS.getLatLng().longitude);
        myPos = new LatLng(mGPS.getLatLng().latitude, mGPS.getLatLng().longitude);
        startPos = new LatLng(mGPS.getLatLng().latitude, mGPS.getLatLng().longitude);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        _remain = (TextView)findViewById(R.id.remain);
        initMap();

        AdView mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("FE4EB46DF11F124494E4B402287CE845").build();
        mAdView.loadAd(adRequest);

        ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(0, COUNT_TIME);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                _timer.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                countDownTimer = new CountDownTimer(COUNT_TIME * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        _timer.setText(String.valueOf((int) millisUntilFinished / 1000));
                    }

                    @Override
                    public void onFinish() {
                        _timer.setText("START");
                        _timer.animate().alpha(0.0f).translationY(_timer.getHeight()).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                _timer.setVisibility(View.INVISIBLE);
                                startGame = true;
                            }
                        }).setStartDelay(1000);
                    }
                }.start();
            }
        });
        animator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round((endValue - startValue) * fraction);
            }
        });
        animator.setDuration(2000);
        animator.start();
    }

    private boolean isAccused(){
        if(MapUtils.distBetween(myPos, enemyPos) <= THR && startGame) return true;
        return false;
    }

    public LatLng trace(LatLng current){
        double lat = current.latitude, lng = current.longitude;
        if(current.latitude < myPos.latitude) lat += enemySpeed;
        else if(current.latitude > myPos.latitude) lat -= enemySpeed;
        if(current.longitude < myPos.longitude) lng += enemySpeed;
        else if(current.longitude > myPos.longitude) lng -= enemySpeed;
        LatLng res = new LatLng(lat, lng);
        return res;
    }

    public void gameOver(){
        startGame = false;
        pause = false;
        mGPS.stopUsingGPS();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Intent i = new Intent(this, GameOverActivity.class);
        i.putExtra("distance", MapUtils.distBetween(myPos, startPos));
        startActivity(i);
        overridePendingTransition(R.anim.push_in, R.anim.push_out);
        finish();
    }

    public void refreshTrace(){
        if(isAccused()){
            gameOver();
        }else{
            enemyPos = trace(enemyPos);
            enemyPath.add(enemyPos);
            enemyRoute.setPoints(enemyPath);
        }
    }

    public void initMap(){
        map.clear();
        map.getUiSettings().setMapToolbarEnabled(false);
        map.addMarker(new MarkerOptions().position(myPos).title("Start Position"));
        myPath.add(myPos);
        enemyPath.add(enemyPos);
        enemyRoute = map.addPolyline(new PolylineOptions().color(getResources().getColor(R.color.blue)).width(70.0f));
        route = map.addPolyline(new PolylineOptions().color(getResources().getColor(R.color.red)).width(70.0f));
        route.setPoints(myPath);
        enemyRoute.setPoints(enemyPath);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, ZOOM));
        map.animateCamera(CameraUpdateFactory.zoomTo(ZOOM), 2000, null);
    }

    public void refreshMap(){
        refreshTrace();
        myPath.add(myPos);
        route.setPoints(myPath);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, ZOOM));
    }

    public boolean mFlag;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){ if(msg.what == 0) mFlag = false; }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            if(!mFlag) {
                toast("뒤로 버튼을 한번 더 누르시면 게임을 중단합니다.");
                mFlag = true;
                mHandler.sendEmptyMessageDelayed(0, 2000);
                return false;
            } else {
                mGPS.stopUsingGPS();
                finish();
                overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case "LOC":
                    if(startGame) {
                        _remain.setText(MapUtils.distBetween(myPos, enemyPos) + " m");
                        _run.setText(MapUtils.distBetween(myPos, startPos) + " m");
                        if (MapUtils.distBetween(myPos, enemyPos) <= THR * 3)
                            _remain.setTextColor(getResources().getColor(R.color.red));
                        else _remain.setTextColor(getResources().getColor(R.color.white));
                    }
                    mGPS.getLocation();
                    if (!mGPS.isGPSEnabled) {
                        toast("GPS를 사용하도록 설정하세요");
                        Intent intent2 = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent2.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivity(intent2);
                        pause = true;
                    }
                    myPos = mGPS.getLatLng();
                    refreshMap();
                    break;
                default: break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!new GPSTracker(this).isGPSEnabled) {
            toast("GPS를 사용하도록 설정하세요");
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
            pause = true;
        }else{
            pause = false;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("LOC"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mGPS.stopUsingGPS();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

}