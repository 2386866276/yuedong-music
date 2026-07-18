package com.yuedong.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

// 注意：这里移除了 AndroidX 的导入，改用原生的 Notification

public class MusicService extends Service {

    public static MediaPlayer mediaPlayer;
    public static boolean isPlaying = false;
    
    private static final String CHANNEL_ID = "yuedong_music_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 使用原生 Notification.Builder 构建通知
        Notification.Builder builder;
        
        // Android 8.0 以上需要指定 ChannelId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        
        Notification notification = builder
                .setContentTitle("悦动音乐")
                .setContentText("正在后台播放")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, notification);
        
        // 处理播放/暂停动作
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("PAUSE")) {
                pause();
            } else if (intent.getAction().equals("PLAY")) {
                play();
            }
        }
        return START_STICKY;
    }

    public static void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public static void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public static void startMusic(String url) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        // 仅在 Android 8.0 及以上版本创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, 
                    "悦动音乐播放器", 
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) { 
        return null; 
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
