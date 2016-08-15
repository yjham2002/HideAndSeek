package com.now.seek.and.hide.hideandseek;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class IntroActivity extends BaseActivity {

    private Handler h;
    private int delayTime = 1200;
    private ImageView iv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        iv = (ImageView)findViewById(R.id.imageView);
        iv.setDrawingCacheEnabled(true);

        AnimationSet animset = new AnimationSet(false);
        //Animation anim1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anim);
        Animation anim2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_anim);
        //animset.addAnimation(anim1);
        animset.addAnimation(anim2);
        iv.startAnimation(animset);
        h = new Handler();

        animset.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                h.postDelayed(intro, delayTime);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

    }

    Runnable intro = new Runnable() {
        public void run() {
            Intent i = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        h.removeCallbacks(intro);
    }
}
