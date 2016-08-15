package com.now.seek.and.hide.hideandseek;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends BaseActivity {

    public static final int COUNT_TIME = 10;

    private TextView _timer;
    private CountDownTimer countDownTimer;
    private GoogleMap map;

    public void toast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        _timer = (TextView)findViewById(R.id.textView);

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

    public void initMap(){
        map.clear();
        map.addMarker(new MarkerOptions().position(new LatLng(36.5, 127.0)).title("현 위치"));
        PolylineOptions partPath = new PolylineOptions().color(getResources().getColor(R.color.colorAccent)).width(5.0f);
        map.addPolyline(partPath);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(36.5, 127.0), 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
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
                finish();
                overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}