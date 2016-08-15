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
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends BaseGameActivity {

    public static final int COUNT_TIME = 10;

    private GPSTracker mGPS;
    private LatLng myPos, enemyPos;
    private TextView _timer;
    private CountDownTimer countDownTimer;
    private GoogleMap map;
    private PolylineOptions myPath;


    public void toast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        _timer = (TextView)findViewById(R.id.textView);

        mGPS = new GPSTracker(this);
        enemyPos = myPos = mGPS.getLatLng();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        initMap();

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

    @Override
    public void onSignInSucceeded(){
        Games.setViewForPopups(getApiClient(), findViewById(R.id.gps_popup));
    }

    @Override
    public void onSignInFailed(){
    }

    public void initMap(){
        map.clear();
        map.addMarker(new MarkerOptions().position(myPos).title("Start Position"));
        myPath = new PolylineOptions().color(getResources().getColor(R.color.colorAccent)).width(5.0f);
        myPath.add(myPos);
        map.addPolyline(myPath);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    public void refreshMap(){
        myPath.add(myPos);
        map.addMarker(new MarkerOptions().position(myPos));
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
                    toast("Location Updated");
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