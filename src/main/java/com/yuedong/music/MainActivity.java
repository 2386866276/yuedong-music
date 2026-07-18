package com.yuedong.music;

import com.yuedong.music.R;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    // 接口地址
    private static final String API_SEARCH = "https://music.163.com/api/search/song/list/page";
    private static final String API_SONG_URL = "https://music.163.com/api/song/enhance/player/url/v1";
    private static final String API_LYRIC = "https://music.163.com/api/song/lyric/v1";

    // 控件
    private View rootView;
    private EditText etSearch;
    private Button btnSearch, btnPlayPause, btnBackList, btnTheme;
    private ListView lvMusicList;
    private LinearLayout playerPanel;
    private ImageView ivCover;
    private TextView tvTitle, tvArtist, tvLyric;
    private SeekBar seekBar;

    // 数据
    private List<Map<String, String>> musicList = new ArrayList<>();
    private List<String> musicTitles = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    
    // 主题相关
    private ThemePalette currentPalette;
    private String currentThemeMode;
    private SharedPreferences prefs;
    
    // 动画与更新
    private ObjectAnimator rotateAnimator;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isSeekBarTouching = false;
    private List<Map<Long, String>> lyricList = new ArrayList<>();
    private String currentLyricAnimation = ThemePalette.ANIM_FADE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        currentThemeMode = prefs.getString("theme_mode", ThemePalette.MODE_DARK);
        
        setContentView(R.layout.activity_main);

        initViews();
        applyTheme();
        setupAnimations();
        startUpdateThread();
        
        startService(new Intent(this, MusicService.class));
    }

    private void applyTheme() {
        currentPalette = ThemePalette.forMode(currentThemeMode);
        
        if (ThemePalette.MODE_CUSTOM.equals(currentThemeMode)) {
            Map<String, String> customColors = new HashMap<>();
            customColors.put("bg", prefs.getString("custom_bg", "#000000"));
            customColors.put("accent", prefs.getString("custom_accent", "#30D158"));
            currentPalette = currentPalette.withCustomColors(customColors);
        }

        rootView.setBackgroundColor(currentPalette.bg);
        etSearch.setBackgroundColor(currentPalette.inputBg);
        etSearch.setTextColor(currentPalette.text);
        etSearch.setHintTextColor(currentPalette.textTertiary);
        btnSearch.setBackgroundTintList(ColorStateList.valueOf(currentPalette.accent));
        btnTheme.setBackgroundTintList(ColorStateList.valueOf(currentPalette.accentDim));
        
        lvMusicList.setBackgroundColor(currentPalette.surface);
        lvMusicList.setDivider(new android.graphics.drawable.ColorDrawable(currentPalette.borderLight));
        lvMusicList.setDividerHeight(1);

        playerPanel.setBackgroundColor(currentPalette.bg);
        tvTitle.setTextColor(currentPalette.text);
        tvArtist.setTextColor(currentPalette.textSecondary);
        tvLyric.setTextColor(currentPalette.textSecondary);
        
        btnPlayPause.setTextColor(currentPalette.textOnColor);
        btnBackList.setTextColor(currentPalette.textOnColor);
    }

    private void initViews() {
        rootView = findViewById(R.id.root_view);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnTheme = findViewById(R.id.btn_theme);
        lvMusicList = findViewById(R.id.lv_music_list);
        playerPanel = findViewById(R.id.player_panel);
        ivCover = findViewById(R.id.iv_cover);
        tvTitle = findViewById(R.id.tv_title);
        tvArtist = findViewById(R.id.tv_artist);
        tvLyric = findViewById(R.id.tv_lyric);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnBackList = findViewById(R.id.btn_back_list);
        seekBar = findViewById(R.id.seek_bar);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, musicTitles);
        lvMusicList.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String kw = etSearch.getText().toString().trim();
            if (!kw.isEmpty()) searchMusic(kw);
        });

        lvMusicList.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < musicList.size()) {
                playMusicAtPosition(position);
            }
        });

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnBackList.setOnClickListener(v -> {
            playerPanel.setVisibility(View.GONE);
            lvMusicList.setVisibility(View.VISIBLE);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) { isSeekBarTouching = true; }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (MusicService.mediaPlayer != null) {
                    MusicService.mediaPlayer.seekTo(seekBar.getProgress());
                    isSeekBarTouching = false;
                }
            }
        });
        
        btnTheme.setOnClickListener(v -> showThemeDialog());
        
        // 唱片封面点击事件
        ivCover.setOnClickListener(v -> showLyricDetail());
    }

    private void showThemeDialog() {
        String[] items = {"深色模式", "浅色模式", "咖啡色", "深灰色", "自定义", "歌词动画", "关于"};
        int checkedItem = 0;
        if (ThemePalette.MODE_LIGHT.equals(currentThemeMode)) checkedItem = 1;
        if (ThemePalette.MODE_COFFEE.equals(currentThemeMode)) checkedItem = 2;
        if (ThemePalette.MODE_VSCODE.equals(currentThemeMode)) checkedItem = 3;
        if (ThemePalette.MODE_CUSTOM.equals(currentThemeMode)) checkedItem = 4;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeDialog);
        builder.setTitle("选择主题");
        builder.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
            if (which == 4) {
                currentThemeMode = ThemePalette.MODE_CUSTOM;
                showCustomColorEditor();
            } else if (which == 5) {
                showAnimationDialog();
            } else if (which == 6) {
                showAboutPage();
            } else {
                if (which == 0) currentThemeMode = ThemePalette.MODE_DARK;
                else if (which == 1) currentThemeMode = ThemePalette.MODE_LIGHT;
                else if (which == 2) currentThemeMode = ThemePalette.MODE_COFFEE;
                else if (which == 3) currentThemeMode = ThemePalette.MODE_VSCODE;
                prefs.edit().putString("theme_mode", currentThemeMode).apply();
                applyTheme();
            }
            dialog.dismiss();
        });
        builder.show();
    }
    
    private void showCustomColorEditor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeDialog);
        builder.setTitle("自定义主题");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 20);
        
        LinearLayout colorInputLayout = new LinearLayout(this);
        colorInputLayout.setOrientation(LinearLayout.HORIZONTAL);
        colorInputLayout.setPadding(0, 0, 0, 15);
        
        final EditText etBg = new EditText(this);
        etBg.setHint("背景色 (例如: #1E1E1E)");
        etBg.setText(prefs.getString("custom_bg", "#1E1E1E"));
        etBg.setSingleLine(true);
        etBg.setPadding(12, 8, 12, 8);
        etBg.setBackgroundTintList(ColorStateList.valueOf(0xFFE0E0E0));
        
        final EditText etAccent = new EditText(this);
        etAccent.setHint("强调色 (例如: #007ACC)");
        etAccent.setText(prefs.getString("custom_accent", "#007ACC"));
        etAccent.setSingleLine(true);
        etAccent.setPadding(12, 8, 12, 8);
        etAccent.setBackgroundTintList(ColorStateList.valueOf(0xFFE0E0E0));
        
        colorInputLayout.addView(etBg);
        colorInputLayout.addView(etAccent);
        layout.addView(colorInputLayout);
        
        final View previewView = new View(this);
        previewView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 100));
        previewView.setBackgroundColor(Color.parseColor("#1E1E1E"));
        layout.addView(previewView);
        
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String bgHex = etBg.getText().toString();
                    String accentHex = etAccent.getText().toString();
                    
                    if (ThemePalette.isHexColor(bgHex)) {
                        previewView.setBackgroundColor(Color.parseColor(bgHex));
                    }
                    if (ThemePalette.isHexColor(accentHex)) {
                        // 可以在这里添加其他预览效果
                    }
                } catch (Exception e) {}
            }
        };
        
        etBg.addTextChangedListener(textWatcher);
        etAccent.addTextChangedListener(textWatcher);
        
        builder.setView(layout);
        
        builder.setPositiveButton("保存并应用", (dialog, which) -> {
            String bgHex = etBg.getText().toString();
            String accentHex = etAccent.getText().toString();
            
            prefs.edit()
                .putString("custom_bg", bgHex)
                .putString("custom_accent", accentHex)
                .putString("theme_mode", ThemePalette.MODE_CUSTOM)
                .apply();
            applyTheme();
            Toast.makeText(this, "自定义主题已应用", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    private void showAnimationDialog() {
        String[] animations = {"无动画", "淡入淡出", "滚动", "打字机效果"};
        int checkedItem = 0;
        if (ThemePalette.ANIM_FADE.equals(currentLyricAnimation)) checkedItem = 1;
        else if (ThemePalette.ANIM_SCROLL.equals(currentLyricAnimation)) checkedItem = 2;
        else if (ThemePalette.ANIM_TYPEWRITER.equals(currentLyricAnimation)) checkedItem = 3;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeDialog);
        builder.setTitle("选择歌词动画效果");
        builder.setSingleChoiceItems(animations, checkedItem, (dialog, which) -> {
            if (which == 1) currentLyricAnimation = ThemePalette.ANIM_FADE;
            else if (which == 2) currentLyricAnimation = ThemePalette.ANIM_SCROLL;
            else if (which == 3) currentLyricAnimation = ThemePalette.ANIM_TYPEWRITER;
            dialog.dismiss();
        });
        builder.show();
    }
    
    private void showAboutPage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeDialog);
        builder.setTitle("关于悦动音乐");
        
        String aboutText = "悦动音乐\n\n" +
                         "作者：林映雪\n" +
                         "QQ：2386866276\n\n" +
                         "隐私政策：https://example.com/privacy\n" +
                         "服务条款：https://example.com/terms";
        
        builder.setMessage(aboutText);
        builder.setPositiveButton("确定", null);
        builder.show();
    }
    
    private void showLyricDetail() {
        if (lyricList.isEmpty()) {
            Toast.makeText(this, "暂无歌词", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeDialog);
        builder.setTitle("歌词详情");
        
        TextView lyricView = new TextView(this);
        lyricView.setPadding(20, 20, 20, 20);
        lyricView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        lyricView.setTextSize(16);
        
        if (MusicService.mediaPlayer != null && MusicService.isPlaying) {
            long currentPosition = MusicService.mediaPlayer.getCurrentPosition();
            String currentLyric = "...";
            
            for (int i = lyricList.size() - 1; i >= 0; i--) {
                long time = lyricList.get(i).keySet().iterator().next();
                if (currentPosition >= time) {
                    currentLyric = lyricList.get(i).values().iterator().next();
                    break;
                }
            }
            
            lyricView.setText(currentLyric);
        } else {
            lyricView.setText("请先播放歌曲");
        }
        
        builder.setView(lyricView);
        builder.setPositiveButton("关闭", null);
        builder.show();
    }

    private void setupAnimations() {
        rotateAnimator = ObjectAnimator.ofFloat(ivCover, "rotation", 0f, 360f);
        rotateAnimator.setDuration(20000);
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimator.setInterpolator(new LinearInterpolator());
    }

    private void togglePlayPause() {
        Intent intent = new Intent(this, MusicService.class);
        if (MusicService.isPlaying) {
            intent.setAction("PAUSE");
            btnPlayPause.setText("播放");
        } else {
            intent.setAction("PLAY");
            btnPlayPause.setText("暂停");
        }
        startService(intent);
        updateAnimationState();
    }

    private void updateAnimationState() {
        if (MusicService.isPlaying) {
            if (!rotateAnimator.isRunning()) rotateAnimator.start();
            else rotateAnimator.resume();
        } else {
            rotateAnimator.pause();
        }
    }

    private void startUpdateThread() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MusicService.mediaPlayer != null && MusicService.isPlaying) {
                    if (!isSeekBarTouching) {
                        seekBar.setMax(MusicService.mediaPlayer.getDuration());
                        seekBar.setProgress(MusicService.mediaPlayer.getCurrentPosition());
                    }
                    updateLyricWithAnimation(MusicService.mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private void updateLyricWithAnimation(long pos) {
        if (lyricList.isEmpty()) return;
        
        String line = "...";
        for (int i = lyricList.size() - 1; i >= 0; i--) {
            long time = lyricList.get(i).keySet().iterator().next();
            if (pos >= time) {
                line = lyricList.get(i).values().iterator().next();
                break;
            }
        }
        
        switch (currentLyricAnimation) {
            case ThemePalette.ANIM_FADE:
                tvLyric.animate().alpha(1f).setDuration(300).start();
                break;
            case ThemePalette.ANIM_SCROLL:
                // 滚动动画效果
                break;
            case ThemePalette.ANIM_TYPEWRITER:
                // 打字机效果
                break;
        }
        
        tvLyric.setText(line);
    }

    // --- 网络请求部分 ---
    
    private void searchMusic(String keyword) {
        musicList.clear();
        musicTitles.clear();
        tvLyric.setText("正在搜索...");
        new Thread(() -> {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("keyword", keyword);
                params.put("scene", "NORMAL");
                params.put("limit", "20");
                params.put("offset", "0");
                params.put("needCorrect", "true");
                params.put("header", "");
                String res = doPost(API_SEARCH, params, null);
                JSONObject root = new JSONObject(res);
                if (root.has("data") && !root.isNull("data")) {
                    JSONObject data = root.getJSONObject("data");
                    if (data.has("resources")) {
                        JSONArray arr = data.getJSONArray("resources");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject resObj = arr.getJSONObject(i);
                            JSONObject baseInfo = resObj.optJSONObject("baseInfo");
                            if (baseInfo == null) continue;
                            JSONObject song = baseInfo.optJSONObject("simpleSongData");
                            if (song == null) continue;
                            String id = song.optString("id");
                            String name = song.optString("name");
                            StringBuilder singer = new StringBuilder();
                            JSONArray ar = song.optJSONArray("ar");
                            if (ar != null) {
                                for (int j = 0; j < ar.length(); j++) singer.append(ar.getJSONObject(j).optString("name")).append(" ");
                            }
                            String cover = song.optJSONObject("al") != null ? song.optJSONObject("al").optString("picUrl") : "";
                            musicTitles.add(name + " - " + singer);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", id); map.put("name", name);
                            map.put("singer", singer.toString().trim()); map.put("cover", cover);
                            musicList.add(map);
                        }
                    }
                }
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    if (musicTitles.isEmpty()) Toast.makeText(this, "未找到结果", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void playMusicAtPosition(int pos) {
        Map<String, String> data = musicList.get(pos);
        String id = data.get("id");
        runOnUiThread(() -> {
            playerPanel.setVisibility(View.VISIBLE);
            lvMusicList.setVisibility(View.GONE);
            tvTitle.setText(data.get("name"));
            tvArtist.setText(data.get("singer"));
            btnPlayPause.setText("暂停");
            if (!rotateAnimator.isRunning()) rotateAnimator.start();
        });
        new Thread(() -> {
            try {
                Map<String, String> p = new HashMap<>();
                p.put("ids", "[\"" + id + "\"]");
                p.put("level", "standard");
                p.put("encodeType", "aac");
                p.put("header", "");
                String res = doPost(API_SONG_URL, p, null);
                JSONObject root = new JSONObject(res);
                JSONArray dataArray = root.optJSONArray("data");
                if (dataArray != null && dataArray.length() > 0) {
                    String url = dataArray.getJSONObject(0).optString("url");
                    if (url != null && !url.isEmpty() && !url.equals("null")) {
                        MusicService.startMusic(url);
                        loadLyric(id);
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "无版权或VIP", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadLyric(String id) {
        new Thread(() -> {
            try {
                Map<String, String> p = new HashMap<>();
                p.put("id", id); p.put("lv", "-1"); p.put("tv", "-1");
                String res = doPost(API_LYRIC, p, null);
                JSONObject root = new JSONObject(res);
                if (root.has("lrc") && !root.isNull("lrc")) {
                    parseLyric(root.getJSONObject("lrc").optString("lyric"));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void parseLyric(String lrc) {
        lyricList.clear();
        if (lrc == null || lrc.isEmpty()) return;
        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)");
        for (String line : lrc.split("\n")) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                long time = Long.parseLong(matcher.group(1)) * 60000 + Long.parseLong(matcher.group(2)) * 1000 + Long.parseLong(matcher.group(3));
                String text = matcher.group(4).trim();
                if (!text.isEmpty()) {
                    Map<Long, String> map = new HashMap<>();
                    map.put(time, text);
                    lyricList.add(map);
                }
            }
        }
    }

    private String doPost(String urlStr, Map<String, String> params, String cookie) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (postData.length() > 0) postData.append("&");
            postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            postData.append("=");
            postData.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        OutputStream os = conn.getOutputStream();
        os.write(postData.toString().getBytes("UTF-8"));
        os.flush(); os.close();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        return response.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
