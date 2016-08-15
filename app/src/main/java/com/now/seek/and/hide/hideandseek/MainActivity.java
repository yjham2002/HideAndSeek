package com.now.seek.and.hide.hideandseek;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.Games;

public class MainActivity extends BaseGameActivity implements View.OnClickListener{

    public static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 17;
    public static final int REQUEST_LEADERBOARD = 100, REQUEST_ACHIVEMENT = 101;
    private SignInButton _SignInButton;
    private Button _start, _leader, _mile, _how;

    public void toast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.googleSignIn:
                beginUserInitiatedSignIn();
                break;
            case R.id.bt_how:
                break;
            case R.id.bt_start:
                if(isPermissionGranted(this)) {
                    Games.Achievements.unlock(getApiClient(), getResources().getString(R.string.achievement_starter));
                    startActivity(new Intent(this, MapActivity.class));
                    overridePendingTransition(R.anim.push_in, R.anim.push_out);
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_COURSE_LOCATION);
                }
                break;
            case R.id.bt_leader:
                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()), REQUEST_LEADERBOARD);
                break;
            case R.id.bt_mile:
                startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), REQUEST_ACHIVEMENT);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _start = (Button)findViewById(R.id.bt_start);
        _leader = (Button)findViewById(R.id.bt_leader);
        _mile = (Button)findViewById(R.id.bt_mile);
        _how = (Button)findViewById(R.id.bt_how);
        _SignInButton = (SignInButton)findViewById(R.id.googleSignIn);

        _start.setOnClickListener(this);
        _leader.setOnClickListener(this);
        _mile.setOnClickListener(this);
        _how.setOnClickListener(this);
        _SignInButton.setOnClickListener(this);

        _start.setTranslationY(_start.getHeight());
        _start.setAlpha(0.0f);
        _start.setVisibility(View.INVISIBLE);
        _leader.setTranslationY(_leader.getHeight());
        _leader.setAlpha(0.0f);
        _leader.setVisibility(View.INVISIBLE);
        _mile.setTranslationY(_mile.getHeight());
        _mile.setAlpha(0.0f);
        _mile.setVisibility(View.INVISIBLE);
        requestPermit(this);
    }

    public static boolean isPermissionGranted(Context context){
        return !( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    @TargetApi(23)
    private void requestPermit(Context context) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }
    }

    @Override
    public void onSignInSucceeded(){
        Games.setViewForPopups(getApiClient(), findViewById(R.id.gps_popup));
        _SignInButton.animate().alpha(0.0f).translationY(_SignInButton.getHeight()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                _start.setVisibility(View.VISIBLE);
                _leader.setVisibility(View.VISIBLE);
                _mile.setVisibility(View.VISIBLE);
                _start.animate().translationY(0).alpha(1.0f);
                _leader.animate().translationY(0).alpha(1.0f);
                _mile.animate().translationY(0).alpha(1.0f);
                _SignInButton.setVisibility(View.GONE);
            }
        }).setStartDelay(500);
    }

    @Override
    public void onSignInFailed(){
        _SignInButton.setVisibility(View.VISIBLE);
        toast(getResources().getString(R.string.SignInFailed));
    }

    public boolean mFlag;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){ if(msg.what == 0) mFlag = false; }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            if(!mFlag) {
                toast("뒤로 버튼을 한번 더 누르시면 종료됩니다.");
                mFlag = true;
                mHandler.sendEmptyMessageDelayed(0, 2000);
                return false;
            } else {
                finish();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
