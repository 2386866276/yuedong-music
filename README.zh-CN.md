# 悦动音乐

一款基于 Java 原生开发的轻量级 Android 音乐播放器应用。

[Android](https://img.shields.io/badge/Android-5.0%2B-green)
[Java](https://img.shields.io/badge/Java-17-blue)
[版本](https://img.shields.io/badge/version-0.1.0-orange)

---

## 功能特性

- 基于网易云音乐 API 的在线歌曲搜索
- 前台服务实现后台持续播放，附带常驻通知
- 多种主题模式：深色、浅色、咖啡色、VS Code 风格、自定义配色
- LRC 歌词解析，支持多种动画效果
- 播放时封面旋转动画
- 全屏动画开屏页

---

## APK 下载

| 版本 | 文件 | 大小 |
|---------|------|------|
| v0.1.0 | [悦动音乐_0.1.0.apk](https://github.com/2386866276/yuedong-music/releases/download/v0.1.0/%E6%82%A6%E5%8A%A8%E9%9F%B3%E4%B9%90_0.1.0.apk) | ~109 KB |

---

## 从源码编译

### 环境要求

- Android Studio 或 AIDE+
- JDK 17
- Gradle 8.3.2

### 编译步骤

1. 克隆本项目到本地
2. 使用 Android Studio 或 AIDE+ 打开项目
3. 构建并生成签名 APK
4. 使用项目自带的 `app-release.jks` 或在 `app/build.gradle` 中配置自定义签名文件

```bash
git clone https://github.com/2386866276/yuedong-music.git
cd yuedong-music
```

---

## 项目结构

```
悦动音乐/
├── app/
│   ├── src/main/
│   │   ├── java/com/yuedong/music/
│   │   │   ├── MainActivity.java       # 主播放界面
│   │   │   ├── SplashActivity.java     # 开屏启动页
│   │   │   ├── MusicService.java       # 后台播放服务
│   │   │   └── ThemePalette.java       # 主题配色定义
│   │   ├── res/                        # 布局、图片及样式资源
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── ConfusionDictionary.txt
├── build.gradle
└── settings.gradle
```

---

## 技术栈

- 开发语言：Java 17
- 最低 SDK：26（Android 8.0）
- 目标 SDK：37
- 构建工具：Gradle 8.3.2
- 音乐接口：网易云音乐 API（搜索、播放地址、歌词）
- 数据存储：SharedPreferences（主题设置持久化）

---

## 开源协议

本项目采用 GNU General Public License v3.0 协议。  
详情见 [LICENSE](LICENSE) 文件。

---

## 作者

**林映雪**  
QQ: 2386866276