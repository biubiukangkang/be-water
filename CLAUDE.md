# Be Water — Android 番茄钟

## 项目简介

极简侘寂风番茄钟 App。Kotlin + Jetpack Compose。

## 构建

```bash
export JAVA_HOME="C:/Java/jdk-17.0.2"
export ANDROID_HOME="D:/SDK"
./gradlew assembleDebug       # debug APK
./gradlew assembleRelease     # 正式版 APK（需签名密钥）
```

APK 输出：`app/build/outputs/apk/{debug,release}/`

## 项目结构

```
app/src/main/java/com/pomodorotimer/
  ui/
    timer/           — 主计时界面 + ViewModel
      components/    — CircularTimer, Controls, TimeDisplay, TomatoDots
    settings/        — 设置面板 + ViewModel
    stats/           — 统计面板 + ViewModel
    theme/           — Color, Theme, Typography
  data/
    local/           — Room 数据库 (PomodoroRecord, DAO)
    preferences/     — DataStore 设置存储
  service/           — TimerService (前台通知), AudioManager
```

## 关键约定

- 设计参考：`.superpowers/brainstorm/3430-1781671091/content/be-water-v4.html`
- 配色：侘寂风暖棕色调，深色 `#2A2520` / 浅色 `#F5F0EB`
- 按钮：玻璃态（半透明背景 + 边框），无 `shadow()`（防渲染多边形变形）
- 时间显示：放在 CircularTimer 圆环正中间，等宽字体
- 不修改 `build.gradle.kts` 和依赖版本

## 本地环境

- JDK: `C:\Java\jdk-17.0.2`
- SDK: `D:\SDK` (platform 34, build-tools 34+)
- Gradle: wrapper 8.5
- 路径含中文，已在 `gradle.properties` 加 `android.overridePathCheck=true`

## 签名

正式版密钥：`app/be-water-keystore.jks`（已加入 .gitignore）
密码：见 `app/build.gradle.kts` signingConfigs 块

## 已知待修

- 顶部栏功能按钮排版异常（待修复）
