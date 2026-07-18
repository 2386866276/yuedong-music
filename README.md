# Yuedong Music

A lightweight Android music player application built with Java.

[Android](https://img.shields.io/badge/Android-5.0%2B-green)
[Java](https://img.shields.io/badge/Java-17-blue)
[Version](https://img.shields.io/badge/version-0.1.0-orange)

---

## Features

- Online song search via NetEase Cloud Music API
- Background playback with foreground service and persistent notification
- Multiple theme modes: Dark, Light, Coffee, VS Code, and Custom
- LRC lyrics parsing with animation effects
- Rotating cover art animation during playback
- Full-screen animated splash screen

---

## Download APK

| Version | File | Size |
|---------|------|------|
| v0.1.0 | [yuedong-music_0.1.0.apk](https://github.com/2386866276/yuedong-music/releases/download/v0.1.0/_0.1.0.apk) | ~109 KB |

---

## Build from Source

### Prerequisites

- Android Studio or AIDE+
- JDK 17
- Gradle 8.3.2

### Steps

1. Clone this repository
2. Open the project in Android Studio or AIDE+
3. Build and generate signed APK
4. Use the provided `app-release.jks` or configure a custom keystore in `app/build.gradle`

```bash
git clone https://github.com/2386866276/yuedong-music.git
cd yuedong-music
```

---

## Project Structure

```
悦动音乐/
├── app/
│   ├── src/main/
│   │   ├── java/com/yuedong/music/
│   │   │   ├── MainActivity.java       # Main player interface
│   │   │   ├── SplashActivity.java     # Launch screen activity
│   │   │   ├── MusicService.java       # Background playback service
│   │   │   └── ThemePalette.java       # Theme color definitions
│   │   ├── res/                        # Layouts, drawables, and values
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── ConfusionDictionary.txt
├── build.gradle
└── settings.gradle
```

---

## Tech Stack

- Language: Java 17
- Min SDK: 26 (Android 8.0)
- Target SDK: 37
- Build System: Gradle 8.3.2
- APIs: NetEase Cloud Music (search, playback URL, lyrics)
- Storage: SharedPreferences for theme persistence

---

## License

This project is licensed under the GNU General Public License v3.0.  
See the [LICENSE](LICENSE) file for details.

---

## Author

**林映雪**  
QQ: 2386866276
