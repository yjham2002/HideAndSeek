package com.now.seek.and.hide.hideandseek;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.games.Games;

public class GameOverActivity extends BaseGameActivity implements View.OnClickListener{

    public static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 17;
    public static final int REQUEST_LEADERBOARD = 100, REQUEST_ACHIVEMENT = 101;
    private Button _next;
    private TextView _score;
    private int score = 0;

    public void toast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_next: finish(); break;
            default: break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over);
        score = (int)getIntent().getExtras().getDouble("distance");
        _score = (TextView)findViewById(R.id.score);
        _score.setText(Integer.toString(score));
        _next = (Button)findViewById(R.id.bt_next);
        _next.setOnClickListener(this);
    }

    @Override
    public void onSignInSucceeded(){
        Games.Leaderboards.submitScore(getApiClient(), getResources().getString(R.string.leaderboard_distance), score);
        if(score >= 1000) Games.Achievements.unlock(getApiClient(), getResources().getString(R.string.achievement_1km));
        if(score >= 2000) Games.Achievements.unlock(getApiClient(), getResources().getString(R.string.achievement_2km));
        if(score >= 3000) Games.Achievements.unlock(getApiClient(), getResources().getString(R.string.achievement_3km));
        if(score >= 5000) Games.Achievements.unlock(getApiClient(), getResources().getString(R.string.achievement_5km));
        if(score >= 10000) Games.Achievements.unlock(getApiClient(), getResources().getString(R.string.achievement_10km));
        if(score >= 20000) Games.Achievements.unlock(getApiClient(), getResources().getString(R.string.achievement_20km));
    }

    @Override
    public void onSignInFailed(){
    }
}
