package com.now.seek.and.hide.hideandseek;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class BaseActivity extends Activity {
    private static Typeface mTypeface = null;

    @Override
    public void setContentView(int layoutResID) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.setContentView(layoutResID);
        if (mTypeface == null) mTypeface = Typeface.createFromAsset(this.getAssets(), "fonts/SeoulNamsanL.ttf");
        //setGlobalFont(getWindow().getDecorView());
    }

    private void setGlobalFont(View view) {
        if (view != null) {
            if(view instanceof ViewGroup){
                ViewGroup vg = (ViewGroup)view;
                int vgCnt = vg.getChildCount();
                for(int i=0; i < vgCnt; i++){
                    View v = vg.getChildAt(i);
                    if(v instanceof TextView) ((TextView) v).setTypeface(mTypeface);
                    setGlobalFont(v);
                }
            }
        }
    }}
