package com.yuedong.music;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏系统状态栏，实现全屏效果
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.splash_logo);
        TextView text = findViewById(R.id.splash_text);

        // 1. 加载 Logo 动画
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.splash_scale);
        logo.startAnimation(scaleAnim);

        // 2. 延迟启动文字淡入动画
        new Handler().postDelayed(() -> {
            text.animate().alpha(1f).setDuration(500).start();
        }, 800);

        // 3. 延时跳转到主页 (总时长 2500毫秒)
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // 淡出过渡动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); // 销毁开屏页
        }, 2500);
    }
}
